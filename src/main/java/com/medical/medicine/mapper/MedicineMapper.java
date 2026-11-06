package com.medical.medicine.mapper;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import org.springframework.stereotype.Component;

@Component
public class MedicineMapper {

    public MedicineResponse toResponse(Medicine entity) {
        if (entity == null) {
            return null;
        }
        return new MedicineResponse(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getDosage(),
                entity.getDescription(),
                entity.getManufacturer(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Medicine toEntity(MedicineRequest request) {
        if (request == null) {
            return null;
        }
        return Medicine.builder()
                .name(request.name())
                .code(request.code())
                .category(request.category())
                .price(request.price())
                .dosage(request.dosage())
                .description(request.description())
                .manufacturer(request.manufacturer())
                .build();
    }

    public void updateEntityFromRequest(Medicine entity, MedicineRequest request) {
        entity.setName(request.name());
        entity.setCode(request.code());
        entity.setCategory(request.category());
        entity.setPrice(request.price());
        entity.setDosage(request.dosage());
        entity.setDescription(request.description());
        entity.setManufacturer(request.manufacturer());
    }
}
