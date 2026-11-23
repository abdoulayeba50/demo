package demo.services;

import demo.entity.Client;
import demo.entity.Order;
import demo.repository.ClientRepository;
import demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }

    public Order findById(Long id) {
        return findEntity(id);
    }

    @Transactional
    public Order create(Order order) {
        Client client = findClient(order.getClient());
        order.setId(null);
        order.setDate(order.getDate() == null ? LocalDateTime.now() : order.getDate());
        order.setClient(client);
        order.setQuantity(0);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setOrderDetails(new ArrayList<>());
        return orderRepository.save(order);
    }

    @Transactional
    public Order update(Long id, Order order) {
        Order existing = findEntity(id);
        existing.setClient(findClient(order.getClient()));
        if (order.getDate() != null) {
            existing.setDate(order.getDate());
        }
        return orderRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.delete(findEntity(id));
    }

    private Order findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable : " + id));
    }

    private Client findClient(Client client) {
        if (client == null || client.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client requis");
        }
        return clientRepository.findById(client.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Client introuvable : " + client.getId()));
    }
}
