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
import ua.tqs.deliveryservice.repository.*;
import ua.tqs.deliveryservice.selenium.pages.LoginPage;
import ua.tqs.deliveryservice.selenium.pages.RidersInfoPage;
import ua.tqs.deliveryservice.selenium.pages.StoresInfoPage;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RidersInfoTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    private RemoteWebDriver driver;

    private List<Rider> riderList;

    private Manager manager;

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
    private RiderRepository riderRepository;

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

        this.riderList = new ArrayList<>();

        this.driver = this.chromeContainer.getWebDriver();

        this.driver = this.chromeContainer.getWebDriver();

        Rider rider = new Rider("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
        personRepository.saveAndFlush(rider);
        this.riderList.add(rider);

        Rider rider1 = new Rider("Joana", bcryptEncoder.encode("difficult-pass"), "joana@email.com");
        personRepository.saveAndFlush(rider1);
        this.riderList.add(rider1);

        this.manager = new Manager("João", bcryptEncoder.encode("difficult-pass"), "joao1@email.com");
        personRepository.saveAndFlush(this.manager);

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr);

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr1);

        Address addr2 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr2);

        Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr3);

        Address addr4 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr4);

        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2, "http://localhost:8080/delivery");
        storeRepository.saveAndFlush(store1);

        Store store2 = new Store("Loja da Manuela", "A melhor loja2.", "eyJhbGciOiJIUzUxMiJ9.eyJleFAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr4, "http://localhost:8081/delivery");
        storeRepository.saveAndFlush(store2);

        Purchase purchase1 = new Purchase(addr, rider, store1, "client1");
        purchase1.setStatus(Status.DELIVERED);
        purchase1.setRiderReview(4);
        rider.setTotalNumReviews(1);
        rider.setReviewsSum(4);

        Purchase purchase_no_rider = new Purchase(addr1, store1, "client22");
        Purchase purchase_no_rider2 = new Purchase(addr3, store1, "client222");

        purchaseRepository.saveAndFlush(purchase_no_rider);
        purchaseRepository.saveAndFlush(purchase_no_rider2);
        purchase1.setDeliveryTime((purchase1.getDate().getTime() + 120000) - purchase1.getDate().getTime());
        purchaseRepository.saveAndFlush(purchase1);
        personRepository.saveAndFlush(rider);

        LoginPage loginPage = new LoginPage(this.driver, this.webApplicationBaseUrl);
        loginPage.login("joao1@email.com", "difficult-pass");
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
    void testSeeRidersInDb() {
        StoresInfoPage storesInfoPage = new StoresInfoPage(this.driver, this.webApplicationBaseUrl, this.manager.getName());

        storesInfoPage.goToRidersInfo();

        RidersInfoPage ridersInfoPage = new RidersInfoPage(this.driver, this.webApplicationBaseUrl, this.manager.getName());

        List<Rider> websiteRiders = ridersInfoPage.getRiders();

        List<Rider> reverseView = Lists.reverse(this.riderList);

        for (int i = 0; i < reverseView.size() - 1; i++) {
            assertThat(reverseView.get(i).getName(), is(websiteRiders.get(i).getName()));
        }

        assertThat(ridersInfoPage.getOrdersInProgress(), is(2));
        assertThat(ridersInfoPage.getAverageRatingRiders(), is(4.0));
        assertThat(ridersInfoPage.getTotalRiders(), is(2));
        assertThat(ridersInfoPage.getAverageMinutesOfDelivery(), is(2));
    }

    @Test
    void testNoRidersInDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        riderRepository.deleteAll();
        riderRepository.flush();

        StoresInfoPage storesInfoPage = new StoresInfoPage(this.driver, this.webApplicationBaseUrl, this.manager.getName());

        storesInfoPage.goToRidersInfo();

        RidersInfoPage ridersInfoPage = new RidersInfoPage(this.driver, this.webApplicationBaseUrl, this.manager.getName());

        assertThat(ridersInfoPage.isEmpty(), is(true));
        assertThat(ridersInfoPage.getTotalRiders(), is(0));
        assertThat(ridersInfoPage.getOrdersInProgress(), is(0));
    }



}
