package org.springframework.samples.petclinic.bdd.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.By;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.java.Log;

@Log
public class DeleteBannerStepDefinitions extends AbstractStep{
	
	int nBanners;

	@Then("A message with the error appears")
    public void canNotDeleteBanner() {
	    assertEquals("cannot be deleted if the collaboration end date has not expired", getDriver().findElement(By.className("error-text")).getText().trim());
	  }
	
	@And("I list the banners")
	public void tryDeleteBanner() {
		getDriver().findElement(By.xpath("//div[@id='main-navbar']/ul[2]/li/a")).click();
		getDriver().findElement(By.xpath("//a[contains(text(),'Banners')]")).click();
	    nBanners = getNumberOfBanners();
	    getDriver().findElement(By.xpath("//a[contains(@href, '/banners/1/delete')]")).click();
	}
	
	@Then("The banner is deleted")
    public void canDeleteAppointment() {
		assertEquals(getNumberOfBanners(), nBanners - 1);
	}
	
	@And("I list all the banners")
	public void tryDeleteAppointment() {
		getDriver().findElement(By.xpath("//div[@id='main-navbar']/ul[2]/li/a")).click();
		getDriver().findElement(By.xpath("//a[contains(text(),'Banners')]")).click();
	    nBanners = getNumberOfBanners();
	    getDriver().findElement(By.xpath("//a[contains(@href, '/banners/4/delete')]")).click();
	}
	
	private int getNumberOfBanners() {
		  return getDriver().findElements(By.xpath("//table[@id='bannersTable']/tbody/tr")).size()-1;
	  }
	
}
