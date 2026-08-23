package com.aicompanyos.customerservice.web;

import java.util.Map;

import com.aicompanyos.customerservice.order.OrderNotAvailableException;
import com.aicompanyos.customerservice.order.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MissingCustomerIdentityException.class)
    public ResponseEntity<Map<String, Object>> missingCustomer(MissingCustomerIdentityException exception) {
        return error(HttpStatus.UNAUTHORIZED, "CUSTOMER_ID_REQUIRED", "An authenticated customer identity is required.");
    }

    @ExceptionHandler(CustomerIdentityAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> deniedCustomer(CustomerIdentityAccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "CUSTOMER_ACCESS_DENIED", "The token does not grant customer support access.");
    }

    @ExceptionHandler(OrderNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> deniedOrder(OrderNotAvailableException exception) {
        return error(HttpStatus.FORBIDDEN, "ORDER_NOT_AVAILABLE", "This order is not available to the current customer.");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> missingOrder(OrderNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("error", Map.of("code", code, "message", message)));
    }
}
