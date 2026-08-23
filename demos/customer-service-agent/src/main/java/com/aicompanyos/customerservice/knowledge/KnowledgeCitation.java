package com.aicompanyos.customerservice.knowledge;

import java.time.LocalDate;

public record KnowledgeCitation(
        String sourceKey,
        String title,
        String sourceUri,
        LocalDate verifiedAt) {
}
