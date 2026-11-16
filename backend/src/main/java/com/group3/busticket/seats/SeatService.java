package com.group3.busticket.seats;

import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Nghiệp vụ tra cứu, thống kê và quản lý ghế của một chuyến xe. */
public class SeatService {
    private static final String CON_TRONG = "CON_TRONG";
    private static final String DA_DAT = "DA_DAT";
    private static final String GHE_THUONG = "GHE_THUONG";
    private static final String GHE_VIP = "GHE_VIP";

    public List<Ghe> layDanhSachGhe(ChuyenXe chuyenXe) {
        kiemTraChuyenXe(chuyenXe);
        return new ArrayList<>(chuyenXe.getDanhSachGhe());
    }

    public List<Ghe> layGheConTrong(ChuyenXe chuyenXe) {
        return layGheTheoTrangThai(chuyenXe, CON_TRONG);
    }

    public List<Ghe> layGheDaDat(ChuyenXe chuyenXe) {
        return layGheTheoTrangThai(chuyenXe, DA_DAT);
    }

    public List<Ghe> layGheTheoLoai(ChuyenXe chuyenXe, String loaiGhe) {
        kiemTraChuyenXe(chuyenXe);
        if (!GHE_THUONG.equals(loaiGhe) && !GHE_VIP.equals(loaiGhe)) {
            throw new IllegalArgumentException("Loại ghế không hợp lệ.");
        }
        List<Ghe> ketQua = new ArrayList<>();
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (loaiGhe.equals(ghe.getLoaiGhe())) {
                ketQua.add(ghe);
            }
        }
        return ketQua;
    }

    public List<Ghe> layGheTheoTrangThai(ChuyenXe chuyenXe, String trangThai) {
        kiemTraChuyenXe(chuyenXe);
        if (!CON_TRONG.equals(trangThai) && !DA_DAT.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái ghế không hợp lệ.");
        }
        List<Ghe> ketQua = new ArrayList<>();
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (trangThai.equals(ghe.getTrangThaiGhe())) {
                ketQua.add(ghe);
            }
        }
        return ketQua;
    }

    public Ghe timGhe(ChuyenXe chuyenXe, String maGhe) {
        kiemTraChuyenXe(chuyenXe);
        if (maGhe == null || maGhe.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ghế không được để trống.");
        }
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (maGhe.trim().equalsIgnoreCase(ghe.getMaGhe())) {
                return ghe;
            }
        }
        throw new IllegalArgumentException(
                "Ghế không tồn tại trên chuyến xe này."
        );
    }

    public Ghe timGheTheoSo(ChuyenXe chuyenXe, int soGhe) {
        kiemTraChuyenXe(chuyenXe);
        if (soGhe <= 0) {
            throw new IllegalArgumentException("Số ghế phải lớn hơn 0.");
        }
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (ghe.getSoGhe() == soGhe) {
                return ghe;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy số ghế này.");
    }

    public List<Ghe> sapXepTheoSoGhe(ChuyenXe chuyenXe) {
        List<Ghe> ketQua = layDanhSachGhe(chuyenXe);
        ketQua.sort(Comparator.comparingInt(Ghe::getSoGhe));
        return ketQua;
    }

    public int demGheConTrong(ChuyenXe chuyenXe) {
        return layGheConTrong(chuyenXe).size();
    }

    public int demGheDaDat(ChuyenXe chuyenXe) {
        return layGheDaDat(chuyenXe).size();
    }

    public int demGheVip(ChuyenXe chuyenXe) {
        return layGheTheoLoai(chuyenXe, GHE_VIP).size();
    }

    /** Thêm ghế mới và đồng bộ tổng số ghế của chuyến. */
    public void themGhe(ChuyenXe chuyenXe, Ghe gheMoi) {
        kiemTraChuyenXe(chuyenXe);
        kiemTraThongTinGhe(gheMoi);
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (ghe.getMaGhe().equalsIgnoreCase(gheMoi.getMaGhe())
                    || ghe.getSoGhe() == gheMoi.getSoGhe()) {
                throw new IllegalArgumentException(
                        "Mã ghế hoặc số ghế đã tồn tại trên chuyến."
                );
            }
        }
        List<Ghe> danhSach = new ArrayList<>(chuyenXe.getDanhSachGhe());
        danhSach.add(gheMoi);
        chuyenXe.setDanhSachGhe(danhSach);
        chuyenXe.setTongSoGhe(danhSach.size());
    }

    /** Không cho xóa ghế đã đặt để tránh làm mất chỗ của vé hiện có. */
    public void xoaGhe(ChuyenXe chuyenXe, String maGhe) {
        Ghe ghe = timGhe(chuyenXe, maGhe);
        if (DA_DAT.equals(ghe.getTrangThaiGhe())) {
            throw new IllegalStateException("Không thể xóa ghế đã được đặt.");
        }
        List<Ghe> danhSach = new ArrayList<>(chuyenXe.getDanhSachGhe());
        danhSach.remove(ghe);
        chuyenXe.setDanhSachGhe(danhSach);
        chuyenXe.setTongSoGhe(danhSach.size());
    }

    /** Đổi ghế thường/VIP; ghế VIP bắt buộc có phụ phí dương. */
    public void capNhatLoaiGhe(
            ChuyenXe chuyenXe,
            String maGhe,
            String loaiGheMoi,
            long phuPhiMoi
    ) {
        if (!GHE_THUONG.equals(loaiGheMoi) && !GHE_VIP.equals(loaiGheMoi)) {
            throw new IllegalArgumentException("Loại ghế không hợp lệ.");
        }
        if (phuPhiMoi < 0 || (GHE_VIP.equals(loaiGheMoi) && phuPhiMoi == 0)) {
            throw new IllegalArgumentException("Phụ phí ghế không hợp lệ.");
        }
        Ghe ghe = timGhe(chuyenXe, maGhe);
        ghe.setLoaiGhe(loaiGheMoi);
        ghe.setPhuPhi(GHE_THUONG.equals(loaiGheMoi) ? 0 : phuPhiMoi);
    }

    public void datGhe(Ghe ghe) {
        if (ghe == null) {
            throw new IllegalArgumentException("Ghế không được để trống.");
        }
        if (!CON_TRONG.equals(ghe.getTrangThaiGhe())) {
            throw new IllegalStateException("Ghế này đã được đặt.");
        }
        ghe.setTrangThaiGhe(DA_DAT);
    }

    public void huyDatGhe(Ghe ghe) {
        if (ghe == null) {
            throw new IllegalArgumentException("Ghế không được để trống.");
        }
        if (!DA_DAT.equals(ghe.getTrangThaiGhe())) {
            throw new IllegalStateException("Ghế hiện chưa được đặt.");
        }
        ghe.setTrangThaiGhe(CON_TRONG);
    }

    private void kiemTraChuyenXe(ChuyenXe chuyenXe) {
        if (chuyenXe == null) {
            throw new IllegalArgumentException("Chuyến xe không được để trống.");
        }
        if (chuyenXe.getDanhSachGhe() == null) {
            throw new IllegalArgumentException("Chuyến xe chưa có danh sách ghế.");
        }
    }

    private void kiemTraThongTinGhe(Ghe ghe) {
        if (ghe == null || ghe.getMaGhe() == null
                || ghe.getMaGhe().trim().isEmpty() || ghe.getSoGhe() <= 0) {
            throw new IllegalArgumentException("Thông tin ghế không hợp lệ.");
        }
        if (!GHE_THUONG.equals(ghe.getLoaiGhe())
                && !GHE_VIP.equals(ghe.getLoaiGhe())) {
            throw new IllegalArgumentException("Loại ghế không hợp lệ.");
        }
        if (!CON_TRONG.equals(ghe.getTrangThaiGhe())
                && !DA_DAT.equals(ghe.getTrangThaiGhe())) {
            throw new IllegalArgumentException("Trạng thái ghế không hợp lệ.");
        }
        if (ghe.getPhuPhi() < 0
                || (GHE_VIP.equals(ghe.getLoaiGhe()) && ghe.getPhuPhi() == 0)) {
            throw new IllegalArgumentException("Phụ phí ghế không hợp lệ.");
        }
    }
}
