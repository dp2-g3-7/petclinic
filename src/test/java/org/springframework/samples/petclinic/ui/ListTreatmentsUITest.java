package org.springframework.samples.petclinic.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListTreatmentsUITest {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @LocalServerPort
  private int port;

  @BeforeEach
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testListTreatment() throws Exception {
    driver.get("http://localhost:"+port);
    driver.findElement(By.xpath("//a[contains(@href, '/login')]")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("owner1");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("0wn3333r_1");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.xpath("//a[contains(@href, '/owner/pets')]")).click();
    driver.findElement(By.xpath("//a[contains(text(),'Treatments')]")).click();
    assertEquals("Current Treatments", driver.findElement(By.id("crtT")).getText());
    assertEquals("Expired Treatments", driver.findElement(By.id("expT")).getText());
  }

  @Test
  public void testUntitledTestCase() throws Exception {
	driver.get("http://localhost:"+port);
    driver.findElement(By.xpath("//a[contains(@href, '/login')]")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("owner1");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("0wn3333r_1");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.xpath("//a[contains(@href, '/owner/pets')]")).click();
    driver.findElement(By.xpath("//a[contains(text(),'Treatments')]")).click();
    driver.get("http://localhost:"+port+"/owners/1/pets/4/treatments");
    assertEquals("Something happened...", driver.findElement(By.id("uops")).getText());
  }
  
  @AfterEach
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
