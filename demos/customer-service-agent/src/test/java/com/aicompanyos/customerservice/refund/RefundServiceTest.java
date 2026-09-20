package com.aicompanyos.customerservice.refund;

import java.time.Instant;
import java.util.Optional;

import com.aicompanyos.customerservice.order.OrderService;
import com.aicompanyos.customerservice.tool.OrderSnapshot;
import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {
    @Mock private OrderService orders;
    @Mock private RefundCaseRepository refunds;

    @Test
    void verifiesOrderOwnershipBeforeReturningCustomerSafeRefundFacts() {
        RefundCase refund = new RefundCase("RF-10001", "ORD-10086", RefundStatus.PENDING_MANUAL_REVIEW,
                "PAYMENT_RISK_REVIEW", "Payment channel requires manual risk review.",
                "WAIT_FOR_OPERATIONS_REVIEW", Instant.parse("2026-08-24T12:30:00Z"));
        when(orders.getForCustomer("CUST-1001", "ORD-10086"))
                .thenReturn(new OrderSnapshot("ORD-10086", "MacBook Case", "SHIPPED", "DHL", "DHL123456", "2026-09-02"));
        when(refunds.findByOrderId("ORD-10086")).thenReturn(Optional.of(refund));

        RefundStatusSnapshot actual = new RefundService(orders, refunds).getStatusForCustomer("CUST-1001", "ORD-10086");

        assertThat(actual.refundId()).isEqualTo("RF-10001");
        assertThat(actual.status()).isEqualTo(RefundStatus.PENDING_MANUAL_REVIEW);
        assertThat(actual.reasonCode()).isEqualTo("PAYMENT_RISK_REVIEW");
        verify(orders).getForCustomer("CUST-1001", "ORD-10086");
        verify(refunds).findByOrderId("ORD-10086");
    }
}
