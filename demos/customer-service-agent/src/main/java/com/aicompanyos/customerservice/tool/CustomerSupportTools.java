package com.aicompanyos.customerservice.tool;

import com.aicompanyos.customerservice.order.OrderService;
import com.aicompanyos.customerservice.ticket.TicketService;
import org.springframework.stereotype.Service;

/**
 * The only business-tool boundary exposed to the agent layer.
 * A future Spring AI/MCP adapter must call this service rather than repositories directly.
 */
@Service
public class CustomerSupportTools {
    private final OrderService orders;
    private final TicketService tickets;

    public CustomerSupportTools(OrderService orders, TicketService tickets) {
        this.orders = orders;
        this.tickets = tickets;
    }

    public OrderSnapshot getOrder(String customerId, String orderId) {
        return orders.getForCustomer(customerId, orderId);
    }

    public TicketSnapshot createTicket(String customerId, String reason) {
        return tickets.create(customerId, reason);
    }
}
