package org.springframework.samples.petclinic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

import lombok.Data;

@Data
@Entity
@Table(name = "treatments")
public class Treatment extends NamedEntity{
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "treatment_medicines", joinColumns = @JoinColumn(name = "treatment_id"), inverseJoinColumns = @JoinColumn(name = "medicine_id"))
	private Set<Medicine> medicines;
	
	protected Set<Medicine> getMedicinesInternal() {
		if (this.medicines == null) {
			this.medicines = new HashSet<>();
		}
		return this.medicines;
	}
	
	protected void setMedicinesInternal(Set<Medicine> medicines) {
		this.medicines = medicines;
	}
	
	public List<Medicine> getMedicines() {
		List<Medicine> sortedMeds = new ArrayList<>(getMedicinesInternal());
		PropertyComparator.sort(sortedMeds, new MutableSortDefinition("name", true, true));
		return Collections.unmodifiableList(sortedMeds);
	}
	
	@NotEmpty
	@Column(name = "description")
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "pet_id")
	private Pet pet;

}
