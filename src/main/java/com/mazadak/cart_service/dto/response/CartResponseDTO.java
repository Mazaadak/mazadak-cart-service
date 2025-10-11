/**
 * DTO for representing a shopping cart in responses.
 * @param cartId The unique identifier of the cart
 * @param userId The ID of the user who owns the cart
 * @param cartItems List of items in the cart
 */
public record CartResponseDTO(
    UUID cartId,
    UUID userId,
    List<CartItemResponse> cartItems
) {
    public CartResponseDTO {
        if (cartItems == null) {
            cartItems = List.of();
        }
    }
}
