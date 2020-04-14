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

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Vets;
import org.springframework.samples.petclinic.service.VetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public class VetController {

	private final VetService vetService;
	private static final String VIEWS_VET_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	@Autowired
	public VetController(VetService clinicService) {
		this.vetService = clinicService;
	}

	@ModelAttribute("specialties")
	public Collection<Specialty> populateSpecialties() {
		return this.vetService.findSpecialties();
	}

	@InitBinder("vet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new VetValidator());
	}

	@GetMapping(value = { "/vets" })
	public String showVetList(Map<String, Object> model) {
		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet
		// objects
		// so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetService.findVets());
		model.put("vets", vets);
		return "vets/vetList";
	}

	@GetMapping(value = { "/vets.xml" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet
		// objects
		// so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetService.findVets());
		return vets;
	}

	@GetMapping(value = "/vets/new")
	public String initCreationForm(ModelMap model) {
		Vet vet = new Vet();
		model.put("vet", vet);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping(value = "/vets/new")
	public String processCreationForm(@Valid Vet vet, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			model.addAttribute("vet", vet);
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		} else {
			try {
				this.vetService.saveVet(vet);
			} catch (Exception ex) {

				if (ex.getClass().equals(DataIntegrityViolationException.class))
					result.rejectValue("user.username", "duplicate");

				return VIEWS_VET_CREATE_OR_UPDATE_FORM;
			}
			return "redirect:/vets/" + vet.getId();
		}
	}

	@GetMapping(value = "/vets/{vetId}/edit")
	public String initUpdateVetForm(@PathVariable("vetId") int vetId, Model model) {
		Vet vet = this.vetService.findVetById(vetId);
		model.addAttribute("vet", vet);
		model.addAttribute("username", vet.getUser().getUsername());
		model.addAttribute("edit", true);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;

	}

	@PostMapping(value = "/vets/{vetId}/edit")
	public String processUpdateVetForm(@Valid Vet vet, BindingResult result, @PathVariable("vetId") int vetId,
			ModelMap model) {

		model.addAttribute("edit", true);
		Vet vetToUpdate = this.vetService.findVetById(vetId);
		model.addAttribute("username", vetToUpdate.getUser().getUsername());

		if (result.hasErrors()) {
			model.put("vet", vet);
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		} else {
			
			BeanUtils.copyProperties(vet, vetToUpdate, "id", "specialties", "user");
			vet.getSpecialties().stream().filter(s -> !vetToUpdate.getSpecialties().contains(s))
					.forEach(s -> vetToUpdate.addSpecialty(s));
			User user = vetToUpdate.getUser();
			user.setPassword(vet.getUser().getPassword());
			vetToUpdate.setUser(user);
			this.vetService.saveVet(vetToUpdate);

			return "redirect:/vets/{vetId}";
		}
	}

	@GetMapping("/vets/{vetId}")
	public ModelAndView showVet(@PathVariable("vetId") int vetId) {
		ModelAndView mav = new ModelAndView("vets/vetDetails");
		mav.addObject(this.vetService.findVetById(vetId));
		return mav;
	}
}