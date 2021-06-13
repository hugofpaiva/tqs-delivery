package ua.tqs.deliveryservice.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.PersonRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthenticationTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
            .withUsername("demo")
            .withPassword("demopw")
            .withDatabaseName("delivery");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    @Container
    private BrowserWebDriverContainer<?> chromeContainer = new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions());

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    public void beforeEachSetUp() {
        if(System.getProperty("os.name").equals("Mac OS X")){
            this.webApplicationBaseUrl = "host.docker.internal";
        }

        Rider rider = new Rider("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
        personRepository.saveAndFlush(rider);

        Manager manager = new Manager("Joana", bcryptEncoder.encode("difficult-pass"), "joana@email.com");
        personRepository.saveAndFlush(manager);
    }

    public void deleteAll() {
        personRepository.deleteAll();
        personRepository.flush();
    }

    @AfterEach
    public void destroyAll() {
        this.deleteAll();
    }

    @Test
    void testLoginLogoutRider() {
        RemoteWebDriver driver = this.chromeContainer.getWebDriver();
        driver.get("http://" + webApplicationBaseUrl + ":4200/");
        driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")));
        }
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).click();
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).sendKeys("joao@email.com");
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div:nth-child(2) > div > input")).sendKeys("difficult-pass");
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.text-center.form-group > button")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#sidenav-collapse-main > ul > li > a")));
        }
        assertThat(driver.findElement(By.cssSelector("#sidenav-collapse-main > ul > li > a")).getText(), is("User profile"));
        driver.findElement(By.cssSelector("#navbar-main > div > ul > li > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#navbar-main > div > ul > li > div > a:nth-child(4)")));
        }
        driver.findElement(By.cssSelector("#navbar-main > div > ul > li > div > a:nth-child(4)")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }
        assertThat(driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), is("Sign in with credentials"));
    }

    @Test
    void testLoginLogoutManager() {
        RemoteWebDriver driver = this.chromeContainer.getWebDriver();
        driver.get("http://" + webApplicationBaseUrl + ":4200/");
        driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")));
        }
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).click();
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).sendKeys("joana@email.com");
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div:nth-child(2) > div > input")).sendKeys("difficult-pass");
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.text-center.form-group > button")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#sidenav-collapse-main > ul > li > a")));
        }
        assertThat(driver.findElement(By.cssSelector("#sidenav-collapse-main > ul > li > a")).getText(), is("Stores Info"));
        driver.findElement(By.cssSelector("#navbar-main > div > ul > li > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#navbar-main > div > ul > li > div > a")));
        }
        driver.findElement(By.cssSelector("#navbar-main > div > ul > li > div > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }
        assertThat(driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), is("Sign in with credentials"));
    }
}
