package com.mazadak.cart_service.service.Impl;

import com.mazadak.cart_service.client.ProductClient;
import com.mazadak.cart_service.dto.entity.ProductImageDTO;
import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.dto.response.DetailedCartItemResponseDTO;
import com.mazadak.cart_service.dto.response.ProductResponseDTO;
import com.mazadak.cart_service.mapper.CartMapper;
import com.mazadak.cart_service.model.Cart;
import com.mazadak.cart_service.model.CartItem;
import com.mazadak.cart_service.model.enums.Status;
import com.mazadak.cart_service.repository.CartItemRepository;
import com.mazadak.cart_service.repository.CartRepository;
import com.mazadak.common.exception.domain.cart.CartIsNotActiveException;
import com.mazadak.common.exception.shared.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID userId;
    private UUID cartId;
    private UUID productId;
    private UUID itemId;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        cart = new Cart();
        cart.setCartId(cartId);
        cart.setUserId(userId);
        cart.setStatus(Status.ACTIVE);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setItemId(itemId);
        cartItem.setCart(cart);
        cartItem.setProductId(productId);
        cartItem.setQuantity(2);
    }

    @Nested
    @DisplayName("GetCart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should return existing cart for user")
        void shouldReturnExistingCart() {
            // Arrange
            CartResponseDTO expectedResponse = new CartResponseDTO(cartId, userId, Collections.emptyList());
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartMapper.toCartResponseDTO(cart)).thenReturn(expectedResponse);

            // Act
            CartResponseDTO result = cartService.getCart(userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.cartId()).isEqualTo(cartId);
            assertThat(result.userId()).isEqualTo(userId);
            verify(cartRepository).findCartByUserId(userId);
            verify(cartMapper).toCartResponseDTO(cart);
        }

        @Test
        @DisplayName("Should create new cart when user has no cart")
        void shouldCreateNewCartWhenNoneExists() {
            // Arrange
            CartResponseDTO expectedResponse = new CartResponseDTO(cartId, userId, Collections.emptyList());

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartMapper.toCartResponseDTO(any(Cart.class))).thenReturn(expectedResponse);

            // Act
            CartResponseDTO result = cartService.getCart(userId);

            // Assert
            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
            verify(cartMapper).toCartResponseDTO(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("AddItem Tests")
    class AddItemTests {

        private AddItemRequest addItemRequest;

        @BeforeEach
        void setUp() {
            addItemRequest = new AddItemRequest(productId, 3);
        }

        @Test
        @DisplayName("Should add new item to cart")
        void shouldAddNewItemToCart() {
            // Arrange
            CartItemResponseDTO expectedResponse = new CartItemResponseDTO(itemId, productId, 3);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
            when(cartMapper.toCartItemResponseDTO(any(CartItem.class))).thenReturn(expectedResponse);

            // Act
            CartItemResponseDTO result = cartService.addItem(userId, addItemRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.productId()).isEqualTo(productId);
            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartMapper).toCartItemResponseDTO(any(CartItem.class));
        }

        @Test
        @DisplayName("Should update quantity when item already exists")
        void shouldUpdateQuantityWhenItemExists() {
            // Arrange
            cartItem.setQuantity(2);
            CartItemResponseDTO expectedResponse = new CartItemResponseDTO(itemId, productId, 5);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
            when(cartMapper.toCartItemResponseDTO(cartItem)).thenReturn(expectedResponse);

            // Act
            CartItemResponseDTO result = cartService.addItem(userId, addItemRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(5);
            verify(cartItemRepository).save(cartItem);
        }

        @Test
        @DisplayName("Should throw exception when cart is inactive")
        void shouldThrowExceptionWhenCartIsInactive() {
            // Arrange
            cart.setStatus(Status.INACTIVE);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addItem(userId, addItemRequest))
                    .isInstanceOf(CartIsNotActiveException.class)
                    .hasMessageContaining("Cart is not active");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }
    }

    @Nested
    @DisplayName("UpdateItemQuantity Tests")
    class UpdateItemQuantityTests {

        private UpdateItemRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateItemRequest(5);
        }

        @Test
        @DisplayName("Should update item quantity successfully")
        void shouldUpdateItemQuantity() {
            // Arrange
            CartItemResponseDTO expectedResponse = new CartItemResponseDTO(itemId, productId, 5);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
            when(cartMapper.toCartItemResponseDTO(cartItem)).thenReturn(expectedResponse);

            // Act
            CartItemResponseDTO result = cartService.updateItemQuantity(userId, productId, updateRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(5);
            verify(cartItemRepository).save(cartItem);
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowExceptionWhenCartNotFound() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateItemQuantity(userId, productId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart");
        }

        @Test
        @DisplayName("Should throw exception when item not found in cart")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateItemQuantity(userId, productId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item not found");
        }
    }

    @Nested
    @DisplayName("ReduceItemQuantity Tests")
    class ReduceItemQuantityTests {

        @Test
        @DisplayName("Should reduce item quantity")
        void shouldReduceItemQuantity() {
            // Arrange
            cartItem.setQuantity(5);
            CartItemResponseDTO expectedResponse = new CartItemResponseDTO(itemId, productId, 3);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.of(cartItem));
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
            when(cartMapper.toCartItemResponseDTO(cartItem)).thenReturn(expectedResponse);

            // Act
            CartItemResponseDTO result = cartService.reduceItemQuantity(userId, productId, 2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(3);
            verify(cartItemRepository).save(cartItem);
            verify(cartItemRepository, never()).delete(any());
        }

    }

    @Nested
    @DisplayName("RemoveItem Tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Should remove item from cart successfully")
        void shouldRemoveItemFromCart() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.of(cartItem));

            // Act
            cartService.removeItem(userId, productId);

            // Assert
            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        @DisplayName("Should throw exception when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCart_CartIdAndProductId(cartId, productId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.removeItem(userId, productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item not found");
        }
    }

    @Nested
    @DisplayName("ClearCart Tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear all items from cart")
        void shouldClearAllItemsFromCart() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // Act
            cartService.clearCart(userId);

            // Assert
            verify(cartItemRepository).deleteAllByCart_CartId(cartId);
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowExceptionWhenCartNotFound() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.clearCart(userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("CartStatus Tests")
    class CartStatusTests {

        @Test
        @DisplayName("Should activate cart successfully")
        void shouldActivateCart() {
            // Arrange
            cart.setStatus(Status.INACTIVE);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            // Act
            cartService.activateCart(userId);

            // Assert
            assertThat(cart.getStatus()).isEqualTo(Status.ACTIVE);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Should deactivate cart successfully")
        void shouldDeactivateCart() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            // Act
            cartService.deactivateCart(userId);

            // Assert
            assertThat(cart.getStatus()).isEqualTo(Status.INACTIVE);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Should return true when cart is active")
        void shouldReturnTrueWhenCartIsActive() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // Act
            Boolean result = cartService.isActive(userId);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when cart is inactive")
        void shouldReturnFalseWhenCartIsInactive() {
            // Arrange
            cart.setStatus(Status.INACTIVE);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // Act
            Boolean result = cartService.isActive(userId);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("GetDetailedCartItems Tests")
    class GetDetailedCartItemsTests {

        private UUID sellerId;

        @BeforeEach
        void setUp() {
            sellerId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should return empty list when cart has no items")
        void shouldReturnEmptyListWhenCartIsEmpty() {
            // Arrange
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // Act
            List<DetailedCartItemResponseDTO> result = cartService.getDetailedCartItems(userId);

            // Assert
            assertThat(result).isEmpty();
            verify(productClient, never()).getProductsByIds(any());
        }

        @Test
        @DisplayName("Should return detailed cart items with product information")
        void shouldReturnDetailedCartItems() {
            // Arrange
            cart.getCartItems().add(cartItem);

            ProductImageDTO primaryImage = new ProductImageDTO(1L, "image.jpg", true, 1);
            ProductResponseDTO productResponse = new ProductResponseDTO(
                    productId,
                    sellerId,
                    "Test Product",
                    "Description",
                    BigDecimal.valueOf(99.99),
                    List.of(primaryImage)
            );

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(productClient.getProductsByIds(anyList()))
                    .thenReturn(ResponseEntity.ok(List.of(productResponse)));

            // Act
            List<DetailedCartItemResponseDTO> result = cartService.getDetailedCartItems(userId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).productId()).isEqualTo(productId);
            assertThat(result.get(0).quantity()).isEqualTo(2);
            assertThat(result.get(0).title()).isEqualTo("Test Product");
            assertThat(result.get(0).price()).isEqualTo(BigDecimal.valueOf(99.99));
            assertThat(result.get(0).primaryImage()).isEqualTo("image.jpg");
            verify(productClient).getProductsByIds(anyList());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Arrange
            cart.getCartItems().add(cartItem);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(productClient.getProductsByIds(anyList()))
                    .thenReturn(ResponseEntity.ok(Collections.emptyList()));

            // Act & Assert
            assertThatThrownBy(() -> cartService.getDetailedCartItems(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should use first image when no primary image exists")
        void shouldUseFirstImageWhenNoPrimaryExists() {
            // Arrange
            cart.getCartItems().add(cartItem);

            ProductImageDTO nonPrimaryImage1 = new ProductImageDTO(1L, "fallback.jpg", false, 1);
            ProductImageDTO nonPrimaryImage2 = new ProductImageDTO(2L, "second.jpg", false, 2);
            ProductResponseDTO productResponse = new ProductResponseDTO(
                    productId,
                    sellerId,
                    "Test Product",
                    "Description",
                    BigDecimal.valueOf(99.99),
                    List.of(nonPrimaryImage1, nonPrimaryImage2)
            );

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(productClient.getProductsByIds(anyList()))
                    .thenReturn(ResponseEntity.ok(List.of(productResponse)));

            // Act
            List<DetailedCartItemResponseDTO> result = cartService.getDetailedCartItems(userId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).primaryImage()).isEqualTo("fallback.jpg");
        }

        @Test
        @DisplayName("Should return null image when product has no images")
        void shouldReturnNullImageWhenProductHasNoImages() {
            // Arrange
            cart.getCartItems().add(cartItem);

            ProductResponseDTO productResponse = new ProductResponseDTO(
                    productId,
                    sellerId,
                    "Test Product",
                    "Description",
                    BigDecimal.valueOf(99.99),
                    Collections.emptyList()
            );

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(productClient.getProductsByIds(anyList()))
                    .thenReturn(ResponseEntity.ok(List.of(productResponse)));

            // Act
            List<DetailedCartItemResponseDTO> result = cartService.getDetailedCartItems(userId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).primaryImage()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple cart items with different products")
        void shouldHandleMultipleCartItems() {
            // Arrange
            UUID productId2 = UUID.randomUUID();
            CartItem cartItem2 = new CartItem();
            cartItem2.setCart(cart);
            cartItem2.setProductId(productId2);
            cartItem2.setQuantity(1);

            cart.getCartItems().add(cartItem);
            cart.getCartItems().add(cartItem2);

            ProductImageDTO image1 = new ProductImageDTO(1L, "image1.jpg", true, 1);
            ProductImageDTO image2 = new ProductImageDTO(2L, "image2.jpg", true, 1);

            ProductResponseDTO product1 = new ProductResponseDTO(
                    productId,
                    sellerId,
                    "Product 1",
                    "Description 1",
                    BigDecimal.valueOf(99.99),
                    List.of(image1)
            );

            ProductResponseDTO product2 = new ProductResponseDTO(
                    productId2,
                    sellerId,
                    "Product 2",
                    "Description 2",
                    BigDecimal.valueOf(149.99),
                    List.of(image2)
            );

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(productClient.getProductsByIds(anyList()))
                    .thenReturn(ResponseEntity.ok(List.of(product1, product2)));

            // Act
            List<DetailedCartItemResponseDTO> result = cartService.getDetailedCartItems(userId);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(productId);
            assertThat(result.get(0).quantity()).isEqualTo(2);
            assertThat(result.get(1).productId()).isEqualTo(productId2);
            assertThat(result.get(1).quantity()).isEqualTo(1);
        }
    }
}