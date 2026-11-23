package demo.controller;

import demo.entity.OrderDetails;
import demo.services.OrderDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
public class OrderDetailsController {

    private final OrderDetailsService orderDetailsService;

    @GetMapping
    public List<OrderDetails> findAll() {
        return orderDetailsService.findAll();
    }

    @GetMapping("/order/{orderId}")
    public List<OrderDetails> findByOrderId(@PathVariable Long orderId) {
        return orderDetailsService.findByOrderId(orderId);
    }

    @GetMapping("/{id}")
    public OrderDetails findById(@PathVariable Long id) {
        return orderDetailsService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OrderDetails> create(@Valid @RequestBody OrderDetails orderDetails) {
        OrderDetails created = orderDetailsService.create(orderDetails);
        return ResponseEntity.created(URI.create("/api/order-details/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public OrderDetails update(@PathVariable Long id, @Valid @RequestBody OrderDetails orderDetails) {
        return orderDetailsService.update(id, orderDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderDetailsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
