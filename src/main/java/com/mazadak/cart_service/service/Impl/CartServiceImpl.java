package com.mazadak.cart_service.service.Impl;

import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.exception.CartItemNotFoundException;
import com.mazadak.cart_service.exception.CartNotFoundException;
import com.mazadak.cart_service.mapper.CartMapper;
import com.mazadak.cart_service.model.Cart;
import com.mazadak.cart_service.model.CartItem;
import com.mazadak.cart_service.model.enums.Status;
import com.mazadak.cart_service.repository.CartItemRepository;
import com.mazadak.cart_service.repository.CartRepository;
import com.mazadak.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getActiveCart(UUID userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return cartMapper.toCartResponseDTO(cart);
    }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(Status.ACTIVE);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> getCartItems(UUID userId) {
        System.out.println("getCartItems");
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .map(cartMapper::toCartItemResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO addItem(UUID userId, AddItemRequest request) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), request.productId())
                .map(existingItem -> {
                    int newQuantity = existingItem.getQuantity() + request.quantity();
                    existingItem.setQuantity(newQuantity);
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(request.productId());
                    newItem.setQuantity(request.quantity());
                    return cartItemRepository.save(newItem);
                });

        return cartMapper.toCartItemResponseDTO(cartItem);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO updateItemQuantity(UUID userId, UpdateItemRequest request) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), request.productId())
                .map(existingItem -> {
                    existingItem.setQuantity(request.quantity());
                    return cartItemRepository.save(existingItem);
                })
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart."));

        return cartMapper.toCartItemResponseDTO(cartItem);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO reduceItemQuantity(UUID userId, UUID productId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .map(item -> {
                    int newQuantity = item.getQuantity() - 1;
                    if (newQuantity <= 0) {
                        cartItemRepository.delete(item);
                        return null;
                    }
                    item.setQuantity(newQuantity);
                    return cartItemRepository.save(item);
                })
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart"));
        return cartMapper.toCartItemResponseDTO(cartItem);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeItem(UUID userId, UUID productId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart"));

        cartItemRepository.delete(cartItem);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        cartItemRepository.deleteAllByCart_CartId(cart.getCartId());
    }

}
