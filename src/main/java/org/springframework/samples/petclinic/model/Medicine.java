package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Entity
@Data
@Table(name = "medicines")
public class Medicine extends NamedEntity  {
	
	@Column(name = "expiration_date")    
	@DateTimeFormat(pattern = "yyyy/MM/dd")
	private LocalDate expirationDate;
	
	@NotEmpty
	@Column(name = "description")
	private String description;

	@NotEmpty
	@Pattern(regexp = "^[A-Z]{3}\\-\\d{3,9}$")
	@Column(name = "code")
	private String code;
	
	
	

}
