package com.mazadak.cart_service.repository;

import com.mazadak.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCart_CartId(UUID cartId);

    Optional<CartItem> findByCart_CartIdAndProductId(UUID cartId, UUID productId);


}
