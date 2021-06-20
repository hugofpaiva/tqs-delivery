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

		Product product = new Product( 0.50,"Parafuso" , "xpto", Category.SCREWS);
		productRepository.saveAndFlush(product);

		Product product1 = new Product(5.00, "Hammer", "xpto",  Category.LATHES);
		productRepository.saveAndFlush(product1);

		Product product2 = new Product(5.00, "Hammer", "xpto", Category.LATHES);
		productRepository.saveAndFlush(product2);

		Product product3 = new Product( 5.00, "Hammer", "xpto", Category.LATHES);
		productRepository.saveAndFlush(product3);

		String genericToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
		Generic generic = new Generic("generic", genericToken);
		genericRepository.save(generic);


		Purchase purchase = new Purchase(person, address1, List.of(product));
		purchase.setServiceOrderId(6L);
		purchaseRepository.saveAndFlush(purchase);

		Purchase purchase2 = new Purchase(person, address1, List.of(product));
		purchase2.setServiceOrderId(5L);
		purchase2.setRiderName("Joao");
		purchase2.setStatus(PurchaseStatus.ACCEPTED);
		purchaseRepository.saveAndFlush(purchase2);


	}
}
