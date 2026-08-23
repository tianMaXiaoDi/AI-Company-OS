package com.aicompanyos.customerservice.order;

import com.aicompanyos.customerservice.tool.OrderSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public OrderSnapshot getForCustomer(String customerId, String orderId) {
        Order order = orders.findById(orderId).orElseThrow(OrderNotFoundException::new);
        if (!order.customerId().equals(customerId)) {
            throw new OrderNotAvailableException();
        }
        return new OrderSnapshot(order.id(), order.product(), order.status(), order.carrier(), order.trackingNumber(), order.estimatedDelivery());
    }
}
