package ua.tqs.humberpecas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.List;
import java.util.Set;

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
	private PurchaseRepository purchaseRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public void run(String... args) {
		System.out.println("Populating database");

		Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
		personRepository.saveAndFlush(person);

		Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
		addressRepository.saveAndFlush(address);

		Address address1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
		address1.setPerson(person);
		addressRepository.saveAndFlush(address1);

		Product product = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "image_url");
		productRepository.saveAndFlush(product);


		Purchase purchase = new Purchase(person, address, List.of(product));
		purchase.setStatus(PurchaseStatus.DELIVERED);
		purchaseRepository.saveAndFlush(purchase);

	}
}
