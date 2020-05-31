package org.springframework.samples.petclinic.web;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.PetTypeService;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedPetNameException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pet-type")
public class PetTypeController {
	
	private static final String PET_TYPE_NAME_ALREADY_EXISTS = "Pet type name already exists";
	private static final String PET_TYPE_FORM = "pet-type/typeForm";
	private static final String PET_TYPE = "petType";
	private final PetTypeService petTypeService;

	@Autowired
	public PetTypeController(PetTypeService petTypeService) {
		this.petTypeService = petTypeService;
	}
	
	@InitBinder("petType")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetTypeValidator());
	}
	
	@GetMapping()
	public String listTypes(ModelMap modelMap) {
		Iterable<PetType> pt = this.petTypeService.findAll();
		modelMap.addAttribute("petTypes", pt);
		return "pet-type/typeList";
	}
	
	@GetMapping("/new")
	public String addType(ModelMap modelMap) {
		modelMap.addAttribute(PET_TYPE, new PetType());
		return PET_TYPE_FORM;
	}
	
	@PostMapping("/new")
	public String savePetType(@Valid PetType petType, BindingResult result, ModelMap modelMap) {
		if(result.hasErrors()) {
			return PET_TYPE_FORM;
		} else {
			try {
			this.petTypeService.addPetType(petType);
			} catch (DuplicatedPetNameException e) {
				result.rejectValue("name", PET_TYPE_NAME_ALREADY_EXISTS, PET_TYPE_NAME_ALREADY_EXISTS);
				return PET_TYPE_FORM;
			}
			return "redirect:/pet-type";
		}
	}
	
	@GetMapping(value = "/{petTypeId}/edit")
	public String initUpdatePetTypeForm(@PathVariable("petTypeId") final int petTypeId, final ModelMap model) {
		
			PetType petType = petTypeService.findById(petTypeId);
			model.addAttribute(PET_TYPE, petType);
			model.addAttribute("edit", true);
			return PET_TYPE_FORM;
		
	}

	@PostMapping(value = "/{petTypeId}/edit")
	public String processUpdatePetTypeForm(@Valid PetType petType, BindingResult result, @PathVariable("petTypeId") int petTypeId,
			ModelMap model) throws DuplicatedPetNameException {

			model.addAttribute("edit", true);
			PetType petTypeToUpdate = this.petTypeService.findById(petTypeId);

			if (result.hasErrors()) {
				model.put(PET_TYPE, petType);
				return PET_TYPE_FORM;
			} else {
				BeanUtils.copyProperties(petType, petTypeToUpdate, "id");
				petTypeToUpdate.setName(petType.getName());
			
				try {
					this.petTypeService.addPetType(petTypeToUpdate);
					} catch (DuplicatedPetNameException e) {
						result.rejectValue("name", PET_TYPE_NAME_ALREADY_EXISTS, PET_TYPE_NAME_ALREADY_EXISTS);
						return PET_TYPE_FORM;
			}
	
	}
			return "redirect:/pet-type";

}
}
