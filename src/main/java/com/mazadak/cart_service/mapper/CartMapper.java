package com.mazadak.cart_service.mapper;


import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.model.CartItem;
import com.mazadak.cart_service.model.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {


    CartResponseDTO toCartResponseDTO(Cart cart);
    CartItemResponseDTO toCartItemResponseDTO(CartItem cartItem);
    CartItem toCartItem(CartItemResponseDTO cartItemResponseDTO);
    Cart toCart(CartResponseDTO cartResponseDTO);

}
