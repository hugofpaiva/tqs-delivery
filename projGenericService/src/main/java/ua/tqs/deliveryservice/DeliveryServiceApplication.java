package ua.tqs.deliveryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

@SpringBootApplication
public class DeliveryServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}

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

	public void run(String... args) {
		System.out.println("Populating database");

		Rider rider1 = new Rider("João", bcryptEncoder.encode("difficult-pass"), "joao@email.com");
		riderRep.saveAndFlush(rider1);

		Manager manager1 = new Manager();
		manager1.setEmail("joao1@email.com");
		manager1.setPwd(bcryptEncoder.encode("difficult-pass"));
		manager1.setName("João");
		managerRep.saveAndFlush(manager1);

		Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.saveAndFlush(addr1);

		Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
		addressRep.saveAndFlush(addr2);

		Store store1 = new Store("Loja do Manel", "A melhor loja.", "manel", addr2);
		storeRep.saveAndFlush(store1);

		Purchase purchase1 = new Purchase(addr1, rider1, store1, "client1");
		Purchase purchase2 = new Purchase(addr1, rider1, store1, "client2");
		purchaseRep.saveAndFlush(purchase1);
		purchaseRep.saveAndFlush(purchase2);

	}
}
