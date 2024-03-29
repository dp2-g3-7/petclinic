package org.springframework.samples.petclinic.web.e2e;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.web.PetController;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
  webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
/*@TestPropertySource(
  locations = "classpath:application-mysql.properties")*/
class MedicineControllerE2ETests {

	private static final int TEST_MED_ID = 1;
	
	@Autowired
	private MockMvc mockMvc;
	
	@WithMockUser(username="admin1",authorities= {"admin"})
	    @Test
	void testList() throws Exception {
		mockMvc.perform(get("/medicines")).andExpect(status().isOk())
				.andExpect(view().name("medicines/medicinesList")).andExpect(model().attributeExists("medicines"));
	}

	@WithMockUser(username="admin1",authorities= {"admin"})
        @Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/medicines/new")).andExpect(status().isOk())
				.andExpect(view().name("medicines/createOrUpdateMedicineForm")).andExpect(model().attributeExists("medicine"));
	}

	@WithMockUser(username="admin1",authorities= {"admin"})
        @Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/medicines/new")
							.with(csrf())
							.param("name", "Virbaninte")
							.param("code", "VET-123")
							.param("expirationDate", "2022/03/12")
							.param("description", "Desparasitante"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/medicines"));
	}

	@WithMockUser(username="admin1",authorities= {"admin"})
    @ParameterizedTest
    @CsvSource({
    	"Virbaninte,123-123,2022/03/12,desparasitante",
    	"Virbaninte,ATN-674,2022/03/12,desparasitante",
    	"Virbaninte,GTN-999,'',desparasitante",
    	"Virbaninte,HUB-232,2022/03/12,''"
    })
	void testProcessCreationFormHasErrors(String name, String code, String expirationDate, String description) throws Exception {
		mockMvc.perform(post("/medicines/new")
							.with(csrf())
							.param("name", name)
							.param("code", code)
							.param("expirationDate", expirationDate)
							.param("description", description))
				.andExpect(model().attributeHasErrors("medicine"))
				.andExpect(status().isOk())
				.andExpect(view().name("medicines/createOrUpdateMedicineForm"));
	}

	@WithMockUser(username="admin1",authorities= {"admin"})
	    @Test
	void testShow() throws Exception {
		mockMvc.perform(get("/medicines/{medicineId}", TEST_MED_ID)).andExpect(status().isOk())
		.andExpect(model().attribute("medicine", hasProperty("name", is("Penicillin"))))
		.andExpect(model().attribute("medicine", hasProperty("code", is("PEN-2356"))))
		.andExpect(model().attribute("medicine", hasProperty("expirationDate", is(LocalDate.of(2021, 7, 4)))))
		.andExpect(view().name("medicines/medicineDetails"));
	}
	
	@WithMockUser(username="admin1",authorities= {"admin"})
    @Test
	void testInitEditMedicineForm() throws Exception {
		mockMvc.perform(get("/medicines/{medicineId}/edit", TEST_MED_ID))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("medicine"))
				.andExpect(view().name("medicines/createOrUpdateMedicineForm"));
	}	
	
	@WithMockUser(username="admin1",authorities= {"admin"})
    @Test
	void testProcessEditMedicineFormSuccess() throws Exception {
		mockMvc.perform(post("/medicines/{medicineId}/edit", TEST_MED_ID)
							.with(csrf())
							.param("name", "Name test")    
	                        .param("code", "TET-111")
							.param("expirationDate", "2022/03/12")
							.param("description", "Testing controller"))
	            .andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/medicines"));
	}
		
	@WithMockUser(username="admin1",authorities= {"admin"})
	@ParameterizedTest
	@CsvSource({
		"1,'',TET-111,2022/03/12,desparasitante",
    	"1,Virbaninte,123-123,2022/03/12,desparasitante",
    	"1,Virbaninte,GTN-999,'',desparasitante",
    	"1,Virbaninte,HUB-232,2022/03/12,''"
	})
	void testProcessEditMedicineFormHasErrors(int medicineId, String name, String code, String expirationDate, String description) throws Exception {
		mockMvc.perform(post("/medicines/{medicineId}/edit", medicineId)
							.with(csrf())
							.param("name", name)    
	                        .param("code", code)
							.param("expirationDate", expirationDate)
							.param("description", description))
				.andExpect(model().attributeHasErrors("medicine"))
				.andExpect(status().isOk())
				.andExpect(view().name("medicines/createOrUpdateMedicineForm"));
	}	
}
