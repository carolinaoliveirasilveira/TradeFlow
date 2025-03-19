package br.com.microservices.choreography.inventoryservice.core.repository;

import br.com.microservices.choreography.inventoryservice.core.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProductCode(String productCode);
}
