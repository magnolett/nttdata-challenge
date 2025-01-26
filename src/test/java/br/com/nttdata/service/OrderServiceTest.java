package br.com.nttdata.service;

import br.com.nttdata.dto.OrderDTO;
import br.com.nttdata.entity.Order;
import br.com.nttdata.exception.OrderNotFoundException;
import br.com.nttdata.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessOrder_savesOrderSuccessfully() {
        OrderDTO.Product product = new OrderDTO.Product("Product1", 10.0, 2);
        OrderDTO orderDTO = new OrderDTO("order123", List.of(product));

        ValueOperations<String, String> valueOperationsMock = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
        when(redisTemplate.hasKey(orderDTO.getOrderId())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.processOrder(orderDTO);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(valueOperationsMock, times(1)).set(eq(orderDTO.getOrderId()), eq("processed"), eq(Duration.ofDays(1)));
    }

    @Test
    void testProcessOrder_whenDuplicate_doesNotSaveOrder() {
        OrderDTO.Product product = new OrderDTO.Product("Product1", 10.0, 2);
        OrderDTO orderDTO = new OrderDTO("order123", List.of(product));

        ValueOperations<String, String> valueOperationsMock = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
        when(redisTemplate.hasKey(orderDTO.getOrderId())).thenReturn(true);

        orderService.processOrder(orderDTO);

        verify(orderRepository, never()).save(any(Order.class));
        verify(valueOperationsMock, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testIsDuplicate_whenOrderExistsInCache_returnsTrue() {
        String orderId = "order123";
        when(redisTemplate.hasKey(orderId)).thenReturn(true);

        boolean result = orderService.isDuplicate(orderId);

        assertTrue(result, "Expected isDuplicate to return true when the order exists in cache.");
    }

    @Test
    void testIsDuplicate_whenOrderDoesNotExistInCache_returnsFalse() {
        String orderId = "order123";
        when(redisTemplate.hasKey(orderId)).thenReturn(false);

        boolean result = orderService.isDuplicate(orderId);

        assertFalse(result, "Expected isDuplicate to return false when the order does not exist in cache.");
    }

    @Test
    void testIsDuplicate_whenRedisTemplateIsNull_returnsFalse() {
        String orderId = "order123";
        OrderService serviceWithNullRedisTemplate = new OrderService(orderRepository, null);

        boolean result = serviceWithNullRedisTemplate.isDuplicate(orderId);

        assertFalse(result, "Expected isDuplicate to return false when RedisTemplate is null.");
    }

    @Test
    void testFindOrderById_whenOrderExists_returnsOrder() {
        String orderId = "order123";
        Order order = new Order(orderId, null, 20.0, "processed", LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderById(orderId);

        assertNotNull(result, "Expected order to be not null.");
        assertEquals(orderId, result.getOrderId(), "Expected order ID to match.");
    }

    @Test
    void testFindOrderById_whenOrderDoesNotExist_throwsException() {
        String orderId = "order123";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findOrderById(orderId), "Expected OrderNotFoundException to be thrown.");
    }

    @Test
    void testFindAllOrders_returnsPagedOrders() {
        Pageable pageable = mock(Pageable.class);
        Order order1 = new Order("order1", null, 20.0, "processed", LocalDateTime.now(), LocalDateTime.now());
        Order order2 = new Order("order2", null, 40.0, "processed", LocalDateTime.now(), LocalDateTime.now());
        Page<Order> page = new PageImpl<>(List.of(order1, order2));

        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<Order> result = orderService.findAllOrders(pageable);

        assertNotNull(result, "Expected result to be not null.");
        assertEquals(2, result.getContent().size(), "Expected page size to match.");
        verify(orderRepository, times(1)).findAll(pageable);
    }
}