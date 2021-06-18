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

		Address address1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal", person);
		addressRepository.saveAndFlush(address1);

		Product product = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
		productRepository.saveAndFlush(product);


		Purchase purchase = new Purchase(person, address1, List.of(product));
		purchase.setStatus(PurchaseStatus.DELIVERED);
		purchaseRepository.saveAndFlush(purchase);

	}
}
