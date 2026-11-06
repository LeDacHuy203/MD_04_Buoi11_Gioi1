package com.medical.medicine;

import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.repository.MedicineRepository;
import com.medical.medicine.service.MedicineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class MedicineCacheIntegrationTest {

    @Autowired
    private MedicineService medicineService;

    @SpyBean
    private MedicineRepository medicineRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Kiem chung Cache-aside: Lan 1 query DB, Lan 2 lay truc tiep tu Redis Cache")
    void testCacheAsidePattern() {
        // Clear cache "medicines" truoc khi test
        if (cacheManager.getCache("medicines") != null) {
            cacheManager.getCache("medicines").clear();
        }

        // Lan 1: Query lan dau -> Goi xuong Database (findById duoc goi 1 lan)
        MedicineResponse firstCall = medicineService.getMedicineById(1L);
        assertThat(firstCall).isNotNull();
        assertThat(firstCall.id()).isEqualTo(1L);
        verify(medicineRepository, times(1)).findById(1L);

        // Kiem tra gia tri da ton tai trong Cache Manager
        assertThat(cacheManager.getCache("medicines")).isNotNull();
        assertThat(cacheManager.getCache("medicines").get(1L)).isNotNull();

        // Lan 2: Query lan tiep theo voi cung ID -> Lay tu Redis Cache -> Repository findById khong bi goi them lan nao
        MedicineResponse secondCall = medicineService.getMedicineById(1L);
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.name()).isEqualTo(firstCall.name());

        // Tong so lan goi findById van chi la 1 lan duy nhat!
        verify(medicineRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Kiem chung Cache Eviction & TTL: Sua gia thuoc thi Cache cu phai bi xoa ngay lap tuc")
    void testCacheEvictionOnUpdatePrice() {
        if (cacheManager.getCache("medicines") != null) {
            cacheManager.getCache("medicines").clear();
        }

        // Buoc 1: Goi lan 1 lay thuoc ID=1 -> Cache vao Redis
        MedicineResponse res1 = medicineService.getMedicineById(1L);
        assertThat(res1).isNotNull();
        assertThat(cacheManager.getCache("medicines").get(1L)).isNotNull();

        // Buoc 2: Goi lan 2 -> Khong query DB, lay tu Cache
        MedicineResponse res2 = medicineService.getMedicineById(1L);
        assertThat(res2).isNotNull();

        // Buoc 3 (Chot ha): Goi Update gia thuoc ID=1 -> @CacheEvict key="#medicine.id" xoa cache
        Medicine updateObj = Medicine.builder()
                .id(1L)
                .name(res1.name())
                .code(res1.code())
                .category(res1.category())
                .price(new BigDecimal("35000.00")) // Sua gia moi
                .dosage(res1.dosage())
                .description(res1.description())
                .manufacturer(res1.manufacturer())
                .build();

        medicineService.updateMedicine(updateObj);

        // Kiem tra cache key 'medicines::1' da bi xoa khoi Redis!
        assertThat(cacheManager.getCache("medicines").get(1L)).isNull();

        // Buoc 4: Goi lai API lay thuoc ID=1 -> He thong phai query DB lai va lay gia moi
        MedicineResponse resAfterUpdate = medicineService.getMedicineById(1L);
        assertThat(resAfterUpdate).isNotNull();
        assertThat(resAfterUpdate.price()).isEqualByComparingTo(new BigDecimal("35000.00"));

        // Cache da duoc nap lai gia tri moi
        assertThat(cacheManager.getCache("medicines").get(1L)).isNotNull();
    }
}
