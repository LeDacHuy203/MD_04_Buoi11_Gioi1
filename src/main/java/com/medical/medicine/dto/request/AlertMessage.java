package com.medical.medicine.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AlertMessage(
        String topicListen,

        @NotBlank(message = "Loai thong bao (type) khong duoc de trong")
        String type,

        @NotBlank(message = "Noi dung (message) khong duoc de trong")
        String message
) {
}
