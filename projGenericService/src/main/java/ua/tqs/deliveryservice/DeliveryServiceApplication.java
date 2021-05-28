package ua.tqs.deliveryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

@SpringBootApplication
public class DeliveryServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}

	@Autowired
	private RiderRepository riderRep;

	@Autowired
	private AddressRepository addressRep;

	@Autowired
	private PurchaseRepository purchaseRep;

	@Autowired
	private StoreRepository storeRep;

	public void run(String... args) {
		System.out.println("Populating database");

		Rider rider1 = new Rider("João", "difficult-pass", "joao@email.com");
		riderRep.save( rider1 );

		Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
		addressRep.save( addr1 );

		Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
		addressRep.save( addr2 );

		Store store1 = new Store("Loja do Manel", "A melhor loja.", "manel", addr2);
		storeRep.save( store1 );

		Purchase purchase1 = new Purchase(addr1, rider1, store1);
		Purchase purchase2 = new Purchase(addr1, rider1, store1);
		purchaseRep.save( purchase1 );
		purchaseRep.save( purchase2 );

	}
}
