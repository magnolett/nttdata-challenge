package br.com.nttdata.controller;

import br.com.nttdata.dto.OrderDTO;
import br.com.nttdata.entity.Order;
import br.com.nttdata.repository.OrderRepository;
import br.com.nttdata.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Void> receiveOrder(@RequestBody OrderDTO order) {
        orderService.processOrder(order);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.findAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.findOrderById(id);
        return ResponseEntity.ok(order);
    }
}
