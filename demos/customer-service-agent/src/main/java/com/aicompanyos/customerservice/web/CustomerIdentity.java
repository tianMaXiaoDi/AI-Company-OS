package com.aicompanyos.customerservice.web;

/** Resolves a customer identity only after the active security profile has authenticated it. */
public interface CustomerIdentity {
    String require(String customerIdHeader);
}
