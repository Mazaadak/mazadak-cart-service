package com.mazadak.cart_service.repository;

import com.mazadak.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByCartId(UUID cartId);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUserId(@Param("userId") UUID userId);

}