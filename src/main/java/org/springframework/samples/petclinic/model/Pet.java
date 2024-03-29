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
package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.format.annotation.DateTimeFormat;
/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

	@Column(name = "birth_date")        
	@DateTimeFormat(pattern = "yyyy/MM/dd")
	private LocalDate birthDate;
	
	@Column(name = "status")
	private PetRegistrationStatus status;
	
	@Column(name = "justification")
	private String justification;
	
	@Column(name = "active")
	private boolean active;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private PetType type;

	@ManyToOne
	@JoinColumn(name = "owner_id")
	private Owner owner;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "pet")
	private Set<Visit> visits;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "pet")
	private Set<Appointment> appointments;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "pet")
	private Set<Stay> stays;

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public PetRegistrationStatus getStatus() {
		return status;
	}

	public void setStatus(PetRegistrationStatus status) {
		this.status = status;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public PetType getType() {
		return this.type;
	}

	public void setType(PetType type) {
		this.type = type;
	}

	public Owner getOwner() {
		return this.owner;
	}

	protected void setOwner(Owner owner) {
		this.owner = owner;
	}

	protected Set<Visit> getVisitsInternal() {
		if (this.visits == null) {
			this.visits = new HashSet<>();
		}
		return this.visits;
	}

	protected void setVisitsInternal(Set<Visit> visits) {
		this.visits = visits;
	}

	public List<Visit> getVisits() {
		List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
		PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
		return Collections.unmodifiableList(sortedVisits);
	}

	public void addVisit(Visit visit) {
		getVisitsInternal().add(visit);
		visit.setPet(this);
	}

	protected Set<Appointment> getAppointmentsInternal() {
		if (this.appointments == null) {
			this.appointments = new HashSet<Appointment>();
		}
		return this.appointments;
	}
	
	protected void setAppointmentsInternal(Set<Appointment> appointments) {
		this.appointments = appointments;
	}
	
	public List<Appointment> getAppointments() {
		List<Appointment> sortedAppointments = new ArrayList<>(getAppointmentsInternal());
		sortedAppointments = sortedAppointments.stream().filter(x->!x.getAppointmentDate().isBefore(LocalDate.now())).collect(Collectors.toList());
		PropertyComparator.sort(sortedAppointments, new MutableSortDefinition("appointmentDate", false, false)); //appointmentDate 
		return Collections.unmodifiableList(sortedAppointments);
	}
	
	public void deleteAppointment(Appointment appointment) {
		getAppointmentsInternal().remove(appointment);
		
	}

	public void addStay(Stay stay) {
		getStaysInternal().add(stay);
		stay.setPet(this);
	}

	private Set<Stay> getStaysInternal() {
		if (this.stays == null) {
			this.stays = new HashSet<>();
		}
		return this.stays;
	}

	public List<Stay> getStays() {
		List<Stay> sortedStances = new ArrayList<>(getStaysInternal());
		PropertyComparator.sort(sortedStances, new MutableSortDefinition("registerDate", false, false));
		return Collections.unmodifiableList(sortedStances);
	}
	
	public void deleteStay(Stay stay) {
		this.stays.remove(stay);
	}

}
