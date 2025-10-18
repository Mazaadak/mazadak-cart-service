package com.mazadak.cart_service.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "cart")
@Table(name = "cart_item",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id", "product_id"})},
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id")
        })
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;


    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    @Min(1)
    private Integer quantity;

}



