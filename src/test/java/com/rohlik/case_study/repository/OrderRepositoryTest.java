package com.rohlik.case_study.repository;

import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findExpiredOrders_returnsCorrectOrders() {
        Order unpaid = new Order();
        unpaid.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        unpaid.setPaid(false);
        unpaid.setCanceled(false);

        Order paid = new Order();
        paid.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        paid.setPaid(true);
        paid.setCanceled(false);

        orderRepository.save(unpaid);
        orderRepository.save(paid);

        List<Order> expired = orderRepository.findExpiredOrders(LocalDateTime.now().minusMinutes(30));
        assertTrue(expired.stream().anyMatch(o -> o.getId().equals(unpaid.getId())));
        assertFalse(expired.stream().anyMatch(o -> o.getId().equals(paid.getId())));
    }
}