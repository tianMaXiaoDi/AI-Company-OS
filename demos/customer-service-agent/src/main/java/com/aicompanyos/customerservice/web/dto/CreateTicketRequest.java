package com.aicompanyos.customerservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(@NotBlank @Size(max = 1000) String reason) {
}
