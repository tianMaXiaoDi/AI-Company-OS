package com.aicompanyos.customerservice.ticket;

import java.util.Locale;
import java.util.UUID;

import com.aicompanyos.customerservice.tool.TicketSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {
    private final SupportTicketRepository tickets;

    public TicketService(SupportTicketRepository tickets) {
        this.tickets = tickets;
    }

    @Transactional
    public TicketSnapshot create(String customerId, String reason) {
        String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        SupportTicket ticket = tickets.save(new SupportTicket(ticketId, customerId, reason));
        return new TicketSnapshot(ticket.id(), ticket.status());
    }
}
