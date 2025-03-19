package br.com.microservices.choreography.productvalidationservice.core.repository;

import br.com.microservices.choreography.productvalidationservice.core.dto.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Boolean existsByCode(String code);
}