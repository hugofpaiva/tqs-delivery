package ua.tqs.deliveryservice.selenium;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;
import ua.tqs.deliveryservice.selenium.pages.LoginPage;
import ua.tqs.deliveryservice.selenium.pages.UserInfoPage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RiderTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    private RemoteWebDriver driver;

    private List<Purchase> purchaseList;

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
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    public void beforeEachSetUp() {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            this.webApplicationBaseUrl = "host.docker.internal";
        }

        this.purchaseList = new ArrayList<>();

        this.driver = this.chromeContainer.getWebDriver();

        Rider rider = new Rider("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
        personRepository.saveAndFlush(rider);

        LoginPage loginPage = new LoginPage(this.driver, this.webApplicationBaseUrl);
        loginPage.login("joao@email.com", "difficult-pass");


        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr);

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr1);

        Address addr2 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr2);

        Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr3);

        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);
        storeRepository.saveAndFlush(store1);

        Purchase purchase1 = new Purchase(addr, rider, store1, "client1");
        purchase1.setStatus(Status.DELIVERED);
        purchase1.setRiderReview(4);

        Purchase purchase_no_rider = new Purchase(addr1, store1, "client22");
        Purchase purchase_no_rider2 = new Purchase(addr3, store1, "client222");

        purchaseRepository.saveAndFlush(purchase_no_rider);
        purchaseList.add(purchase_no_rider);
        purchaseRepository.saveAndFlush(purchase_no_rider2);
        purchaseList.add(purchase_no_rider2);
        purchase1.setDeliveryTime((purchase1.getDate().getTime() + 120000) - purchase1.getDate().getTime());
        purchaseRepository.saveAndFlush(purchase1);
        purchaseList.add(purchase1);

    }

    @AfterEach
    public void resetDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        storeRepository.deleteAll();
        storeRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }

    @Test
    void testSeeLastOrdersInDb() {
        UserInfoPage userInfoPage = new UserInfoPage(this.driver);

        List<Purchase> reverseView = Lists.reverse(this.purchaseList);
        List<Purchase> websitePurchases = userInfoPage.getOrders();


        System.out.println(websitePurchases.get(0));
        assertThat(false, is(true));
    }

    @Test
    void testNoOrdersInDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
        UserInfoPage userInfoPage = new UserInfoPage(this.driver);

        assertThat(userInfoPage.isEmpty(), is(true));
    }


}
