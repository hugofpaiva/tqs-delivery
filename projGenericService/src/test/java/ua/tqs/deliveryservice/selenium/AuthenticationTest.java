package ua.tqs.deliveryservice.selenium;

import org.junit.jupiter.api.*;
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
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.selenium.pages.LoginPage;
import ua.tqs.deliveryservice.selenium.pages.RegisterPage;
import ua.tqs.deliveryservice.selenium.pages.StoresInfoPage;
import ua.tqs.deliveryservice.selenium.pages.UserInfoPage;

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
        LoginPage loginPage = new LoginPage(driver, this.webApplicationBaseUrl);

        // Check if loaded page is the Rider one
        assertThat(loginPage.login("joao@email.com", "difficult-pass"), is("User profile"));
        UserInfoPage userInfoPage = new UserInfoPage(driver);
        userInfoPage.logoutRider();
    }

    @Test
    void testLoginLogoutManager() {
        RemoteWebDriver driver = this.chromeContainer.getWebDriver();
        LoginPage loginPage = new LoginPage(driver, this.webApplicationBaseUrl);

        // Check if loaded page is the Manager one
        assertThat(loginPage.login("joana@email.com", "difficult-pass"), is("Stores Info"));
        StoresInfoPage storesInfoPage = new StoresInfoPage(driver);
        storesInfoPage.logoutManager();
    }

    @Test
    void testRegisterLoginLogoutRider() {
        RemoteWebDriver driver = this.chromeContainer.getWebDriver();
        RegisterPage registerPage = new RegisterPage(driver, this.webApplicationBaseUrl);
        registerPage.register("TesteEmail@email.com", "teste123", "TesteName");
        LoginPage loginPage = new LoginPage(driver, this.webApplicationBaseUrl);

        // Check if loaded page is the Rider one
        assertThat(loginPage.login("TesteEmail@email.com", "teste123"), is("User profile"));
        UserInfoPage userInfoPage = new UserInfoPage(driver);
        userInfoPage.logoutRider();
    }
}
