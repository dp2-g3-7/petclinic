package org.springframework.samples.petclinic.bdd.stepdefinitions;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.xmlunit.builder.Input;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

public class AddVisitStepDefinitions extends AbstractStep{

	@Then("Error message appears")
    public void canNotAddVisit() {
		List<String> spans = getDriver().findElements(By.tagName("span")).stream().map(s -> s.getText()).collect(Collectors.toList());
		Assert.assertTrue(spans.contains("no puede estar vacío"));
	}
	
	@And("I try to add a visit with errors")
	public void tryAddVisitWithErrors() {
		getDriver().findElement(By.linkText("APPOINTMENTS")).click();
	    getDriver().findElement(By.xpath("(//a[contains(text(),'Add visit')])[1]")).click();
		getDriver().findElement(By.id("medicalTests1")).click();
		getDriver().findElement(By.id("medicalTests2")).click();
	    getDriver().findElement(By.xpath("//button[@type='submit']")).click();
	}
	
	@Then("A registered message appears")
    public void canAddVisit() {
		assertEquals("Already registered", getDriver().findElement(By.xpath("//table[@id='AppointmentsTodayTable']/tbody/tr/td[5]")).getText());
	}
	
	@And("I add a visit")
	public void addVisit() {
		getDriver().findElement(By.linkText("APPOINTMENTS")).click();
		getDriver().findElement(By.xpath("(//a[contains(text(),'Add visit')])[2]")).click();
		getDriver().findElement(By.id("description")).click();
		getDriver().findElement(By.id("description")).clear();
		getDriver().findElement(By.id("description")).sendKeys("Clinical examination of the pet");
		getDriver().findElement(By.id("medicalTests1")).click();
		getDriver().findElement(By.id("medicalTests2")).click();
	    getDriver().findElement(By.xpath("//button[@type='submit']")).click();
	}
}
