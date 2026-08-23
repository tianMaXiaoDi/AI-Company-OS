package com.aicompanyos.customerservice.web;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Production identity adapter. The request header is deliberately ignored in the jwt profile. */
@Component
@Profile("jwt")
public class JwtCustomerIdentity implements CustomerIdentity {
    private final CustomerIdentityProperties properties;

    public JwtCustomerIdentity(CustomerIdentityProperties properties) {
        this.properties = properties;
    }

    @Override
    public String require(String ignoredCustomerIdHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            throw new MissingCustomerIdentityException();
        }
        boolean authorityGranted = authentication.getAuthorities().stream()
                .anyMatch(authority -> properties.requiredAuthority().equals(authority.getAuthority()));
        if (!authorityGranted) {
            throw new CustomerIdentityAccessDeniedException();
        }
        String customerId = jwtAuthentication.getToken().getClaimAsString(properties.customerIdClaim());
        if (customerId == null || customerId.isBlank()) {
            throw new MissingCustomerIdentityException();
        }
        return customerId.trim();
    }
}
