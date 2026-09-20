package com.aicompanyos.customerservice.tool;

import com.aicompanyos.customerservice.order.OrderService;
import com.aicompanyos.customerservice.refund.RefundService;
import com.aicompanyos.customerservice.ticket.TicketService;
import org.springframework.stereotype.Service;

/**
 * The only business-tool boundary exposed to the agent layer.
 * A future Spring AI/MCP adapter must call this service rather than repositories directly.
 */
@Service
public class CustomerSupportTools {
    private final OrderService orders;
    private final RefundService refunds;
    private final TicketService tickets;

    public CustomerSupportTools(OrderService orders, RefundService refunds, TicketService tickets) {
        this.orders = orders;
        this.refunds = refunds;
        this.tickets = tickets;
    }

    public OrderSnapshot getOrder(String customerId, String orderId) {
        return orders.getForCustomer(customerId, orderId);
    }

    public RefundStatusSnapshot getRefundStatus(String customerId, String orderId) {
        return refunds.getStatusForCustomer(customerId, orderId);
    }

    public TicketSnapshot createTicket(String customerId, String reason) {
        return tickets.create(customerId, reason);
    }
}
