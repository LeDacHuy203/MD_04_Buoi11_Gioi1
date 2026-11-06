package com.medical.medicine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.exception.ResourceNotFoundException;
import com.medical.medicine.mapper.MedicineMapper;
import com.medical.medicine.service.MedicineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicineController.class)
class MedicineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicineService medicineService;

    @SpyBean
    private MedicineMapper medicineMapper;

    @Test
    @DisplayName("GET /api/v1/medicines/{id} - Tra ve 200 OK va du lieu thuoc")
    void getMedicineById_Success() throws Exception {
        MedicineResponse response = new MedicineResponse(
                1L,
                "Panadol Extra",
                "MED-001",
                "Giam dau - Ha sot",
                new BigDecimal("25000.00"),
                "Paracetamol 500mg, Caffeine 65mg",
                "Thuoc giam dau ha sot",
                "GSK",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(medicineService.getMedicineById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/medicines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Panadol Extra"))
                .andExpect(jsonPath("$.data.code").value("MED-001"));
    }

    @Test
    @DisplayName("PUT /api/v1/medicines/{id} - Cap nhat gia thuoc thanh cong")
    void updateMedicine_Success() throws Exception {
        MedicineRequest request = new MedicineRequest(
                "Panadol Extra",
                "MED-001",
                "Giam dau - Ha sot",
                new BigDecimal("35000.00"), // Gia moi
                "Paracetamol 500mg, Caffeine 65mg",
                "Thuoc giam dau ha sot",
                "GSK"
        );

        MedicineResponse response = new MedicineResponse(
                1L,
                request.name(),
                request.code(),
                request.category(),
                request.price(),
                request.dosage(),
                request.description(),
                request.manufacturer(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(medicineService.updateMedicine(any(Medicine.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/medicines/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.price").value(35000.00));
    }

    @Test
    @DisplayName("GET /api/v1/medicines/{id} - Tra ve 404 NOT FOUND khi khong tim thay thuoc")
    void getMedicineById_NotFound() throws Exception {
        when(medicineService.getMedicineById(99L))
                .thenThrow(new ResourceNotFoundException("Khong tim thay thuoc voi ID: 99"));

        mockMvc.perform(get("/api/v1/medicines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Khong tim thay thuoc voi ID: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/medicines - Tra ve 201 CREATED khi du lieu hop le")
    void createMedicine_Success() throws Exception {
        MedicineRequest request = new MedicineRequest(
                "Amoxicillin 500mg",
                "MED-003",
                "Khang sinh",
                new BigDecimal("45000.00"),
                "Amoxicillin 500mg",
                "Khang sinh duong ho hap",
                "Imexpharm"
        );

        MedicineResponse response = new MedicineResponse(
                3L,
                request.name(),
                request.code(),
                request.category(),
                request.price(),
                request.dosage(),
                request.description(),
                request.manufacturer(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(medicineService.createMedicine(any(MedicineRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("MED-003"));
    }

    @Test
    @DisplayName("POST /api/v1/medicines - Tra ve 400 BAD REQUEST khi validate that bai")
    void createMedicine_ValidationError() throws Exception {
        MedicineRequest invalidRequest = new MedicineRequest(
                "",
                "MED-999",
                "Khang sinh",
                new BigDecimal("-100"),
                "Dosage",
                "Desc",
                "Manufacturer"
        );

        mockMvc.perform(post("/api/v1/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }

    @Test
    @DisplayName("GET /api/v1/medicines - Tra ve danh sach tat ca thuoc")
    void getAllMedicines_Success() throws Exception {
        MedicineResponse response = new MedicineResponse(
                1L,
                "Panadol Extra",
                "MED-001",
                "Giam dau - Ha sot",
                new BigDecimal("25000.00"),
                "Paracetamol 500mg, Caffeine 65mg",
                "Thuoc giam dau ha sot",
                "GSK",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(medicineService.getAllMedicines()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Panadol Extra"));
    }
}
