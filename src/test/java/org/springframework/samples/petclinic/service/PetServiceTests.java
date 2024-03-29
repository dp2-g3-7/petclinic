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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetRegistrationStatus;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedPetNameException;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following
 * services provided by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary
 * set up time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning
 * that we don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code>{@link
 * ClinicServiceTests#clinicService clinicService}</code> instance variable,
 * which uses autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is
 * executed in its own transaction, which is automatically rolled back by
 * default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script.
 * <li>An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean
 * lookup if necessary.</li>
 * </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PetServiceTests {

	@Autowired
	protected PetService petService;

	@Autowired
	protected OwnerService ownerService;

	private static final PetRegistrationStatus accepted = PetRegistrationStatus.ACCEPTED;

	private static final PetRegistrationStatus pending = PetRegistrationStatus.PENDING;

	private static final PetRegistrationStatus rejected = PetRegistrationStatus.REJECTED;

	@Test
	void shouldFindPetWithCorrectId() {
		Pet pet7 = this.petService.findPetById(7);
		assertThat(pet7.getName()).startsWith("Samantha");
		assertThat(pet7.getOwner().getFirstName()).isEqualTo("Eduardo");
	}

	@Test
	void shouldFindAllPetTypes() {
		Collection<PetType> petTypes = this.petService.findPetTypes();

		PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
		assertThat(petType1.getName()).isEqualTo("cat");
		PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
		assertThat(petType4.getName()).isEqualTo("snake");
	}

	@Test
	@Transactional
	public void shouldInsertPetIntoDatabaseAndGenerateId() {
		Owner owner6 = this.ownerService.findOwnerById(6);
		int found = owner6.getPets().size();

		Pet pet = new Pet();
		pet.setName("bowser");
		Collection<PetType> types = this.petService.findPetTypes();
		pet.setType(EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(LocalDate.now());

		try {
			this.petService.savePet(pet, owner6);
		} catch (DuplicatedPetNameException ex) {
			Logger.getLogger(PetServiceTests.class.getName()).log(Level.SEVERE, null, ex);
		}

		owner6 = this.ownerService.findOwnerById(6);
		assertThat(owner6.getPets().size()).isEqualTo(found + 1);
		// checks that id has been generated
		assertThat(pet.getId()).isNotNull();
	}

	@Test
	@Transactional
	public void shouldThrowExceptionInsertingPetsWithTheSameName() {
		Owner owner6 = this.ownerService.findOwnerById(6);
		Pet pet = new Pet();
		pet.setName("wario");
		Collection<PetType> types = this.petService.findPetTypes();
		pet.setType(EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(LocalDate.now());
		owner6.addPet(pet);
		try {
			petService.savePet(pet, owner6);
		} catch (DuplicatedPetNameException e) {
			// The pet already exists!
			e.printStackTrace();
		}

		Pet anotherPetWithTheSameName = new Pet();
		anotherPetWithTheSameName.setName("wario");
		anotherPetWithTheSameName.setType(EntityUtils.getById(types, PetType.class, 1));
		anotherPetWithTheSameName.setBirthDate(LocalDate.now().minusWeeks(2));
		Assertions.assertThrows(DuplicatedPetNameException.class, () -> {
			owner6.addPet(anotherPetWithTheSameName);
			petService.savePet(anotherPetWithTheSameName, owner6);
		});
	}

	@Test
	@Transactional
	public void shouldUpdatePetName() throws Exception {
		Pet pet7 = this.petService.findPetById(7);
		String oldName = pet7.getName();

		String newName = oldName + "X";
		pet7.setName(newName);
		this.petService.EditPet(pet7);

		pet7 = this.petService.findPetById(7);
		assertThat(pet7.getName()).isEqualTo(newName);
	}

	@Test
	@Transactional
	public void shouldThrowExceptionUpdatingPetsWithTheSameName() {
		Owner owner6 = this.ownerService.findOwnerById(6);
		Pet pet = new Pet();
		pet.setName("donatelo");
		Collection<PetType> types = this.petService.findPetTypes();
		pet.setType(EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(LocalDate.now());
		owner6.addPet(pet);

		Pet anotherPet = new Pet();
		anotherPet.setName("waluigi");
		anotherPet.setType(EntityUtils.getById(types, PetType.class, 1));
		anotherPet.setBirthDate(LocalDate.now().minusWeeks(2));
		owner6.addPet(anotherPet);

		try {
			petService.EditPet(pet);
			petService.EditPet(anotherPet);
		} catch (DuplicatedPetNameException e) {
			// The pets already exists!
			e.printStackTrace();
		}

		Assertions.assertThrows(DuplicatedPetNameException.class, () -> {
			anotherPet.setName("donatelo");
			petService.EditPet(anotherPet);
		});
	}
	

	@Test
	void shouldFindPetsRequests() {
		List<Pet> petsRequests = this.petService.findPetsRequests(pending);

		assertThat(petsRequests.get(0).getStatus()).isEqualTo(pending);
		assertThat(petsRequests.get(0).getJustification()).isEqualTo("");
		assertThat(petsRequests.get(0).isActive()).isEqualTo(true);
	}

	@Test
	void shouldFindMyPetsRequests() {
		List<Pet> myPetsRequests= this.petService.findMyPetsRequests(pending, rejected, 3);
		
		assertThat(myPetsRequests.get(0).getStatus()).isEqualTo(pending);
		assertThat(myPetsRequests.get(0).getJustification()).isEqualTo("");
		assertThat(myPetsRequests.get(0).isActive()).isEqualTo(true);
		
		assertThat(myPetsRequests.get(1).getStatus()).isEqualTo(rejected);
		assertThat(myPetsRequests.get(1).getJustification()).isEqualTo("It is impossible to accept it because the lizard quota has been exceeded");
		assertThat(myPetsRequests.get(1).isActive()).isEqualTo(true);
		
	}
	
	@Test
	void shouldFindMyPetsAcceptedByActiveTrue() {
		List<Pet> myPetsAcceptedByActive = this.petService.findMyPetsAcceptedByActive(accepted, true, 3);
	
		assertThat(myPetsAcceptedByActive.get(0).getStatus()).isEqualTo(accepted);
		assertThat(myPetsAcceptedByActive.get(0).isActive()).isEqualTo(true);
	}
	
	@Test 
	void shouldFindMyPetsAcceptedByActiveFalse(){
		List<Pet> myPetsAcceptedAndDisabled= this.petService.findMyPetsAcceptedByActive(accepted, false, 3);

		assertThat(myPetsAcceptedAndDisabled.get(0).getStatus()).isEqualTo(accepted);
		assertThat(myPetsAcceptedAndDisabled.get(0).isActive()).isEqualTo(false);
		
	}
	
	@Test
	void shouldCountMyPetsAcceptedByActive() {
		Integer myPetsAcceptedAndDisabled= this.petService.countMyPetsAcceptedByActive(accepted, false, 3);
		
		assertThat(myPetsAcceptedAndDisabled).isEqualTo(1);
	}
    
	@ParameterizedTest
	@ValueSource(ints={1,14,21})
	void testStaysOrAppointmentActive(int petId) {
		boolean res = this.petService.petHasStaysOrAppointmentsActive(petId);
		assertThat(res).isTrue();
	}
	
	@Test
	void testNotStaysOrAppointmentsActive() {
		boolean res = this.petService.petHasStaysOrAppointmentsActive(7);
		assertThat(res).isFalse();
	}
}
