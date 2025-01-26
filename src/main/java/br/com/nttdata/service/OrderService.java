package br.com.nttdata.service;

import br.com.nttdata.dto.OrderDTO;
import br.com.nttdata.entity.Order;
import br.com.nttdata.exception.OrderNotFoundException;
import br.com.nttdata.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;

    private RedisTemplate<String, String> redisTemplate;

    public void processOrder(OrderDTO orderDTO) {
        if (isDuplicate(orderDTO.getOrderId()))
            return;

        double total = orderDTO.getProducts().stream()
                .mapToDouble(OrderDTO.Product::getTotalPrice)
                .sum();

        Order order = new Order(
                orderDTO.getOrderId(),
                orderDTO.getProducts().stream().map(p -> new Order.Product(p.getName(), p.getUnitPrice(), p.getQuantity())).toList(),
                total,
                "processed",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        orderRepository.save(order);
        redisTemplate.opsForValue().set(orderDTO.getOrderId(), "processed", Duration.ofDays(1));
    }

    boolean isDuplicate(String orderId) {
        if (redisTemplate == null || orderId == null) {
            return false;
        }
        Boolean result = redisTemplate.hasKey(orderId);
        return Boolean.TRUE.equals(result);
    }

    public Order findOrderById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}