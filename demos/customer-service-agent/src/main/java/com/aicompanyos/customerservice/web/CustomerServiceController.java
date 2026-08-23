package com.aicompanyos.customerservice.web;

import com.aicompanyos.customerservice.agent.AgentResult;
import com.aicompanyos.customerservice.agent.CustomerSupportAgent;
import com.aicompanyos.customerservice.tool.CustomerSupportTools;
import com.aicompanyos.customerservice.tool.OrderSnapshot;
import com.aicompanyos.customerservice.tool.TicketSnapshot;
import com.aicompanyos.customerservice.web.dto.ChatRequest;
import com.aicompanyos.customerservice.web.dto.CreateTicketRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CustomerServiceController {
    private final CustomerIdentity identity;
    private final CustomerSupportTools tools;
    private final CustomerSupportAgent agent;

    public CustomerServiceController(CustomerIdentity identity, CustomerSupportTools tools, CustomerSupportAgent agent) {
        this.identity = identity;
        this.tools = tools;
        this.agent = agent;
    }

    @GetMapping("/orders/{orderId}")
    public OrderSnapshot getOrder(@RequestHeader(value = "X-Customer-Id", required = false) String customerId, @PathVariable String orderId) {
        return tools.getOrder(identity.require(customerId), orderId);
    }

    @PostMapping("/tickets")
    public ResponseEntity<TicketSnapshot> createTicket(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerId,
            @Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(201).body(tools.createTicket(identity.require(customerId), request.reason()));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerId,
            @Valid @RequestBody ChatRequest request) {
        AgentResult result = agent.respond(identity.require(customerId), request.sessionId(), request.message());
        return ResponseEntity.status(result.status()).body(result.reply());
    }
}
