package com.medical.medicine.dto.response;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicineResponse(
        Long id,
        String name,
        String code,
        String category,
        BigDecimal price,
        String dosage,
        String description,
        String manufacturer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
