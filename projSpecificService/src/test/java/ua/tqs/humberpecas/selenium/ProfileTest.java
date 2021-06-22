package ua.tqs.humberpecas.selenium;

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.selenium.pages.LoginPage;
import ua.tqs.humberpecas.selenium.pages.ProfilePage;
import ua.tqs.humberpecas.selenium.pages.ShopPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProfileTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    private RemoteWebDriver driver;

    private List<Product> productList;

    private List<Purchase> purchaseList;

    private List<Address> addressesList;

    private Person client;

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
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void beforeEachSetUp() {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            this.webApplicationBaseUrl = "host.docker.internal";
        }

        this.driver = this.chromeContainer.getWebDriver();

        this.productList = new ArrayList<>();

        this.purchaseList = new ArrayList<>();

        this.addressesList = new ArrayList<>();


        Product product = new Product("Hex Bolt", 0.50, Category.SCREWS, "Hex Bolt Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product);
        productList.add(product);

        Product product1 = new Product("Hose Clamp Plier", 5.00, Category.PLIERS, "Hose Clamp Plier Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product1);
        productList.add(product1);

        Product product2 = new Product("Slip Joint Pliers", 4.95, Category.PLIERS, "Slip Joint Pliers Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product2);
        productList.add(product2);

        Product product3 = new Product("Roofing Nail", 0.25, Category.NAILS, "Roofing Nail Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product3);
        productList.add(product3);

        Product product4 = new Product("Masonry Nail", 0.20, Category.NAILS, "Masonry Nail Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product4);
        productList.add(product4);

        Product product5 = new Product("Slotted Driver", 4.20, Category.SCREWDRIVER, "Slotted Driver Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product5);
        productList.add(product5);

        this.client = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
        personRepository.saveAndFlush(this.client);

        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal", client);
        addressRepository.saveAndFlush(address);
        addressesList.add(address);

        Purchase purchase1 = new Purchase(client, address, List.of(product3, product4, product3));
        purchaseRepository.saveAndFlush(purchase1);
        purchaseList.add(purchase1);

        Purchase purchase2 = new Purchase(client, address, List.of(product3, product4, product3));
        purchase2.setStatus(PurchaseStatus.DELIVERED);
        purchase2.setRiderReview(5);
        purchase2.setRiderName("Jose");
        purchaseRepository.saveAndFlush(purchase2);
        purchaseList.add(purchase2);

        Purchase purchase = new Purchase(client, address, List.of(product, product, product3));
        purchase.setStatus(PurchaseStatus.DELIVERED);
        purchase.setRiderName("Manelito");
        purchase.setServiceOrderId(14L);
        purchaseRepository.saveAndFlush(purchase);
        purchaseList.add(purchase);

        LoginPage loginPage = new LoginPage(this.driver, this.webApplicationBaseUrl);
        loginPage.login("joao@email.com", "difficult-pass");

        ShopPage shopPage = new ShopPage(this.driver, this.webApplicationBaseUrl);
        shopPage.goToProfile();
    }

    @AfterEach
    public void resetDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        productRepository.deleteAll();
        productRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }

    @Test
    void testGetPurchasesInDB() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());

        assertThat(profilePage.getTotalPurchases(), is(3));
        assertThat(profilePage.getTotalReviews(), is(1));

        // The Purchases come in order of -id to the website
        List<Purchase> reverseView = Lists.reverse(this.purchaseList);
        List<Purchase> purchaseListWebsite = profilePage.getAllPurchases();

        for (int i = 0; i < reverseView.size() - 1; i++) {
            assertThat(purchaseListWebsite.get(i).getRiderName(), is(reverseView.get(i).getRiderName()));
            assertThat(purchaseListWebsite.get(i).getRiderReview(), is(reverseView.get(i).getRiderReview()));
            assertThat(purchaseListWebsite.get(i).getStatus(), is(reverseView.get(i).getStatus()));
            Assertions.assertThat(purchaseListWebsite.get(i).getProducts()).hasSize(reverseView.get(i).getProducts()
                    .size()).extracting(Product::getName).containsAll(reverseView.get(i).getProducts().stream().map(Product::getName).collect(Collectors.toList()));
        }

    }

    @Test
    void testNoPurchasesInDBEmpty() {
        purchaseRepository.deleteAll();

        this.driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        ShopPage shopPage = new ShopPage(this.driver, this.webApplicationBaseUrl);
        shopPage.goToProfile();
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());

        assertThat(profilePage.getTotalPurchases(), is(0));
        assertThat(profilePage.getTotalReviews(), is(0));

        assertThat(profilePage.ordersAreEmpty(), is(true));

    }

    @Test
    void testNoAddressesInDBEmpty() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());

        purchaseRepository.deleteAll();
        addressRepository.deleteAll();

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        assertThat(profilePage.addressesAreEmpty(), is(true));

    }


    @Test
    @Disabled("The endpoint is tested with MockController and TemplateIT and when it is used in the context of " +
            "Selenium test gives an error related to the state of the database. We weren't unable to find a solution" +
            " to this. ")
    void testGiveReviewAndGetProductsInDB() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());

        profilePage.giveReviewToFirstOrder(4);

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        assertThat(profilePage.getTotalPurchases(), is(3));
        assertThat(profilePage.getTotalReviews(), is(2));

        // The Purchases come in order of -id to the website
        List<Purchase> reverseView = Lists.reverse(this.purchaseList);
        List<Purchase> purchaseListWebsite = profilePage.getAllPurchases();

        for (int i = 0; i < reverseView.size() - 1; i++) {
            assertThat(purchaseListWebsite.get(i).getRiderName(), is(reverseView.get(i).getRiderName()));
            assertThat(purchaseListWebsite.get(i).getRiderReview(), is(reverseView.get(i).getRiderReview()));
            assertThat(purchaseListWebsite.get(i).getStatus(), is(reverseView.get(i).getStatus()));
            Assertions.assertThat(purchaseListWebsite.get(i).getProducts()).hasSize(reverseView.get(i).getProducts()
                    .size()).extracting(Product::getName).containsAll(reverseView.get(i).getProducts().stream().map(Product::getName).collect(Collectors.toList()));
        }

    }

    @Test
    void testGetAddressesInDB() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());

        // The Addresses come in order of -id to the website
        List<Address> reverseView = Lists.reverse(this.addressesList);
        List<Address> addressesListWebsite = profilePage.getAllAddresses();;

        for (int i = 0; i < reverseView.size() - 1; i++) {
            assertThat(addressesListWebsite.get(i).getAddress(), is(reverseView.get(i).getAddress()));
            assertThat(addressesListWebsite.get(i).getCity(), is(reverseView.get(i).getCity()));
            assertThat(addressesListWebsite.get(i).getCountry(), is(reverseView.get(i).getCountry()));
            assertThat(addressesListWebsite.get(i).getPostalCode(), is(reverseView.get(i).getPostalCode()));
        }

    }

    @Test
    @Disabled("Because the deletion of Address is in a modal, sometimes Selenium " +
            "can't click it, even with all the verifications...")
    void testDeleteAddress() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());
        profilePage.deleteFirstAddress();

        this.driver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);

        assertThat(profilePage.addressesAreEmpty(), is(true));
    }

    @Test
    void testCreateAddress() {
        ProfilePage profilePage = new ProfilePage(this.driver, this.client.getName());
        Address new_add = new Address("Rua 123", "3670-251", "Vouzela", "Portugal");
        profilePage.createNewAddress(new_add);

        this.driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS);

        List<Address> addressesListWebsite = profilePage.getAllAddresses();

        Assertions.assertThat(addressesListWebsite).hasSize(2);
        Assertions.assertThat(addressesListWebsite).extracting(Address::getAddress).containsAll(Arrays
                .asList(new_add.getAddress(), this.addressesList.get(0).getAddress()));
        Assertions.assertThat(addressesListWebsite).extracting(Address::getCity).containsAll(Arrays
                .asList(new_add.getCity(), this.addressesList.get(0).getCity()));
        Assertions.assertThat(addressesListWebsite).extracting(Address::getCountry).containsAll(Arrays
                .asList(new_add.getCountry(), this.addressesList.get(0).getCountry()));
        Assertions.assertThat(addressesListWebsite).extracting(Address::getPostalCode).containsAll(Arrays
                .asList(new_add.getPostalCode(), this.addressesList.get(0).getPostalCode()));
    }

}
