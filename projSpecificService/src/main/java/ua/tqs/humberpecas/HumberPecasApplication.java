package ua.tqs.humberpecas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;

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
	private ProductRepository productRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public void run(String... args) {
		System.out.println("Populating database");

		Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
		personRepository.saveAndFlush(person);

		Product product = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "image_url");
		productRepository.saveAndFlush(product);


	}
}
