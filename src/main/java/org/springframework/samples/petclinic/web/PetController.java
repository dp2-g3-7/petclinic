/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetRegistrationStatus;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.samples.petclinic.service.PetService;
import org.springframework.samples.petclinic.service.PetTypeService;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedPetNameException;
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
 */
@Controller
public class PetController {

	private static final String PETS_UPDATE_PET_REQUEST = "pets/updatePetRequest";
	private static final String ALREADY_EXISTS = "already exists";
	private static final String DUPLICATE = "duplicate";
	private static final String OWNER = "owner";
	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
	private static final String REDIRECT_TO_OUPS = "redirect:/oups";
	private static final PetRegistrationStatus ACCEPTED = PetRegistrationStatus.ACCEPTED;
	private static final PetRegistrationStatus PENDING = PetRegistrationStatus.PENDING;
	private static final PetRegistrationStatus REJECTED = PetRegistrationStatus.REJECTED;

	private final PetService petService;
	private final OwnerService ownerService;
	private final PetTypeService petTypeService;

	@Autowired
	public PetController(PetService petService, OwnerService ownerService, PetTypeService petTypeService) {
		this.petService = petService;
		this.ownerService = ownerService;
		this.petTypeService=petTypeService;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.petService.findPetTypes();
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping(value = "/owners/{ownerId}/pets/new")
	public String initCreationForm(@PathVariable("ownerId") int ownerId, ModelMap model) {
		if (securityAccessPetRequestAndProfile(ownerId)) {
			Pet pet = new Pet();
			Owner owner = this.ownerService.findOwnerById(ownerId);
			model.addAttribute(OWNER, owner);
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	@PostMapping(value ="/adoptions/pet")
	public String processAdoptForm(@RequestParam String name, @RequestParam String type, @RequestParam String age,ModelMap modelMap) throws DataAccessException, DuplicatedPetNameException {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Owner owner = this.ownerService.findOwnerByUsername(username);
		this.initPet(name, type, age, owner);
		return "redirect:/owner/requests";
	}
	
	private void initPet(String name, String type, String age, Owner owner) throws DataAccessException, DuplicatedPetNameException {
	//Inicialización pet
		Pet petToAdopt = new Pet();
		petToAdopt.setType(this.petType(type));
		petToAdopt.setBirthDate(this.birtdate(age));
		petToAdopt.setName(name);
		petToAdopt.setStatus(PENDING);
		petToAdopt.setJustification("");
		petToAdopt.setActive(true);
		owner.addPet(petToAdopt);
		//guardado mascota
		try {
			this.petService.savePet(petToAdopt);
		} catch (Exception e) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			Integer entero= random.nextInt(0,100);
			petToAdopt.setName(name+"-"+entero);
			this.petService.savePet(petToAdopt);
		}
	}
	
	@PostMapping(value ="/adoptions/owner/{ownerId}/pet")
	public String processAdoptAdminForm(@PathVariable("ownerId") int ownerId, @RequestParam String name, @RequestParam String type, @RequestParam String age,ModelMap modelMap) throws DataAccessException, DuplicatedPetNameException {
		Owner owner = this.ownerService.findOwnerById(ownerId);
		this.initPet(name, type, age, owner);
		return "redirect:/owner/"+ownerId+"/requests";
	}

	@PostMapping(value = "/owners/{ownerId}/pets/new")
	public String processCreationForm(@PathVariable("ownerId") int ownerId, @Valid Pet pet, BindingResult result,
			ModelMap model) {
		if (securityAccessPetRequestAndProfile(ownerId)) {

			Owner owner = this.ownerService.findOwnerById(ownerId);
			model.addAttribute(OWNER, owner);
			if (result.hasErrors()) {
				model.put("pet", pet);
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			} else {
				try {
					pet.setStatus(PENDING);
					pet.setJustification("");
					pet.setActive(true);
					owner.addPet(pet);
					this.petService.savePet(pet);
				} catch (DuplicatedPetNameException ex) {
					result.rejectValue("name", DUPLICATE, ALREADY_EXISTS);
					return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
				}
				return isAdmin()?"redirect:/owner/{ownerId}/requests":"redirect:/owner/requests";
			}

		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	@GetMapping(value = "/owners/{ownerId}/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			ModelMap model) {
		Owner owner = this.ownerService.findOwnerById(ownerId);
		Pet petToUpdate = this.petService.findPetById(petId);
		Boolean isHisPetAcceptedAndAcctive = petToUpdate.getOwner().getId().equals(owner.getId()) && petToUpdate.isActive()
				&& petToUpdate.getStatus().equals(ACCEPTED);
		if (securityAccessPetRequestAndProfile(ownerId) && isHisPetAcceptedAndAcctive) {
			Pet pet = this.petService.findPetById(petId);
			model.addAttribute("pet", pet);
			model.addAttribute(OWNER, pet.getOwner());
			model.addAttribute("edit", true);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;

		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	/**
	 *
	 * @param pet
	 * @param result
	 * @param petId
	 * @param model
	 * @param owner
	 * @param model
	 * @return
	 */
	@PostMapping(value = "/owners/{ownerId}/pets/{petId}/edit")
	public String processUpdateForm(@PathVariable("ownerId") int ownerId, @Valid Pet pet, BindingResult result,
			@PathVariable("petId") int petId, ModelMap model) {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();

		Pet petToUpdate = this.petService.findPetById(petId);

		Boolean isHisPetAcceptedAndAcctive = petToUpdate.getOwner().getId().equals(ownerId) && petToUpdate.isActive()
				&& petToUpdate.getStatus().equals(ACCEPTED);
		if (securityAccessPetRequestAndProfile(ownerId) && isHisPetAcceptedAndAcctive) {
			Owner owner = this.ownerService.findOwnerById(ownerId);

			model.addAttribute(OWNER, owner);
			model.addAttribute("edit", true);

			if (result.hasErrors()) {
				model.put("pet", pet);
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			} else {
				BeanUtils.copyProperties(pet, petToUpdate, "id", OWNER, "stays", "appointments", "visits", "status",
						"justification", "active");
				try {
					this.petService.savePet(petToUpdate);
				} catch (DuplicatedPetNameException ex) {
					result.rejectValue("name", DUPLICATE, ALREADY_EXISTS);
					return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
				}

				if (authority.equals(OWNER)) {
					return "redirect:/owner/pets";
				} else {
					return "redirect:/owners/" + owner.getId();
				}
			}
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	@GetMapping(value = "/owners/{ownerId}/pet/{petId}")
	public String showAndUpdatePetRequest(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			ModelMap model) {
		
		Pet pet = this.petService.findPetById(petId);
		if (securityAccessPetRequestAndProfile(ownerId)) {
			if (!pet.getStatus().equals(PENDING)) {
				model.addAttribute("readonly", true);
			}	
			model.addAttribute("pet", pet);
			model.addAttribute("petRequest", pet);
			return PETS_UPDATE_PET_REQUEST;
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	@PostMapping("/owners/{ownerId}/pet/{petId}")
	public String AnswerPetRequest(@Valid Pet pet, BindingResult result, @PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId, ModelMap model) {

		Pet petToUpdate = this.petService.findPetById(petId);
		if (isAdmin() && petToUpdate.getStatus().equals(PENDING)
				&& petToUpdate.getOwner().getId().equals(ownerId)) {
			model.addAttribute("petRequest", petToUpdate);
			if (result.hasErrors()) {
				model.put("pet", pet);
				return PETS_UPDATE_PET_REQUEST;
			} else {
				petToUpdate.setStatus(pet.getStatus());
				petToUpdate.setJustification(pet.getJustification());
				try {
					this.petService.savePet(petToUpdate);
				} catch (DuplicatedPetNameException ex) {
					result.rejectValue("name", DUPLICATE, ALREADY_EXISTS);
					return PETS_UPDATE_PET_REQUEST;
				}
				return "redirect:/requests";
			}

		} else {
			return isAdmin() ? "redirect:/requests" : REDIRECT_TO_OUPS;
		}
	}

	@GetMapping(value = { "/requests" })
	public String showPetRequests(ModelMap model) {

		List<Pet> petsRequests = this.petService.findPetsRequests(PENDING);
		model.addAttribute("pets", petsRequests);
		return "pets/requests";
	}

	@GetMapping(value = "/owner/requests")
	public String showMyPetRequests(ModelMap model) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Owner owner = this.ownerService.findOwnerByUsername(username);
		List<Pet> myPetsRequests = this.petService.findMyPetsRequests(PENDING, REJECTED, owner.getId());
		model.addAttribute("pets", myPetsRequests);
		return "pets/myRequests";
	}
	
	// MÉTODO PARA EL SUPERUSUARIO
	@GetMapping(value = "/owner/{ownerId}/requests")
	public String showMyPetRequestsSuperUser(@PathVariable("ownerId") int ownerId, ModelMap model) {
		if(isAdmin()) {	
			Owner owner = this.ownerService.findOwnerById(ownerId);
			List<Pet> myPetsRequests = this.petService.findMyPetsRequests(PENDING, REJECTED, owner.getId());
			model.addAttribute("pets", myPetsRequests);
			return "pets/myRequests";
		} else {
			return REDIRECT_TO_OUPS;
		}
	}
	
	// MÉTODO PARA EL SUPERUSUARIO
		@GetMapping(value = "/owner/{ownerId}/pets")
		public String showMyPetsActiveSuperUser(@PathVariable("ownerId") int ownerId, ModelMap model) {
			if(isAdmin()) {	
				Owner owner = this.ownerService.findOwnerById(ownerId);
				List<Pet> myPets = this.petService.findMyPetsAcceptedByActive(ACCEPTED, true, owner.getId());
				model.addAttribute("disabled", this.petService.countMyPetsAcceptedByActive(ACCEPTED, false, owner.getId()) != 0);
				model.addAttribute(OWNER, owner);
				model.addAttribute("pets", myPets);
				return "pets/myPetsActive";
			} else {
				return REDIRECT_TO_OUPS;
			}
		}

	@GetMapping(value = "/owner/pets")
	public String showMyPetsActive(ModelMap model) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Owner owner = this.ownerService.findOwnerByUsername(username);
		List<Pet> myPets = this.petService.findMyPetsAcceptedByActive(ACCEPTED, true, owner.getId());
		model.addAttribute("disabled", this.petService.countMyPetsAcceptedByActive(ACCEPTED, false, owner.getId()) != 0);
		model.addAttribute(OWNER, owner);
		model.addAttribute("pets", myPets);
		return "pets/myPetsActive";
	}

	@GetMapping(value = "/owners/{ownerId}/pets/disabled")
	public String showMyPetsDisabled(@PathVariable("ownerId") int ownerId, ModelMap model) {
		
		Boolean havePetDisabled = this.petService.countMyPetsAcceptedByActive(ACCEPTED, false, ownerId) !=0;
		
		if (securityAccessPetRequestAndProfile(ownerId) && havePetDisabled) {

			Owner owner = this.ownerService.findOwnerById(ownerId);
			List<Pet> myPets = this.petService.findMyPetsAcceptedByActive(ACCEPTED, false, ownerId);
			model.put(OWNER, owner);
			model.put("pets", myPets);
			return "pets/myPetsDisabled";

		} else {
			return REDIRECT_TO_OUPS;
		}
	}
	
	@GetMapping(value = "/owners/{ownerId}/pets/{petId}/disable")
	public String processDisablePet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId, ModelMap model) throws DataAccessException, DuplicatedPetNameException {
		
		Boolean petIsActive = this.petService.findPetById(petId).isActive();
		Pet updatePet = this.petService.findPetById(petId);
		boolean hasStaysOrAppointments= this.petService.petHasStaysOrAppointmentsActive(petId);
		if (securityAccessPetRequestAndProfile(ownerId) && petIsActive && updatePet.getOwner().getId().equals(ownerId)) {
			if(hasStaysOrAppointments) {
				model.addAttribute("errorDisabled", "You can not disable a pet with appointments or stays active");
				return isAdmin()?this.showMyPetsActiveSuperUser(ownerId, model):this.showMyPetsActive(model);

			} else {
				updatePet.setActive(false);
				this.petService.savePet(updatePet);
				return "redirect:/owners/{ownerId}/pets/disabled";
			}
		} else {
			return REDIRECT_TO_OUPS;
		}
	}
	
	@GetMapping(value = "/owners/{ownerId}/pets/{petId}/enable")
	public String processEnablePet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId, ModelMap model) throws DataAccessException, DuplicatedPetNameException {
		
		Pet updatePet = this.petService.findPetById(petId);
		Boolean petIsActive = !updatePet.isActive();
		if (securityAccessPetRequestAndProfile(ownerId) && petIsActive && updatePet.getOwner().getId().equals(ownerId)) {
			updatePet.setActive(true);
			this.petService.savePet(updatePet);
			return isAdmin()?"redirect:/owner/{ownerId}/pets":"redirect:/owner/pets";
		} else {
			return REDIRECT_TO_OUPS;
		}
	}

	private Boolean isAdmin() {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();
		return authority.equals("admin");
	}

	private boolean securityAccessPetRequestAndProfile(int ownerId) {
		String authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.collect(Collectors.toList()).get(0).toString();
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		Owner owner = new Owner();
		if (authority.equals(OWNER)) {
			owner = this.ownerService.findOwnerById(ownerId);
		}

		return authority.equals("admin")
				|| authority.equals(OWNER) && username.equals(owner.getUser().getUsername());
	}
	
	
	private PetType petType(String type) {
		PetType petType= new PetType();
		if (!this.petTypeService.typeNameDontExists(type.toLowerCase())) {
			PetType p= this.petTypeService.findByName(type.toLowerCase());
			petType = p;
		}else{
			petType.setName(type.toLowerCase());
			try {
				this.petTypeService.addPetType(petType);
			} catch (DuplicatedPetNameException e) {
				PetType p= this.petTypeService.findByName(type.toLowerCase());
				petType = p;
			}
		}
		return petType;
	}
	
	private LocalDate birtdate(String age) {
		LocalDate birthdate= LocalDate.now();
		if (age.equals("Baby")) {
			birthdate=birthdate.minusMonths(2);
		}else if (age.equals("Young")) {
			birthdate=birthdate.minusYears(2);
		}else if (age.equals("Adult")) {
			birthdate=birthdate.minusYears(5);
		}else {
			birthdate=birthdate.minusYears(8);
		}		
		return birthdate;
	}

}
