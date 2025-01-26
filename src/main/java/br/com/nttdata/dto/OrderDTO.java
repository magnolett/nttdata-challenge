package br.com.nttdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String orderId;
    private List<Product> products;

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
