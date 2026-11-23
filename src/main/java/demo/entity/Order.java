package demo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Order {

    @JsonCreator
    public Order() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> orderDetails = new ArrayList<>();

    public void addDetail(OrderDetails detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
        recalculate();
    }

    public void removeDetail(OrderDetails detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
        recalculate();
    }

    public void recalculate() {
        this.quantity = orderDetails.stream().mapToInt(OrderDetails::getQuantite).sum();
        this.totalPrice = orderDetails.stream()
                .map(detail -> detail.getPrix().multiply(BigDecimal.valueOf(detail.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
