package com.medical.medicine.config;

import com.medical.medicine.entity.Medicine;
import com.medical.medicine.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MedicineRepository medicineRepository;

    @Override
    public void run(String... args) {
        if (medicineRepository.count() == 0) {
            log.info("Khoi tao du lieu mau cho danh muc thuoc vao Database...");

            List<Medicine> initialMedicines = List.of(
                    Medicine.builder()
                            .name("Panadol Extra")
                            .code("MED-001")
                            .category("Giam dau - Ha sot")
                            .price(new BigDecimal("25000.00"))
                            .dosage("Paracetamol 500mg, Caffeine 65mg")
                            .description("Thuoc giam dau, ha sot, giam dau dau, dau co va sot sau tiem ngua.")
                            .manufacturer("GlaxoSmithKline (GSK)")
                            .build(),

                    Medicine.builder()
                            .name("Paracetamol 500mg")
                            .code("MED-002")
                            .category("Giam dau - Ha sot")
                            .price(new BigDecimal("15000.00"))
                            .dosage("Paracetamol 500mg")
                            .description("Thuoc ha sot, giam dau nhanh chong.")
                            .manufacturer("Dược Hậu Giang (DHG Pharma)")
                            .build(),

                    Medicine.builder()
                            .name("Amoxicillin 500mg")
                            .code("MED-003")
                            .category("Khang sinh")
                            .price(new BigDecimal("45000.00"))
                            .dosage("Amoxicillin 500mg")
                            .description("Khang sinh dieu tri nhiem khuan duong ho hap, tai mui hong.")
                            .manufacturer("Imexpharm")
                            .build(),

                    Medicine.builder()
                            .name("Decolgen Forte")
                            .code("MED-004")
                            .category("Cam cum")
                            .price(new BigDecimal("22000.00"))
                            .dosage("Paracetamol 500mg, Phenylephrine 10mg, Chlorpheniramine 2mg")
                            .description("Dieu tri cac trieu chung cam cum, so mui, nghet mui.")
                            .manufacturer("United Pharma")
                            .build(),

                    Medicine.builder()
                            .name("Berberin 100mg")
                            .code("MED-005")
                            .category("Tieu hoa")
                            .price(new BigDecimal("18000.00"))
                            .dosage("Berberin Clorid 100mg")
                            .description("Dieu tri cac benh tieu chay, viem ruot, kiet ly.")
                            .manufacturer("Dược Hà Tĩnh (Hadiphar)")
                            .build(),

                    Medicine.builder()
                            .name("Omeprazole 20mg")
                            .code("MED-006")
                            .category("Da day")
                            .price(new BigDecimal("35000.00"))
                            .dosage("Omeprazole 20mg")
                            .description("Dieu tri loet da day, ta trang, trao nguoc da day thuc quan.")
                            .manufacturer("Domesco")
                            .build(),

                    Medicine.builder()
                            .name("Cetirizine 10mg")
                            .code("MED-007")
                            .category("Chong di ung")
                            .price(new BigDecimal("20000.00"))
                            .dosage("Cetirizine Hydrochloride 10mg")
                            .description("Khang histamin, giam di ung, may day, viem mui di ung.")
                            .manufacturer("Stada")
                            .build()
            );

            medicineRepository.saveAll(initialMedicines);
            log.info("Khoi tao thanh cong {} ban ghi thuoc vao Database!", initialMedicines.size());
        } else {
            log.info("Database da co {} ban ghi thuoc, bo qua khoi tao.", medicineRepository.count());
        }
    }
}
