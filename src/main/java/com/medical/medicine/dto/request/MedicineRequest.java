package com.medical.medicine.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MedicineRequest(
        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(max = 150, message = "Tên thuốc tối đa 150 ký tự")
        String name,

        @NotBlank(message = "Mã thuốc không được để trống")
        @Size(max = 50, message = "Mã thuốc tối đa 50 ký tự")
        String code,

        @NotBlank(message = "Danh mục không được để trống")
        @Size(max = 100, message = "Danh mục tối đa 100 ký tự")
        String category,

        @NotNull(message = "Giá thuốc không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuốc phải lớn hơn 0")
        BigDecimal price,

        @NotBlank(message = "Liều lượng không được để trống")
        @Size(max = 100, message = "Liều lượng tối đa 100 ký tự")
        String dosage,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String description,

        @NotBlank(message = "Nhà sản xuất không được để trống")
        @Size(max = 150, message = "Nhà sản xuất tối đa 150 ký tự")
        String manufacturer
) {
}
