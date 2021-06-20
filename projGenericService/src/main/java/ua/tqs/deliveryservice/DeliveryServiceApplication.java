package ua.tqs.deliveryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

@SpringBootApplication
public class DeliveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}
}

@Profile("!test")
@Component
class DBLoader implements CommandLineRunner {

	@Autowired
	private RiderRepository riderRep;

	@Autowired
	private ManagerRepository managerRep;

	@Autowired
	private AddressRepository addressRep;

	@Autowired
	private PurchaseRepository purchaseRep;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Autowired
	private StoreRepository storeRep;

	@Override
	public void run(String... args) {
		System.out.println("Populating database");

		Rider rider1 = new Rider("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
		riderRep.saveAndFlush(rider1);

		Rider new_rider = new Rider("Antonio", bcryptEncoder.encode("difficult-pass"), "antonio@email.com");
		riderRep.saveAndFlush(new_rider);

		Manager manager1 = new Manager();
		manager1.setEmail("joao1@email.com");
		manager1.setPwd(bcryptEncoder.encode("difficult-pass"));
		manager1.setName("João");
		managerRep.saveAndFlush(manager1);

		Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.saveAndFlush(addr1);

		Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
		addressRep.saveAndFlush(addr2);

		Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.saveAndFlush(addr3);

		Address addr4 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.saveAndFlush(addr4);

		Address addr5 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.saveAndFlush(addr5);
		String storeUrl = "http://localhost:8081/delivery/";
		Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2, storeUrl);
		storeRep.saveAndFlush(store1);

		Purchase purchase1 = new Purchase(addr1, rider1, store1, "client1");
		// Purchase purchase2 = new Purchase(addr3, rider1, store1, "client2");

		Purchase purchase_no_rider = new Purchase(addr4, store1, "client22");
		Purchase purchase_no_rider2 = new Purchase(addr5, store1, "client222");

		purchaseRep.saveAndFlush(purchase_no_rider);
		purchaseRep.saveAndFlush(purchase_no_rider2);
		purchaseRep.saveAndFlush(purchase1);
		//purchaseRep.saveAndFlush(purchase2);


	}
}
