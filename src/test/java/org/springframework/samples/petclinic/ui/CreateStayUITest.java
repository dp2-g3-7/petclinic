package org.springframework.samples.petclinic.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateStayUITest {
	
	@LocalServerPort
	private int port;
	
	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@BeforeEach
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	@Order(1)
	public void testCreateNewStay() throws Exception {
		logIn();
		petsAndNewStay();
		driver.findElement(By.id("registerDate")).click();
		driver.findElement(By.id("registerDate")).clear();
		driver.findElement(By.id("registerDate")).sendKeys("2022/10/20");
		driver.findElement(By.id("releaseDate")).click();
		driver.findElement(By.id("releaseDate")).clear();
		driver.findElement(By.id("releaseDate")).sendKeys("2022/10/22");
		driver.findElement(By.xpath("//form[@id='stay']/div[2]/div")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		WebElement stayTable = driver.findElement(By.id("medicinesTable"));
		List<WebElement> staysList = stayTable.findElements(By.id("stay"));
		assertEquals(staysList.size(), 1);
		logOut();
	}
	
	@Test
	@Order(2)
	public void testCreateNewStayError() throws Exception {
		logIn();
		petsAndNewStay();
		driver.findElement(By.id("registerDate")).click();
		driver.findElement(By.id("registerDate")).clear();
		driver.findElement(By.id("registerDate")).sendKeys("2020/10/02");
		driver.findElement(By.id("releaseDate")).click();
		driver.findElement(By.id("releaseDate")).clear();
		driver.findElement(By.id("releaseDate")).sendKeys("2020/10/04");
		driver.findElement(By.xpath("//form[@id='stay']/div[2]/div")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		List<String> spans = driver.findElements(By.tagName("span")).stream().map(s -> s.getText()).collect(Collectors.toList());
		Assert.assertTrue(spans.contains("There exists already a Stay"));	
		logOut();
	}
	
	private void petsAndNewStay() {
		driver.findElement(By.linkText("MY PETS")).click();
		driver.findElement(By.linkText("Stays")).click();
		driver.findElement(By.linkText("New stay")).click();
	}
	
	private void logIn() {
		driver.get("http://localhost:" + port + "/");
		driver.findElement(By.xpath("//a[contains(@href, '/login')]")).click();
		driver.findElement(By.id("username")).click();
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("owner1");
		driver.findElement(By.id("password")).click();
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("0wn3333r_1");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
	}
	
	private void logOut() {
		 driver.findElement(By.linkText("OWNER1")).click();
		 driver.findElement(By.linkText("Logout")).click();
		 driver.findElement(By.xpath("//button[@type='submit']")).click();
	}

	@AfterEach
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
}
