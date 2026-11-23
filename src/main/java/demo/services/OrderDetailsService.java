package demo.services;

import demo.entity.Order;
import demo.entity.OrderDetails;
import demo.entity.Product;
import demo.repository.OrderDetailsRepository;
import demo.repository.OrderRepository;
import demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<OrderDetails> findAll() {
        return orderDetailsRepository.findAll();
    }

    public List<OrderDetails> findByOrderId(Long orderId) {
        return orderDetailsRepository.findByOrderId(orderId);
    }

    public OrderDetails findById(Long id) {
        return findEntity(id);
    }

    @Transactional
    public OrderDetails create(OrderDetails orderDetails) {
        Order order = findOrder(orderDetails.getOrder());
        Product product = findProduct(orderDetails.getProduct());
        OrderDetails detail = OrderDetails.builder()
                .prix(orderDetails.getPrix())
                .quantite(orderDetails.getQuantite())
                .order(order)
                .product(product)
                .build();
        order.addDetail(detail);
        return orderDetailsRepository.saveAndFlush(detail);
    }

    @Transactional
    public OrderDetails update(Long id, OrderDetails orderDetails) {
        OrderDetails existing = findEntity(id);
        existing.setPrix(orderDetails.getPrix());
        existing.setQuantite(orderDetails.getQuantite());
        existing.setProduct(findProduct(orderDetails.getProduct()));
        Order order = existing.getOrder();
        order.recalculate();
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        OrderDetails detail = findEntity(id);
        Order order = detail.getOrder();
        order.removeDetail(detail);
    }

    private OrderDetails findEntity(Long id) {
        return orderDetailsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Détail de commande introuvable : " + id));
    }

    private Order findOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commande requise");
        }
        return orderRepository.findById(order.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Commande introuvable : " + order.getId()));
    }

    private Product findProduct(Product product) {
        if (product == null || product.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit requis");
        }
        return productRepository.findById(product.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produit introuvable : " + product.getId()));
    }
}
