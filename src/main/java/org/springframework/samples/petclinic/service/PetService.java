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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetRegistrationStatus;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Status;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedPetNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all Petclinic controllers Also a placeholder
 * for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 */
@Service
public class PetService {

	private PetRepository petRepository;
	
	@Autowired
	public PetService(PetRepository petRepository) {
		this.petRepository = petRepository;
	}

	@Transactional(readOnly = true)
	@Cacheable("petTypes")
	public Collection<PetType> findPetTypes() throws DataAccessException {
		return petRepository.findPetTypes();
	}
	
	@Transactional(readOnly = true)
	@Cacheable("petById")
	public Pet findPetById(Integer petId) throws DataAccessException {
		return petRepository.findPetByIdWithVisitsAppoimentsStays(petId);
	}
	
	@Transactional(rollbackFor = DuplicatedPetNameException.class)
	@CacheEvict(cacheNames = {"allPets", "petById"}, allEntries = true)
	public void savePet(Pet pet) throws DataAccessException, DuplicatedPetNameException {	
		if (existOtherPetWithSameName(pet)) {
			throw new DuplicatedPetNameException();
		} else
			petRepository.save(pet);
	}
	public Boolean existOtherPetWithSameName(Pet newPet) {
		Boolean res= false;
		String petName = newPet.getName().toLowerCase();
		List<Pet> ownerPets= this.petRepository.findAllPetsByOwnerId(newPet.getOwner().getId());
		for (Pet pet : ownerPets) {
			String compName = pet.getName();
			compName = compName.toLowerCase();
			if (compName.equals(petName) && pet.getId()!=newPet.getId()) {
				res= true;
			}
		}
		return res;
	}

	@Cacheable("allPets")
	@Transactional(readOnly = true)
	public List<Pet> findAll() {
		return this.petRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Pet> findPetsRequests(PetRegistrationStatus pending) {
		return this.petRepository.findPetsRequests(pending);
	}

	@Transactional(readOnly = true)
	public List<Pet> findMyPetsRequests(PetRegistrationStatus pending, PetRegistrationStatus rejected, Integer ownerId) {
		return this.petRepository.findMyPetsRequests(pending, rejected, ownerId);
	}

	@Transactional(readOnly = true)
	public List<Pet> findMyPetsAcceptedByActive(PetRegistrationStatus accepted, boolean active, Integer ownerId) {
		return this.petRepository.findMyPetsAcceptedByActive(accepted, active, ownerId);
	}

	@Transactional(readOnly = true)
	public Integer countMyPetsAcceptedByActive(PetRegistrationStatus accepted, boolean active, int ownerId) {
		return this.petRepository.countMyPetsAcceptedByActive(accepted, active, ownerId);
	}
	
	@Transactional(readOnly = true)
	public Boolean petHasStaysOrAppointmentsActive(int petId) {
		return this.petRepository.countMyPetActiveStays(petId,LocalDate.now(),Status.ACCEPTED)>0 || this.petRepository.countMyPetActiveAppointments(petId,LocalDate.now())>0; 
	}


}
