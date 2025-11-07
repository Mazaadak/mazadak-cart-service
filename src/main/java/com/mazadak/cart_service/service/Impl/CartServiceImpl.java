package com.mazadak.cart_service.service.Impl;

import com.mazadak.cart_service.client.ProductClient;
import com.mazadak.cart_service.dto.entity.ProductImageDTO;
import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.dto.response.DetailedCartItemResponseDTO;
import com.mazadak.cart_service.dto.response.ProductResponseDTO;
import com.mazadak.cart_service.exception.CartIsNotActiveException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartMapper cartMapper;

    private final ProductClient productClient;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(UUID userId) {
        log.info("getting The Active Cart for {}", userId);
        Cart cart = getUserCart(userId);
        log.info("cart: {}", cart);
        return cartMapper.toCartResponseDTO(cart);
    }

    private Cart getUserCart(UUID userId) {
        return cartRepository.findCartByUserId(userId)
                .orElseGet(() -> {
                    log.debug("No cart found for user: {}", userId);
                    return createNewCart(userId);
                });
    }

    private Cart createNewCart(UUID userId) {
        log.info("creating new cart for {}", userId);
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(Status.ACTIVE);
        log.info("new cart: {}", cart);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> getCartItems(UUID userId) {
        log.info("getting cart items for {}", userId);
        Cart cart = getUserCart(userId);
        return cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .map(cartMapper::toCartItemResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO addItem(UUID userId, AddItemRequest request) {
        log.info("adding item {} to cart for {}", request, userId);
        Cart cart = getUserCart(userId);

       checkCartStatus(cart);

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), request.productId())
                .map(existingItem -> {
                    log.info("item {} already exists in cart", request.productId());
                    int newQuantity = existingItem.getQuantity() + request.quantity();
                    log.info("new quantity: {}", newQuantity);
                    existingItem.setQuantity(newQuantity);
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    log.info("item {} does not exist in cart, adding new item", request.productId());
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(request.productId());
                    newItem.setQuantity(request.quantity());
                    return cartItemRepository.save(newItem);
                });
        log.info("item {} added to cart", request.productId());
        return cartMapper.toCartItemResponseDTO(cartItem);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO updateItemQuantity(UUID userId, UUID productId, UpdateItemRequest request) {
        log.info("updating item {} quantity to {} for user {}",productId, request.quantity(), userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        checkCartStatus(cart);

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(),productId)
                .map(existingItem -> {
                    existingItem.setQuantity(request.quantity());
                    return cartItemRepository.save(existingItem);
                })
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart."));
        log.info("item {} quantity updated to {}",productId, request.quantity());
        return cartMapper.toCartItemResponseDTO(cartItem);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartItemResponseDTO reduceItemQuantity(UUID userId, UUID productId, int quantity) {
        log.info("reducing item {} quantity for user {}", productId, userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        checkCartStatus(cart);

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .map(item -> {
                    int newQuantity = item.getQuantity() - quantity;
                    if (newQuantity <= 0) {
                        log.info("item {} quantity reduced to 0, removing item from cart", productId);
                        cartItemRepository.delete(item);
                        return null;
                    }
                    log.info("item {} quantity reduced to {}", productId, newQuantity);
                    item.setQuantity(newQuantity);
                    return cartItemRepository.save(item);
                })
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart"));
        return cartMapper.toCartItemResponseDTO(cartItem);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeItem(UUID userId, UUID productId) {
        log.info("removing item {} from cart for user {}", productId, userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        checkCartStatus(cart);

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart"));

        cartItemRepository.delete(cartItem);
        log.info("item {} removed from cart", productId);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void clearCart(UUID userId) {
        log.info("clearing cart for user {}", userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));

        cartItemRepository.deleteAllByCart_CartId(cart.getCartId());
        log.info("cart cleared for user {}", userId);
    }

    public void checkCartStatus(Cart cart) {
        if(cart.getStatus() == Status.INACTIVE) {
            log.info("Cart is not active Checkout is processing");
            throw new CartIsNotActiveException("Cart is not active Checkout is processing");
        }
    }

    @Override
    public void activateCart(UUID userId) {
        log.info("activating cart for user {}", userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        cart.setStatus(Status.ACTIVE);
        cartRepository.save(cart);
        log.info("cart activated for user {}", userId);
    }

    @Override
    public void deactivateCart(UUID userId) {
        log.info("deactivating cart for user {}", userId);
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        cart.setStatus(Status.INACTIVE);
        cartRepository.save(cart);
        log.info("cart deactivated for user {}", userId);
    }

    @Override
    public Boolean isActive(UUID userId) {
        Cart cart = getUserCart(userId);

        return cart.getStatus().equals(Status.ACTIVE);
    }

    @Override
    public List<DetailedCartItemResponseDTO> getDetailedCartItems(UUID userId) {
        log.info("getting detailed cart items for user {} ", userId);

        Cart cart = getUserCart(userId);

        List<CartItem> cartItems = cart.getCartItems();

        if(cartItems.isEmpty()){
            log.info("no cartItems found for user {}", userId);
            return new ArrayList<>();
        }

        List<UUID> productIds = cartItems.stream().map(CartItem::getProductId).collect(Collectors.toList());
        List<ProductResponseDTO> products = productClient.getProductsByIds(productIds).getBody();
        // Mapping ProductId to ProductResponseDTO for fast lookup
        Map<UUID,ProductResponseDTO> productMap = products.stream().collect(Collectors.toMap(ProductResponseDTO::productId, Function.identity()));

        List<DetailedCartItemResponseDTO> detailedCartItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductResponseDTO product = productMap.get(cartItem.getProductId());
            if (product == null) {
                log.error("Product not found: {}", cartItem.getProductId());
                throw new RuntimeException("Product not found: " + cartItem.getProductId());
            }

            String primaryImage = product.images().stream()
                    .filter(ProductImageDTO::isPrimary)
                    .findFirst()
                    .map(ProductImageDTO::imageUri)
                    .orElseGet(() -> product.images().isEmpty() ? null : product.images().get(0).imageUri());

            DetailedCartItemResponseDTO detailedCartItem = new DetailedCartItemResponseDTO(
                    cartItem.getProductId(),
                    cartItem.getQuantity(),
                    product.title(),
                    product.description(),
                    product.price(),
                    primaryImage
            );

            detailedCartItems.add(detailedCartItem);

            log.info("Detailed Cart Item for product {} is {}", cartItem.getProductId(), detailedCartItem);
        }
        return detailedCartItems;
    }

}
