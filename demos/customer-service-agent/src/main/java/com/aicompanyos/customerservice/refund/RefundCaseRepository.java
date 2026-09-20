package com.aicompanyos.customerservice.refund;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundCaseRepository extends JpaRepository<RefundCase, String> {
    Optional<RefundCase> findByOrderId(String orderId);
}
