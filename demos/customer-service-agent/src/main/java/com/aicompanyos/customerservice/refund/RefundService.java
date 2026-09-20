package com.aicompanyos.customerservice.refund;

import com.aicompanyos.customerservice.order.OrderService;
import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only customer-scoped boundary for refund operations data. It first verifies
 * order ownership and exposes only customer-safe fields from an approved read model.
 */
@Service
public class RefundService {
    private final OrderService orders;
    private final RefundCaseRepository refunds;

    public RefundService(OrderService orders, RefundCaseRepository refunds) {
        this.orders = orders;
        this.refunds = refunds;
    }

    @Transactional(readOnly = true)
    public RefundStatusSnapshot getStatusForCustomer(String customerId, String orderId) {
        orders.getForCustomer(customerId, orderId);
        RefundCase refund = refunds.findByOrderId(orderId).orElseThrow(RefundNotFoundException::new);
        return new RefundStatusSnapshot(refund.id(), refund.orderId(), refund.status(), refund.reasonCode(),
                refund.reasonDescription(), refund.nextAction(), refund.updatedAt());
    }
}
