package ua.tqs.humberpecas.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.selenium.pages.LoginPage;
import ua.tqs.humberpecas.selenium.pages.RegisterPage;
import ua.tqs.humberpecas.selenium.pages.ShopPage;

// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ShopTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    private RemoteWebDriver driver;

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

        if (personRepository.findByEmail("joao@email.com").isEmpty()) {
            this.driver = this.chromeContainer.getWebDriver();

            Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
            personRepository.saveAndFlush(person);

            LoginPage loginPage = new LoginPage(this.driver, this.webApplicationBaseUrl);
            loginPage.login("joao@email.com", "difficult-pass");
        }
    }

    @Test
    void testLoginLogoutClient() {
        ShopPage shopPage = new ShopPage(this.driver);
    }

    @Test
    void testRegisterLoginLogoutClient() {
        RemoteWebDriver driver = this.chromeContainer.getWebDriver();

        RegisterPage registerPage = new RegisterPage(driver, this.webApplicationBaseUrl);
        registerPage.register("TesteEmail@email.com", "teste123", "TesteName");

        LoginPage loginPage = new LoginPage(driver, this.webApplicationBaseUrl);
        loginPage.login("TesteEmail@email.com", "teste123");

        ShopPage shopPage = new ShopPage(driver);
        shopPage.logoutRider();
    }
}
