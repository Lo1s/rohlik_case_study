package com.rohlik.case_study.repository;

import com.rohlik.case_study.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Find orders that are unpaid, not canceled, and created before a given expiration time.
     * This is used by a scheduled job to expire orders older than 30 minutes.
     */
    @Query("SELECT o FROM Order o WHERE o.paid = false AND o.canceled = false AND o.createdAt < :expirationTime")
    List<Order> findExpiredOrders(LocalDateTime expirationTime);
}
