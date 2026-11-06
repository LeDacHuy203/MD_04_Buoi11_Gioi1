package com.medical.medicine.service;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.exception.DuplicateResourceException;
import com.medical.medicine.exception.ResourceNotFoundException;
import com.medical.medicine.mapper.MedicineMapper;
import com.medical.medicine.repository.MedicineRepository;
import com.medical.medicine.service.impl.MedicineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private com.medical.medicine.pubsub.AlertPublisher alertPublisher;

    @Spy
    private MedicineMapper medicineMapper = new MedicineMapper();

    @InjectMocks
    private MedicineServiceImpl medicineService;

    private Medicine sampleMedicine;
    private MedicineRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleMedicine = Medicine.builder()
                .id(1L)
                .name("Panadol Extra")
                .code("MED-001")
                .category("Giam dau - Ha sot")
                .price(new BigDecimal("25000.00"))
                .dosage("Paracetamol 500mg, Caffeine 65mg")
                .description("Thuoc giam dau ha sot")
                .manufacturer("GSK")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new MedicineRequest(
                "Panadol Extra",
                "MED-001",
                "Giam dau - Ha sot",
                new BigDecimal("25000.00"),
                "Paracetamol 500mg, Caffeine 65mg",
                "Thuoc giam dau ha sot",
                "GSK"
        );
    }

    @Test
    @DisplayName("getMedicineById: tra ve MedicineResponse khi tim thay ID")
    void getMedicineById_WhenFound_ReturnsResponse() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));

        MedicineResponse response = medicineService.getMedicineById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Panadol Extra");
        assertThat(response.code()).isEqualTo("MED-001");
        verify(medicineRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getMedicineById: nem ngoai le ResourceNotFoundException khi khong tim thay ID")
    void getMedicineById_WhenNotFound_ThrowsException() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicineService.getMedicineById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(medicineRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("createMedicine: luu va tra ve MedicineResponse khi du lieu hop le")
    void createMedicine_Success() {
        when(medicineRepository.existsByCode("MED-001")).thenReturn(false);
        when(medicineRepository.save(any(Medicine.class))).thenReturn(sampleMedicine);

        MedicineResponse response = medicineService.createMedicine(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("MED-001");
        verify(medicineRepository, times(1)).save(any(Medicine.class));
    }

    @Test
    @DisplayName("createMedicine: nem ngoai le DuplicateResourceException khi ma code bi trung")
    void createMedicine_DuplicateCode_ThrowsException() {
        when(medicineRepository.existsByCode("MED-001")).thenReturn(true);

        assertThatThrownBy(() -> medicineService.createMedicine(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("MED-001");

        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    @DisplayName("updateMedicine: cap nhat gia thuoc thanh cong")
    void updateMedicine_Success() {
        Medicine updatedInput = Medicine.builder()
                .id(1L)
                .name("Panadol Extra")
                .code("MED-001")
                .category("Giam dau - Ha sot")
                .price(new BigDecimal("30000.00")) // Gia moi
                .dosage("Paracetamol 500mg, Caffeine 65mg")
                .description("Thuoc giam dau ha sot")
                .manufacturer("GSK")
                .build();

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(updatedInput);

        MedicineResponse response = medicineService.updateMedicine(updatedInput);

        assertThat(response).isNotNull();
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("30000.00"));
        verify(medicineRepository, times(1)).findById(1L);
        verify(medicineRepository, times(1)).save(any(Medicine.class));
    }

    @Test
    @DisplayName("getAllMedicines: tra ve danh sach day du")
    void getAllMedicines_ReturnsList() {
        when(medicineRepository.findAll()).thenReturn(List.of(sampleMedicine));

        List<MedicineResponse> list = medicineService.getAllMedicines();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).name()).isEqualTo("Panadol Extra");
        verify(medicineRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("importMedicine: nhap hang va gui thong bao Pub/Sub thanh cong")
    void importMedicine_Success() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));

        MedicineResponse response = medicineService.importMedicine(1L, 100);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(alertPublisher, times(1)).publishAlert(any());
    }

    @Test
    @DisplayName("deleteMedicine: xoa thanh cong khi ID ton tai")
    void deleteMedicine_Success() {
        when(medicineRepository.existsById(1L)).thenReturn(true);
        doNothing().when(medicineRepository).deleteById(1L);

        medicineService.deleteMedicine(1L);

        verify(medicineRepository, times(1)).deleteById(1L);
    }
}
