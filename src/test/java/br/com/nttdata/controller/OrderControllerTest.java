package br.com.nttdata.controller;

import br.com.nttdata.dto.OrderDTO;
import br.com.nttdata.entity.Order;
import br.com.nttdata.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = OrderControllerTest.TestConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    static class TestConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        public OrderController orderController(OrderService orderService) {
            return new OrderController(orderService);
        }
    }

    @BeforeEach
    void setUp() {
        reset(orderService);
    }

    @Test
    void testReceiveOrder_success() throws Exception {
        OrderDTO orderDTO = new OrderDTO("order123", List.of(
                new OrderDTO.Product("Product1", 10.0, 2)
        ));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).processOrder(orderDTO);
    }

    @Test
    void testGetOrders_success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> ordersPage = new PageImpl<>(List.of(
                new Order("order123", List.of(), 100.0, "processed", LocalDateTime.now(), LocalDateTime.now())
        ));

        when(orderService.findAllOrders(pageable)).thenReturn(ordersPage);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].orderId").value("order123"));

        verify(orderService, times(1)).findAllOrders(pageable);
    }

    @Test
    void testGetOrderById_success() throws Exception {
        String orderId = "order123";
        Order order = new Order(orderId, List.of(), 100.0, "processed", LocalDateTime.now(), LocalDateTime.now());

        when(orderService.findOrderById(orderId)).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));

        verify(orderService, times(1)).findOrderById(orderId);
    }
}