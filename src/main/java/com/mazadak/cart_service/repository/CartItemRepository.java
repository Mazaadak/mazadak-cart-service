package com.mazadak.cart_service.repository;

import com.mazadak.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCart_CartId(UUID cartId);


    Optional<CartItem> findByCart_CartIdAndProductId(@Param("cartId") UUID cartId,
                                                             @Param("productId") UUID productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteAllByCart_CartId(@Param("cartId") UUID cartId);
}
