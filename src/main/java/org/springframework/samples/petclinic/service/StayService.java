package org.springframework.samples.petclinic.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Status;
import org.springframework.samples.petclinic.model.Stay;
import org.springframework.samples.petclinic.repository.StayRepository;
import org.springframework.samples.petclinic.service.exceptions.DateNotAllowed;
import org.springframework.samples.petclinic.service.exceptions.MaximumStaysReached;
import org.springframework.samples.petclinic.service.exceptions.StayAlreadyConfirmed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StayService {

	private StayRepository stayRepository;
	
	
	@Autowired
	public StayService(StayRepository stayRepository) {
		this.stayRepository = stayRepository;
	}
	
	@Transactional
	public void saveStay(Stay stay) throws MaximumStaysReached {
		if (this.dayExists(stay, -1)) {
			throw new MaximumStaysReached();
		} else {
			stay.setStatus(Status.PENDING);
			stayRepository.save(stay);
		}

	}
	
	public boolean dayExists(Stay stay, int stayId) {
		return this.stayRepository.numOfStaysThatDates(stay.getRegisterDate(), stay.getReleaseDate(), stay.getPet().getId(), stayId) > 0;
	}

	@Transactional(readOnly = true)
	public Stay findStayById(int stayId) {
		return stayRepository.findById(stayId).orElse(null);
	}

	@Transactional
	public void deleteStay(Stay stay) throws StayAlreadyConfirmed {
		if (stay.getStatus() != Status.PENDING) {
			throw new StayAlreadyConfirmed();
		} else {
			stayRepository.delete(stay);
		}
	}

	public Collection<Stay> findStaysByPetId(int petId) {
		return stayRepository.findByPetId(petId);
	}

	@Transactional
	public void editStay(final Stay stay) throws MaximumStaysReached, DateNotAllowed, StayAlreadyConfirmed {
		Optional<Stay> stayOP = this.stayRepository.findById(stay.getId());
		Stay stayToUpdate = new Stay();
		if(stayOP.isPresent()) {
			stayToUpdate = stayOP.get();
		}
		if ((stayToUpdate.getRegisterDate().equals(stay.getRegisterDate())
				&& stayToUpdate.getReleaseDate().equals(stay.getReleaseDate()))) {
			throw new DateNotAllowed();
		} else if (this.dayExists(stay, stay.getId())) {
			throw new MaximumStaysReached();
		} else if (!stayToUpdate.getStatus().equals(Status.PENDING)) {
			throw new StayAlreadyConfirmed();
		}

		else {
			this.stayRepository.save(stay);
		}
	}
	
	public List<Stay> findAllStays() {
		return (List<Stay>) this.stayRepository.findAll();
	}
	
	@Transactional
	public void editStatus(final Stay stay) throws StayAlreadyConfirmed {
		Stay stayToUpdate = this.findStayById(stay.getId());
		if (!stayToUpdate.getStatus().equals(Status.PENDING)) {
			throw new StayAlreadyConfirmed();
		} else {
			this.stayRepository.save(stay);
		}
	}

	
}
