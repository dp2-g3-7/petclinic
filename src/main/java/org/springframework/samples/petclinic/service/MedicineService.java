package org.springframework.samples.petclinic.service;



import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Medicine;
import org.springframework.samples.petclinic.repository.MedicineRepository;
import org.springframework.samples.petclinic.service.exceptions.DuplicatedMedicineCodeException;
import org.springframework.samples.petclinic.service.exceptions.PastMedicineDateException;
import org.springframework.samples.petclinic.service.exceptions.WrongMedicineCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
public class MedicineService {
	
	private MedicineRepository medicineRepository;
	

	
	@Autowired
	public MedicineService(MedicineRepository medicineRepository) {
		this.medicineRepository = medicineRepository;

	}
	
	
	
	@Transactional
	public void saveMedicine(Medicine medicine) throws DataAccessException, DuplicatedMedicineCodeException, PastMedicineDateException, WrongMedicineCodeException {
		String code = medicine.getCode();	
		LocalDate date = medicine.getExpirationDate();
		if (StringUtils.hasLength(code) && this.codeAlreadyExists(code)) {            	
            throw new DuplicatedMedicineCodeException();
        } else if (date != null && LocalDate.now().isAfter(date)) {
        	throw new PastMedicineDateException();	
        } else if (StringUtils.hasLength(code) && !code.matches("^[A-Z]{3}\\-\\d{3,9}$")) {
        	throw new WrongMedicineCodeException();	
        } else {
             this.medicineRepository.save(medicine);  
        }
	}



	@Transactional(readOnly = true)
	public Medicine findMedicineById(int id) throws DataAccessException {
		return medicineRepository.findById(id).get();
	}



	public Iterable<Medicine> findAll() {
		return this.medicineRepository.findAll();
	}



	public Boolean codeAlreadyExists(String code) {
		return this.medicineRepository.codeAlreadyExists(code).size() > 0;
	}

}