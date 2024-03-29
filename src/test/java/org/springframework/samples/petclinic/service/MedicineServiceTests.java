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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.model.Medicine;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedMedicineCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided
 * by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we
 * don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code>{@link
 * ClinicServiceTests#clinicService clinicService}</code> instance variable, which uses
 * autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in
 * its own transaction, which is automatically rolled back by default. Thus, even if tests
 * insert or otherwise change database state, there is no need for a teardown or cleanup
 * script.
 * <li>An {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.</li>
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
class MedicineServiceTests {
    
	@Autowired
	protected MedicineService medicineService;

	@Test
	void shouldFindMedicineWithCorrectId() {
		Medicine med2 = this.medicineService.findMedicineById(2);
		assertThat(med2.getName()).contains("Pet-Dalsy");
	}

	@Test
	void shouldNotFindMedicineWithCorrectId() {
		Medicine medicine = this.medicineService.findMedicineById(12);
		assertThat(medicine).isNull();
	}

    @ParameterizedTest
    @CsvSource({
        "Virbaninte,Desparasitante,MED-123",
        "Diclofenaco,Calmante para caballos,DIC-6156",
        "Vacuna RB,Vacuna contra la rabia, RAB-000-E1"
    })
    @Transactional
	public void shouldInsertMedicineIntoDatabaseAndGenerateId(String name, String description, String code) {
		Collection<Medicine> medicines = (Collection<Medicine>) this.medicineService.findAll();
		int found = medicines.size();

		Medicine med = new Medicine();
		med.setName(name);
		med.setExpirationDate(LocalDate.now().plusYears(2));
		med.setDescription(description);
		med.setCode(code);
            try {
				this.medicineService.saveMedicine(med);
			} catch (DuplicatedMedicineCodeException ex) {
				 Logger.getLogger(MedicineServiceTests.class.getName()).log(Level.SEVERE, null, ex);
			}
        Collection<Medicine> medicines2 = (Collection<Medicine>) this.medicineService.findAll();
		assertThat(medicines2.size()).isEqualTo(found + 1);
		// checks that id has been generated
		assertThat(med.getId()).isNotNull();
	}

	@ParameterizedTest
    @CsvSource({
        "'',Desparasitante,MED-123,2021/01/13",
        "Diclofenaco,'',DIC-666,2020/05/11",
        "Vacuna RB,Vacuna contra la rabia,'',2020/06/15",
        "Virbaninte,Desparasitante,BAY-123,"
    })
	@Transactional
	public void shouldNotInsertMedicine(String name, String description, String code, String expirationDate) {
		Medicine med = new Medicine();
		med.setName(name);
		med.setDescription(description);
		med.setCode(code);
		if (expirationDate != null ) {
			med.setExpirationDate(LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("yyyy/MM/dd")));			
		} else {
			med.setExpirationDate(null);
		}
		assertThrows(ConstraintViolationException.class, () -> {
			this.medicineService.saveMedicine(med);
		});
	}

	@Test
	public void shouldNotInsertMedicineUsedCode() {
		String usedCode = this.medicineService.findMedicineById(1).getCode();
		Medicine med = new Medicine();
		med.setName("Virbaninte");
		med.setExpirationDate(LocalDate.now().plusYears(2));
		med.setDescription("Desparasitante");
		med.setCode(usedCode);

		assertThrows(DuplicatedMedicineCodeException.class, () -> {
			this.medicineService.saveMedicine(med);
		});
	}

	@Test
	void shouldFindAllMedicines() {
		Collection<Medicine> medicines = (Collection<Medicine>) this.medicineService.findAll();
		int found = medicines.size();
		assertThat(found).isEqualTo(6);
	}

	@Test
	void shouldFindUsedCode() {
		Medicine medicine = this.medicineService.findMedicineById(1);
		String code = medicine.getCode();
		assertThat(this.medicineService.codeAlreadyExists(code)).isTrue();
	}

	@Test
	void shouldEditMedicine() {
		Medicine med = this.medicineService.findMedicineById(1);
		Medicine m = new Medicine();
		BeanUtils.copyProperties(med, m);
		try {
			m.setDescription("New description");
			this.medicineService.editMedicine(m);
		} catch (DuplicatedMedicineCodeException e) {
			e.printStackTrace();

		}
		assertThat(this.medicineService.findMedicineById(1).getDescription()).isEqualTo("New description");
	}

	@Test
	void shouldNotEditMedicine() {
		Medicine med = this.medicineService.findMedicineById(1);
		Medicine m = new Medicine();
		BeanUtils.copyProperties(med, m);
		String repeatedCode = this.medicineService.findMedicineById(2).getCode();
		m.setCode(repeatedCode);
		assertThrows(DuplicatedMedicineCodeException.class,	() -> {
			this.medicineService.editMedicine(m);
		});
	}

}
