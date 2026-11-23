package demo.repository;

import demo.entity.Client;
import demo.entity.Order;
import demo.entity.OrderDetails;
import demo.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Test
    void shouldPersistOrderGraphAndFindClientByEmail() {
        Client client = clientRepository.save(Client.builder()
                .firstname("Marie")
                .lastname("Dubois")
                .email("marie.dubois@example.com")
                .build());
        Product product = productRepository.save(Product.builder()
                .name("Clavier")
                .price(new BigDecimal("9.99"))
                .build());

        Order order = Order.builder()
                .date(LocalDateTime.of(2026, 1, 15, 10, 30))
                .quantity(0)
                .totalPrice(BigDecimal.ZERO)
                .client(client)
                .build();
        order.addDetail(OrderDetails.builder()
                .prix(product.getPrice())
                .quantite(2)
                .product(product)
                .build());

        Order saved = orderRepository.saveAndFlush(order);

        assertThat(clientRepository.findByEmail("marie.dubois@example.com")).isPresent();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("19.98"));
        assertThat(orderDetailsRepository.findByOrderId(saved.getId())).hasSize(1);
    }
}
