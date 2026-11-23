package demo.services;

import demo.entity.Client;
import demo.entity.Order;
import demo.entity.OrderDetails;
import demo.entity.Product;
import demo.repository.OrderDetailsRepository;
import demo.repository.OrderRepository;
import demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailsServiceTest {

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderDetailsService orderDetailsService;

    @Test
    void createShouldRecalculateOrderQuantityAndTotal() {
        Order order = Order.builder()
                .id(1L)
                .date(LocalDateTime.of(2026, 1, 15, 10, 30))
                .quantity(0)
                .totalPrice(BigDecimal.ZERO)
                .client(Client.builder().id(1L).firstname("Marie").lastname("Dubois")
                        .email("marie.dubois@example.com").build())
                .build();
        Product product = Product.builder()
                .id(2L)
                .name("Clavier")
                .price(new BigDecimal("9.99"))
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(orderDetailsRepository.saveAndFlush(any(OrderDetails.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderDetails result = orderDetailsService.create(OrderDetails.builder()
                .prix(new BigDecimal("9.99"))
                .quantite(3)
                .order(Order.builder().id(1L).build())
                .product(Product.builder().id(2L).build())
                .build());

        assertThat(result.getOrder().getQuantity()).isEqualTo(3);
        assertThat(result.getOrder().getTotalPrice()).isEqualByComparingTo(new BigDecimal("29.97"));
        assertThat(result.getProduct().getName()).isEqualTo("Clavier");
    }

    @Test
    void createShouldFailWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderDetailsService.create(OrderDetails.builder()
                .prix(new BigDecimal("9.99"))
                .quantite(1)
                .order(Order.builder().id(99L).build())
                .product(Product.builder().id(2L).build())
                .build()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
