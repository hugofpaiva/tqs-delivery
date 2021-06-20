package ua.tqs.humberpecas.selenium;

import org.assertj.core.api.Assertions;
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
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.selenium.pages.LoginPage;
import ua.tqs.humberpecas.selenium.pages.ShopPage;
import ua.tqs.humberpecas.selenium.pages.ShoppingCartPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// SpringBootTest to run the REST API
@Testcontainers
@ExtendWith({ScreenshotOnFailureExtension.class})
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ShoppingCartTest {

    private String webApplicationBaseUrl = "172.17.0.1";

    private RemoteWebDriver driver;

    private List<Product> productList;

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

        Product product6 = new Product("Torx Driver", 4.20, Category.SCREWDRIVER, "Torx Driver Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product6);
        productList.add(product6);

        Product product7 = new Product("Robertson Drivers", 10.11, Category.SCREWDRIVER, "Robertson Driver Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product7);
        productList.add(product7);

        Product product8 = new Product("Grommet Plier", 25.00, Category.PLIERS, "Grommet Plier Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product8);
        productList.add(product8);

        Product product9 = new Product("Locking Plier", 15.20, Category.SCREWDRIVER, "Locking Plier Description", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
        productRepository.saveAndFlush(product9);
        productList.add(product9);

        Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
        personRepository.saveAndFlush(person);

        Address address = new Address("Endreço1", "3512-122", "Viseu", "Portugal", person);
        addressRepository.saveAndFlush(address);


        LoginPage loginPage = new LoginPage(this.driver, this.webApplicationBaseUrl);
        loginPage.login("joao@email.com", "difficult-pass");

        ShopPage shopPage = new ShopPage(this.driver, this.webApplicationBaseUrl);
    }

    @AfterEach
    public void resetDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        productRepository.deleteAll();
        productRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }

    @Test
    void testAddProductsToCart() {
        ShopPage shopPage = new ShopPage(this.driver, this.webApplicationBaseUrl);

        shopPage.addToCart(this.productList.get(2));
        shopPage.addToCart(this.productList.get(2));
        shopPage.addToCart(this.productList.get(3));
        shopPage.addToCart(this.productList.get(5));
        shopPage.addToCart(this.productList.get(6));

        shopPage.goToShoppingCart();

        ShoppingCartPage shoppingCartPage = new ShoppingCartPage(this.driver);

        assertThat(shoppingCartPage.getCartTotalProducts(), is(5));
        assertThat(shoppingCartPage.getCartTotalPrice(), is(this.productList.get(2).getPrice() * 2 +
                this.productList.get(3).getPrice() + this.productList.get(5).getPrice() + this.productList.get(6).getPrice()));


        Assertions.assertThat(shoppingCartPage.getAllProducts()).hasSize(5).extracting(Product::getName)
                .containsAll(Arrays.asList(this.productList.get(2).getName(), this.productList.get(2).getName(),
                        this.productList.get(3).getName(), this.productList.get(5).getName(), this.productList.get(6).getName()));
    }

    @Test
    void makePurchase() {
        ShopPage shopPage = new ShopPage(this.driver, this.webApplicationBaseUrl);

        shopPage.addToCart(this.productList.get(7));
        shopPage.addToCart(this.productList.get(2));
        shopPage.addToCart(this.productList.get(2));
        shopPage.addToCart(this.productList.get(3));
        shopPage.addToCart(this.productList.get(5));
        shopPage.addToCart(this.productList.get(6));

        shopPage.goToShoppingCart();

        ShoppingCartPage shoppingCartPage = new ShoppingCartPage(this.driver);

        assertThat(shoppingCartPage.getCartTotalProducts(), is(6));
        assertThat(shoppingCartPage.getCartTotalPrice(), is(this.productList.get(2).getPrice() * 2 +
                this.productList.get(3).getPrice() + this.productList.get(5).getPrice() + this.productList.get(6).getPrice()
                + this.productList.get(7).getPrice()));


        Assertions.assertThat(shoppingCartPage.getAllProducts()).hasSize(6).extracting(Product::getName)
                .containsAll(Arrays.asList(this.productList.get(2).getName(), this.productList.get(2).getName(),
                        this.productList.get(3).getName(), this.productList.get(5).getName(), this.productList.get(6).getName()
                        , this.productList.get(7).getName()));

        shoppingCartPage.MakePurchaseWithFirstAddress();

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        assertThat(shoppingCartPage.isEmpty(), is(true));
    }

}
