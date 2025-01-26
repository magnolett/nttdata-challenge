package br.com.nttdata.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String orderId;

    private List<Product> products;

    private double total;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String name;
        private double unitPrice;
        private int quantity;

        public double getTotalPrice() {
            return unitPrice * quantity;
        }
    }
}
