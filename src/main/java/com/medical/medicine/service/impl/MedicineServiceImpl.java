package com.medical.medicine.service.impl;

import com.medical.medicine.dto.request.AlertMessage;
import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.exception.DuplicateResourceException;
import com.medical.medicine.exception.ResourceNotFoundException;
import com.medical.medicine.mapper.MedicineMapper;
import com.medical.medicine.pubsub.AlertPublisher;
import com.medical.medicine.repository.MedicineRepository;
import com.medical.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final AlertPublisher alertPublisher;

    /**
     * Lay chi tiet thuoc theo ID voi co che Cache-aside pattern:
     * - Lan 1: Redis chua co cache -> Spring Cache chay ham nay -> Log SQL truy van database -> Luu vao Redis (TTL 60 phut).
     * - Lan 2: Redis da co cache -> Spring Cache tra ve thang tu Redis -> Ham nay KHONG chay -> Khong co log SQL.
     */
    @Override
    @Cacheable(value = "medicines", key = "#id")
    public MedicineResponse getMedicineById(Long id) {
        log.info("--> [CACHE MISS / DB QUERY] Dang truy van thong tin thuoc tu Database voi ID = {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id));

        return medicineMapper.toResponse(medicine);
    }

    @Override
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(medicineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        if (medicineRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ma thuoc da ton tai: " + request.code());
        }

        Medicine medicine = medicineMapper.toEntity(request);
        Medicine savedMedicine = medicineRepository.save(medicine);
        log.info("Tao moi thuoc thanh cong voi ID = {}", savedMedicine.getId());
        return medicineMapper.toResponse(savedMedicine);
    }

    /**
     * Yeu cau de bai:
     * Su dung @CacheEvict(value = "medicines", key = "#medicine.id") tai ham cap nhat thong tin thuoc.
     * Khi mot loai thuoc duoc sua gia, Cache cu bi xoa ngay lap tuc khoi Redis.
     */
    @Override
    @Transactional
    @CacheEvict(value = "medicines", key = "#medicine.id")
    public MedicineResponse updateMedicine(Medicine medicine) {
        log.info("--> [CACHE EVICT] Dang cap nhat thong tin/gia thuoc va xoa cache Redis voi ID = {}", medicine.getId());

        Medicine existing = medicineRepository.findById(medicine.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + medicine.getId()));

        if (!existing.getCode().equals(medicine.getCode()) && medicineRepository.existsByCode(medicine.getCode())) {
            throw new DuplicateResourceException("Ma thuoc da ton tai: " + medicine.getCode());
        }

        existing.setName(medicine.getName());
        existing.setCode(medicine.getCode());
        existing.setCategory(medicine.getCategory());
        existing.setPrice(medicine.getPrice());
        existing.setDosage(medicine.getDosage());
        existing.setDescription(medicine.getDescription());
        existing.setManufacturer(medicine.getManufacturer());

        Medicine updated = medicineRepository.save(existing);
        log.info("Cap nhat thuoc thanh cong! Cache key medicines::{} da bi xoa khoi Redis.", medicine.getId());
        return medicineMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        log.info("--> [CACHE EVICT] Dang cap nhat thong tin/gia thuoc va xoa cache Redis voi ID = {}", id);

        Medicine existing = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id));

        if (!existing.getCode().equals(request.code()) && medicineRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ma thuoc da ton tai: " + request.code());
        }

        medicineMapper.updateEntityFromRequest(existing, request);
        Medicine updated = medicineRepository.save(existing);
        log.info("Cap nhat thuoc thanh cong! Cache key medicines::{} da bi xoa khoi Redis.", id);
        return medicineMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id);
        }
        medicineRepository.deleteById(id);
        log.info("Xoa thuoc va xoa cache Redis voi ID = {}", id);
    }

    /**
     * Nhap them hang cho thuoc:
     * Phat sinh message ban tin JSON vao Redis Pub/Sub channel 'pharmacy-alerts':
     * { "type": "IMPORT", "message": "Đã nhập X hộp [Tên thuốc]" }
     */
    @Override
    @Transactional
    public MedicineResponse importMedicine(Long id, int quantity) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id));

        String alertText = "Đã nhập " + quantity + " hộp " + medicine.getName();

        // Ban thong bao Fire-and-forget qua Redis Pub/Sub
        alertPublisher.publishAlert(new AlertMessage("pharmacy-alerts", "IMPORT", alertText));

        return medicineMapper.toResponse(medicine);
    }
}
