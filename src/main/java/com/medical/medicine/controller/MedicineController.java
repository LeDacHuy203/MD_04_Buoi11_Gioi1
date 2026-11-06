package com.medical.medicine.controller;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.ApiResponse;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.mapper.MedicineMapper;
import com.medical.medicine.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Management", description = "APIs quan ly va truy van danh muc thuoc voi Redis Caching & TTL")
public class MedicineController {

    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;

    /**
     * Endpoint lay chi tiet thuoc theo ID
     * Yeu cau bai tap:
     * - Lan 1: Query database -> Log hien SQL -> Cache vao Redis (TTL 60 phut).
     * - Lan 2: Lay truc tiep tu Redis -> Khong co SQL log -> Toc do phan hoi sieu nhanh (<10ms).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lay chi tiet thuoc theo ID (co Caching Redis voi TTL 60 phut)")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineById(@PathVariable Long id) {
        MedicineResponse medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(medicine, "Lay thong tin thuoc thanh cong"));
    }

    @GetMapping
    @Operation(summary = "Lay danh sach tat ca cac thuoc trong danh muc")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAllMedicines() {
        List<MedicineResponse> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines, "Lay danh sach thuoc thanh cong"));
    }

    @PostMapping
    @Operation(summary = "Them moi mot loai thuoc vao danh muc")
    public ResponseEntity<ApiResponse<MedicineResponse>> createMedicine(
            @Valid @RequestBody MedicineRequest request) {
        MedicineResponse created = medicineService.createMedicine(request);
        return new ResponseEntity<>(
                ApiResponse.success(created, "Tao moi thuoc thanh cong"),
                HttpStatus.CREATED
        );
    }

    /**
     * Endpoint cap nhat thong tin/gia thuoc
     * Yeu cau bai tap:
     * - Khi mot loai thuoc duoc sua gia, Cache cu bi xoa ngay lap tuc khoi Redis (@CacheEvict key = "#medicine.id")
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cap nhat thong tin/gia thuoc (Xoa cache cu khoi Redis)")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {
        Medicine medicine = medicineMapper.toEntity(request);
        medicine.setId(id);
        MedicineResponse updated = medicineService.updateMedicine(medicine);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cap nhat thuoc va xoa cache cu thanh cong"));
    }

    @PostMapping("/{id}/import")
    @Operation(summary = "Nhap hang thuoc va phat thong bao Dashboard (Redis Pub/Sub)")
    public ResponseEntity<ApiResponse<MedicineResponse>> importMedicine(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") int quantity) {
        MedicineResponse medicine = medicineService.importMedicine(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(medicine, "Nhap hang thanh cong, da phat thong bao Dashboard"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoa thuoc theo ID (xoa cache Redis tuong ung)")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xoa thuoc thanh cong"));
    }
}
