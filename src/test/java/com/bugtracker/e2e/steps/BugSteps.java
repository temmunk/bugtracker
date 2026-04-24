package com.bugtracker.e2e.steps;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Role;
import com.bugtracker.model.User;
import com.bugtracker.repository.BugRepository;
import com.bugtracker.repository.UserRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class BugSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        if (!userRepository.existsByUsername("testuser")) {
            User user = new User("testuser", passwordEncoder.encode("password123"), "test@test.com", Role.USER);
            userRepository.save(user);
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        bugRepository.deleteAll();
    }

    @Given("I am on the BugTracker home page")
    public void iAmOnTheHomePage() {
        driver.get("http://localhost:" + port);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm")));

        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.cssSelector("#loginForm button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bugTableBody")));
    }

    @Given("a bug with title {string} exists")
    public void aBugWithTitleExists(String title) {
        Bug bug = new Bug(title, "Auto-generated for testing",
                BugPriority.MEDIUM, BugStatus.OPEN, "tester", null);
        bugRepository.save(bug);
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonText) {
        if (buttonText.equals("New Bug")) {
            driver.findElement(By.id("newBugBtn")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bugForm")));
        }
    }

    @And("I fill in the bug form with title {string} and priority {string}")
    public void iFillInTheBugForm(String title, String priority) {
        driver.findElement(By.id("title")).sendKeys(title);
        new Select(driver.findElement(By.id("priority"))).selectByValue(priority);
    }

    @And("I submit the bug form")
    public void iSubmitTheBugForm() {
        driver.findElement(By.cssSelector("#bugFormElement button[type='submit']")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("bugForm")));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    @Then("I should see {string} in the bug list")
    public void iShouldSeeInTheBugList(String title) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bugTableBody")));
        String pageSource = driver.findElement(By.id("bugTableBody")).getText();
        assertTrue(pageSource.contains(title),
                "Expected to find '" + title + "' in bug list but did not.");
    }

    @Then("I should not see {string} in the bug list")
    public void iShouldNotSeeInTheBugList(String title) {
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        String pageSource = driver.findElement(By.id("bugTableBody")).getText();
        assertFalse(pageSource.contains(title),
                "Expected NOT to find '" + title + "' in bug list but it was present.");
    }

    @When("I click the edit button for {string}")
    public void iClickTheEditButtonFor(String title) {
        WebElement row = findRowByTitle(title);
        assertNotNull(row, "Could not find row with title: " + title);
        row.findElement(By.cssSelector(".btn-edit")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bugForm")));
    }

    @And("I change the title to {string}")
    public void iChangeTheTitleTo(String newTitle) {
        WebElement titleField = driver.findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys(newTitle);
    }

    @When("I click the delete button for {string}")
    public void iClickTheDeleteButtonFor(String title) {
        WebElement row = findRowByTitle(title);
        assertNotNull(row, "Could not find row with title: " + title);
        row.findElement(By.cssSelector(".btn-danger")).click();
    }

    @And("I confirm the deletion")
    public void iConfirmTheDeletion() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    private WebElement findRowByTitle(String title) {
        for (WebElement row : driver.findElements(By.cssSelector("#bugTableBody tr"))) {
            if (row.getText().contains(title)) {
                return row;
            }
        }
        return null;
    }
}
