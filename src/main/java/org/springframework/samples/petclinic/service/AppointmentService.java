
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Appointment;
import org.springframework.samples.petclinic.repository.AppointmentRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Transactional
	public void saveAppointment(final Appointment appointment) throws DataAccessException {
		/*LocalDate localDateNow = LocalDate.now();
		if (appointment.getAppointmentDate().isAfter(localDateNow)) {
			this.appointmentRepository.save(appointment);
		}*/
		this.appointmentRepository.save(appointment);
	}
}
