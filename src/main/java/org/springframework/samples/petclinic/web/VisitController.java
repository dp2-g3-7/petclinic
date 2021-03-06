/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.web;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.MedicalTest;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetRegistrationStatus;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.MedicalTestService;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.samples.petclinic.service.PetService;
import org.springframework.samples.petclinic.service.VisitService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class VisitController {

	private static final String PETS_CREATE_OR_UPDATE_VISIT_FORM = "pets/createOrUpdateVisitForm";

	private static final String VISIT = "visit";

	private static final String ADMIN = "admin";

	private static final String REDIRECT_TO_OUPS = "redirect:/oups";

	private static final PetRegistrationStatus ACCEPTED = PetRegistrationStatus.ACCEPTED;

	private final VisitService visitService;

	private final PetService petService;

	private final MedicalTestService medicalTestService;

	private final OwnerService ownerService;

	@Autowired
	public VisitController(final VisitService visitService, final PetService petService,
			final MedicalTestService medicalTestService, final OwnerService ownerService) {
		this.visitService = visitService;
		this.petService = petService;
		this.medicalTestService = medicalTestService;
		this.ownerService = ownerService;
	}

	@InitBinder
	public void setAllowedFields(final WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("tests")
	public Collection<MedicalTest> populateMedicalTests() {
		return this.medicalTestService.findMedicalTests();
	}
	
	private boolean isAdmin() {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();
		return authority.equals(ADMIN);
	}

	private Boolean securityAccessRequestProfile(int ownerId) {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Owner owner = new Owner();

		if (authority.equals("owner"))
			owner = this.ownerService.findOwnerById(ownerId);

		return authority.equals(ADMIN) || authority.equals("owner") && username.equals(owner.getUser().getUsername());
	}

	private boolean securityAccessRequestVisit(int ownerId, int petId) {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();

		boolean isHisPetAcceptedAndActive = false;
		if (authority.equals("veterinarian") || authority.equals(ADMIN)) {
			Pet pet = this.petService.findPetById(petId);
			isHisPetAcceptedAndActive = pet.getOwner().getId().equals(ownerId) && pet.isActive()
					&& pet.getStatus().equals(ACCEPTED);
		}

		return isHisPetAcceptedAndActive && (authority.equals(ADMIN) || authority.equals("veterinarian"));
	}

	/**
	 * Called before each and every @GetMapping or @PostMapping annotated method. 2
	 * goals: - Make sure we always have fresh data - Since we do not use the
	 * session scope, make sure that Pet object always has an id (Even though id is
	 * not part of the form fields)
	 * 
	 * @param petId
	 * @return Pet
	 */

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping(value = "/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm(@RequestParam int vetId, @PathVariable("petId") final int petId, @PathVariable("ownerId") final int ownerId,
			final ModelMap model) {
		if (securityAccessRequestVisit(ownerId, petId) || isAdmin()) {
			Pet pet = this.petService.findPetById(petId);
			Visit visit = new Visit();
			model.put("vetId", vetId);
			model.put(VISIT, visit);
			pet.addVisit(visit);
			return PETS_CREATE_OR_UPDATE_VISIT_FORM;
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping(value = "/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@RequestParam int vetId, @PathVariable("petId") final int petId,
			@PathVariable("ownerId") final int ownerId, @Valid final Visit visit, final BindingResult result) {
		if (securityAccessRequestVisit(ownerId, petId) || isAdmin()) {
			Pet pet = this.petService.findPetById(petId);
			pet.addVisit(visit);
			if (result.hasErrors()) {
				return PETS_CREATE_OR_UPDATE_VISIT_FORM;
			} else {
				this.visitService.saveVisit(visit);
				return isAdmin()?"redirect:/appointments/"+vetId:"redirect:/appointments";
			}
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	@GetMapping(value = "/vets/pets/{petId}/visits/{visitId}")
	public String initUpdateVisitForm(@PathVariable("petId") final int petId,
				@PathVariable("visitId") final int visitId, final ModelMap model) {
		model.put("edit", true);
		Visit visit = this.visitService.findVisitById(visitId);
		model.put(VISIT, visit);
		return PETS_CREATE_OR_UPDATE_VISIT_FORM;

	}

	@PostMapping(value = "/vets/pets/{petId}/visits/{visitId}")
	public String processUpdateVisitForm(@PathVariable("petId") final int petId,
			@PathVariable("visitId") final int visitId, @Valid final Visit visit, final BindingResult result, final ModelMap model) {
		Visit visitToUpdate = this.visitService.findVisitById(visitId);
		model.put("edit", true);
		if (result.hasErrors()) {
			model.put("edit", true);
			return PETS_CREATE_OR_UPDATE_VISIT_FORM;
		} else {
			visitToUpdate.setDescription(visit.getDescription());
			visitToUpdate.setMedicalTests(visit.getMedicalTests());
			this.visitService.saveVisit(visitToUpdate);
			return "redirect:/vets/pets/" + petId + "/visits";
		}
	}

	@GetMapping(value = "/owners/{ownerId}/pets/{petId}/visits/{visitId}")
	public String showVisit(@PathVariable final int ownerId, @PathVariable final int visitId,
			final Map<String, Object> model) {
		if (securityAccessRequestProfile(ownerId) || isAdmin()) {
			Visit visit = this.visitService.findVisitById(visitId);
			model.put(VISIT, visit);
			return "visits/visitDetails";
		} else {
			return REDIRECT_TO_OUPS;
		}
	}
}
