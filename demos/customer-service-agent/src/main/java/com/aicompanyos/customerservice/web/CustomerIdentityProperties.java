package com.aicompanyos.customerservice.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer-service.identity")
public record CustomerIdentityProperties(String customerIdClaim, String requiredAuthority) {
    public CustomerIdentityProperties {
        customerIdClaim = customerIdClaim == null || customerIdClaim.isBlank() ? "customer_id" : customerIdClaim;
        requiredAuthority = requiredAuthority == null || requiredAuthority.isBlank()
                ? "SCOPE_customer.support" : requiredAuthority;
    }
}
