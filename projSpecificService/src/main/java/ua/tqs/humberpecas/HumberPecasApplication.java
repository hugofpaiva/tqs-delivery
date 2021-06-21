package ua.tqs.humberpecas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class HumberPecasApplication {

	public static void main(String[] args) {
		SpringApplication.run(HumberPecasApplication.class, args);
	}

}

@Profile("!test")
@Component
class DBLoader implements CommandLineRunner {
	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PurchaseRepository purchaseRepository;

	@Autowired
	private GenericRepository genericRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public void run(String... args) {
		System.out.println("Populating database");

		Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
		personRepository.saveAndFlush(person);

		Address address1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal", person);
		addressRepository.saveAndFlush(address1);

		Product product = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
		productRepository.saveAndFlush(product);

		Product product1 = new Product("Hammer", 5.00, Category.LATHES, "xpto",  "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
		productRepository.saveAndFlush(product1);

		Product product2 = new Product("Hammer", 5.00, Category.LATHES, "xpto",  "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
		productRepository.saveAndFlush(product2);

		Product product3 = new Product("Hammer", 5.00, Category.LATHES, "xpto",  "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
		productRepository.saveAndFlush(product3);

		String genericToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
		Generic generic = new Generic("generic", genericToken);
		genericRepository.save(generic);

		Purchase purchase = new Purchase(person, address1, List.of(product));
		purchase.setStatus(PurchaseStatus.DELIVERED);
		purchase.setServiceOrderId(5L);
		purchaseRepository.saveAndFlush(purchase);


		Purchase purchase2 = new Purchase(person, address1, List.of(product2));
		purchase2.setStatus(PurchaseStatus.PENDENT);
		purchase2.setServiceOrderId(6L);
		purchaseRepository.saveAndFlush(purchase2);


		Purchase purchase3 = new Purchase(person, address1, List.of(product3));
		purchase3.setStatus(PurchaseStatus.PENDENT);
		purchase3.setServiceOrderId(11L);
		purchaseRepository.saveAndFlush(purchase3);



		Purchase purchase4 = new Purchase(person, address1, Arrays.asList(product2, product));
		purchase4.setStatus(PurchaseStatus.PENDENT);
		purchase4.setServiceOrderId(22L);
		purchaseRepository.saveAndFlush(purchase4);


		Purchase purchase5 = new Purchase(person, address1, Arrays.asList(product2, product3));
		purchase5.setStatus(PurchaseStatus.PENDENT);
		purchase5.setServiceOrderId(23L);
		purchaseRepository.saveAndFlush(purchase5);
	}
}
