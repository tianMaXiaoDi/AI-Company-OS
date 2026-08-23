package com.aicompanyos.customerservice.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtCustomerIdentityTest {
    private final JwtCustomerIdentity identity = new JwtCustomerIdentity(
            new CustomerIdentityProperties("customer_id", "SCOPE_customer.support"));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesTheVerifiedTokenClaimAndIgnoresTheHeader() {
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication("CUST-1001", "SCOPE_customer.support"));

        assertThat(identity.require("CUST-OTHER")).isEqualTo("CUST-1001");
    }

    @Test
    void rejectsAnAuthenticatedTokenWithoutTheRequiredAuthority() {
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication("CUST-1001", "SCOPE_other"));

        assertThatThrownBy(() -> identity.require("CUST-1001"))
                .isInstanceOf(CustomerIdentityAccessDeniedException.class);
    }

    @Test
    void rejectsNonJwtAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("customer", "n/a"));

        assertThatThrownBy(() -> identity.require("CUST-1001"))
                .isInstanceOf(MissingCustomerIdentityException.class);
    }

    private JwtAuthenticationToken jwtAuthentication(String customerId, String authority) {
        Instant issuedAt = Instant.now();
        Jwt jwt = new Jwt("test-token", issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"),
                Map.of("customer_id", customerId));
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(authority)));
    }
}
