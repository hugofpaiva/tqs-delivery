package ua.tqs.humberpecas;

import lombok.extern.log4j.Log4j2;
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

@Profile("!test && !CI")
@Log4j2
@Component
class DBLoaderProd implements CommandLineRunner {
	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private GenericRepository genericRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public void run(String... args) {
		log.info("Populating database");

		Person person = new Person("João", bcryptEncoder.encode("difficult-pass1"), "joao@email.com");
		personRepository.saveAndFlush(person);

		Person person1 = new Person("tone", bcryptEncoder.encode("difficult-pass"), "tone@email.com");
		personRepository.saveAndFlush(person1);

		Address address1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal", person);
		addressRepository.saveAndFlush(address1);

		Address address2 = new Address("Cascos de Rolha", "3800-000", "Aveiro", "Portugal", person1);
		addressRepository.saveAndFlush(address2);

		Address address3 = new Address("Canidelo", "3800-000", "Porto", "Portugal", person);
		addressRepository.saveAndFlush(address3);

		Product product10 = new Product("Hex Bolt", 0.50, Category.SCREWS, "Hex Bolt Description", "https://images.homedepot-static.com/productImages/c31796fc-fdd9-4994-9c5a-97d7ee2b6d4f/svn/everbilt-hex-bolts-80450-64_1000.jpg");
		productRepository.saveAndFlush(product10);

		Product product1 = new Product("Hose Clamp Plier", 5.00, Category.PLIERS, "Hose Clamp Plier Description", "https://alexnld.com/wp-content/uploads/2018/07/92513d90-634d-428b-90a3-56d6d476a0a6.jpg");
		productRepository.saveAndFlush(product1);


		Product product2 = new Product("Slip Joint Pliers", 4.95, Category.PLIERS, "Slip Joint Pliers Description", "https://www.wildetool.com/wp-content/uploads/2017/02/085432052988.png");
		productRepository.saveAndFlush(product2);


		Product product3 = new Product("Roofing Nail", 0.25, Category.NAILS, "Roofing Nail Description", "https://advancedbuilders.co.ke/wp-content/uploads/2018/03/roofing-nails.jpg");
		productRepository.saveAndFlush(product3);


		Product product4 = new Product("Masonry Nail", 0.20, Category.NAILS, "Masonry Nail Description", "https://res.cloudinary.com/rglweb/image/fetch/f_auto/https://www.raygrahams.com/images/thumbs/0054121_700.jpg");
		productRepository.saveAndFlush(product4);


		Product product5 = new Product("Slotted Driver", 4.20, Category.SCREWDRIVER, "Slotted Driver Description", "http://mayhew.com/wp-content/uploads/2016/09/45005-slotted-1.jpg");
		productRepository.saveAndFlush(product5);


		Product product6 = new Product("Torx Driver", 4.20, Category.SCREWDRIVER, "Torx Driver Description", "https://encosystems.net/wp-content/uploads/t6.jpg");
		productRepository.saveAndFlush(product6);


		Product product7 = new Product("Robertson Driver", 10.11, Category.SCREWDRIVER, "Robertson Driver Description", "https://smhttp-ssl-68934.nexcesscdn.net/media/catalog/product/cache/71dc07da46cd1a9d67b7d47451e6e708/I/R/IRW1837488-hires.jpg");
		productRepository.saveAndFlush(product7);


		Product product8 = new Product("Grommet Plier", 25.00, Category.PLIERS, "Grommet Plier Description", "https://www.toolsource.com/images/prod_images/KTI54055_1200Wx1200H.jpg");
		productRepository.saveAndFlush(product8);


		Product product9 = new Product("Locking Plier", 15.20, Category.SCREWDRIVER, "Locking Plier Description", "https://itslondon.s3.amazonaws.com/p/xxl/VIST0902EL4.jpg");
		productRepository.saveAndFlush(product9);


		Product product11 = new Product("Frearson Driver", 10.20, Category.SCREWDRIVER, "Frearson Driver Description", "https://cdn.shopify.com/s/files/1/0665/9843/products/386075_1024x1024.jpg?v=1443713197");
		productRepository.saveAndFlush(product11);

		Product product12 = new Product("Bolster Screwdriver", 18.50, Category.SCREWDRIVER, "Bolster Screwdriver Description", "http://www.expert-toolstore.com/4450-thickbox_default/britool-expert-phillips-bolster-screwdrivers.jpg");
		productRepository.saveAndFlush(product12);

		Product product13 = new Product("Spanner Screwdriver", 8.50, Category.SCREWDRIVER, "Spanner Screwdriver Description", "https://i.ebayimg.com/00/s/NTAwWDUwMA==/z/qlUAAMXQ74JTWH6m/$_3.JPG?set_id=2");
		productRepository.saveAndFlush(product13);

		Product product14 = new Product("Tri-angle Driver", 12.50, Category.SCREWDRIVER, "Tri-angle Driver Description", "https://ae01.alicdn.com/kf/HTB1LNHLb1GSBuNjSspbq6AiipXa9/Magnetic-Triangle-Screwdriver-2-3mm-Triangle-Head-Screw-driver-repair-tool-for-toys-Cheap.jpg");
		productRepository.saveAndFlush(product14);


		String genericToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
		Generic generic = new Generic("generic", genericToken);
		genericRepository.save(generic);



	}

}


	@Profile("CI")
	@Log4j2
	@Component
	class DBLoaderCI implements CommandLineRunner {

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
			log.info("Populating database");

			Person person = new Person("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
			personRepository.saveAndFlush(person);

			Address address1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal", person);
			addressRepository.saveAndFlush(address1);

			Product product = new Product("Parafuso", 0.50, Category.SCREWS, "xpto", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
			productRepository.saveAndFlush(product);

			Product product1 = new Product("Hammer", 5.00, Category.LATHES, "xpto", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
			productRepository.saveAndFlush(product1);

			Product product2 = new Product("Hammer", 5.00, Category.LATHES, "xpto", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
			productRepository.saveAndFlush(product2);

			Product product3 = new Product("Hammer", 5.00, Category.LATHES, "xpto", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.megalojista.com.br%2Fmedia%2Fcatalog%2Fproduct%2Fp%2Fa%2Fparafuso_auto_atarraxante_cabeca_panela_phillips_passivado_base.png_61.jpg&f=1&nofb=1");
			productRepository.saveAndFlush(product3);

			String genericToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
			Generic generic = new Generic("generic", genericToken);
			genericRepository.save(generic);

			Purchase purchase = new Purchase(person, address1, List.of(product));
			purchase.setStatus(PurchaseStatus.DELIVERED);
			purchase.setServiceOrderId(28L);
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

