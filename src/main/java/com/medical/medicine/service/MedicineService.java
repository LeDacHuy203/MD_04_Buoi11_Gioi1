package com.medical.medicine.service;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;

import java.util.List;

public interface MedicineService {

    /**
     * Lay chi tiet thuoc theo ID - Duoc ap dung Redis Caching theo Cache-aside pattern
     *
     * @param id ID cua thuoc can tim
     * @return Thong tin chi tiet thuoc (MedicineResponse)
     */
    MedicineResponse getMedicineById(Long id);

    /**
     * Lay danh sach tat ca cac loai thuoc trong danh muc
     *
     * @return Danh sach MedicineResponse
     */
    List<MedicineResponse> getAllMedicines();

    /**
     * Tao moi thuoc vao he thong
     *
     * @param request Du lieu tao thuoc
     * @return Thong tin thuoc da duoc tao
     */
    MedicineResponse createMedicine(MedicineRequest request);

    /**
     * Cap nhat thong tin thuoc su dung Entity va xoa cache Redis
     * Su dung @CacheEvict(value = "medicines", key = "#medicine.id")
     *
     * @param medicine Doi tuong Medicine can cap nhat (co chua id)
     * @return Thong tin thuoc sau khi cap nhat
     */
    MedicineResponse updateMedicine(Medicine medicine);

    /**
     * Cap nhat thong tin thuoc theo ID va MedicineRequest
     *
     * @param id ID thuoc can cap nhat
     * @param request Du lieu cap nhat
     * @return Thong tin thuoc sau khi cap nhat
     */
    MedicineResponse updateMedicine(Long id, MedicineRequest request);

    /**
     * Xoa thuoc theo ID va dong thoi xoa cache trong Redis
     *
     * @param id ID thuoc can xoa
     */
    void deleteMedicine(Long id);

    /**
     * Nhap hang thuoc va phat thong bao den Dashboard quan ly chi nhanh qua Redis Pub/Sub
     *
     * @param id ID thuoc
     * @param quantity So luong hop thuoc nhap
     * @return Thong tin thuoc
     */
    MedicineResponse importMedicine(Long id, int quantity);
}
