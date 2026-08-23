package com.aicompanyos.customerservice.web;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Development-only identity adapter. It must never be enabled in a production deployment. */
@Component
@Profile("dev")
public class DevelopmentHeaderCustomerIdentity implements CustomerIdentity {
    @Override
    public String require(String customerIdHeader) {
        if (customerIdHeader == null || customerIdHeader.isBlank()) {
            throw new MissingCustomerIdentityException();
        }
        return customerIdHeader.trim();
    }
}
