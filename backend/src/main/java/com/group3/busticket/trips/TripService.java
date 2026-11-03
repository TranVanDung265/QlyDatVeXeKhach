package com.group3.busticket.trips;

import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Nghiệp vụ tra cứu và quản lý chuyến xe trong thời gian chạy chương trình. */
public class TripService {
    private static final String DA_DAT = "DA_DAT";
    private static final DateTimeFormatter DINH_DANG_GIO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<ChuyenXe> danhSachChuyenXe;

    public TripService(List<ChuyenXe> danhSachChuyenXe) {
        if (danhSachChuyenXe == null) {
            throw new IllegalArgumentException(
                    "Danh sách chuyến xe không được null."
            );
        }
        // Tạo danh sách có thể thêm/xóa kể cả khi đầu vào là Arrays.asList().
        this.danhSachChuyenXe = new ArrayList<>(danhSachChuyenXe);
    }

    public List<ChuyenXe> layDanhSachChuyenXe() {
        return new ArrayList<>(danhSachChuyenXe);
    }

    /** Tìm theo điểm đi, điểm đến; để trống một tiêu chí thì bỏ qua tiêu chí đó. */
    public List<ChuyenXe> timChuyenXeTheoTuyen(
            String diemDi,
            String diemDen
    ) {
        List<ChuyenXe> ketQua = new ArrayList<>();

        for (ChuyenXe chuyenXe : danhSachChuyenXe) {
            boolean dungDiemDi = laTieuChiRong(diemDi)
                    || chuyenXe.getDiemDi().equalsIgnoreCase(diemDi.trim());
            boolean dungDiemDen = laTieuChiRong(diemDen)
                    || chuyenXe.getDiemDen().equalsIgnoreCase(diemDen.trim());

            if (dungDiemDi && dungDiemDen) {
                ketQua.add(chuyenXe);
            }
        }
        return ketQua;
    }

    /** Lọc chuyến theo ngày, định dạng yyyy-MM-dd. */
    public List<ChuyenXe> layChuyenXeTheoNgay(String ngay) {
        if (ngay == null || ngay.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày tìm kiếm không được để trống.");
        }
        String ngayCanTim = ngay.trim();
        try {
            LocalDate.parse(ngayCanTim);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Ngày phải có định dạng yyyy-MM-dd."
            );
        }

        List<ChuyenXe> ketQua = new ArrayList<>();
        for (ChuyenXe chuyenXe : danhSachChuyenXe) {
            String thoiGian = chuyenXe.getThoiGianKhoiHanh();
            if (thoiGian != null && thoiGian.startsWith(ngayCanTim)) {
                ketQua.add(chuyenXe);
            }
        }
        return ketQua;
    }

    /** Trả về bản sao danh sách được sắp xếp theo giờ khởi hành tăng dần. */
    public List<ChuyenXe> sapXepTheoGioKhoiHanh() {
        List<ChuyenXe> ketQua = layDanhSachChuyenXe();
        ketQua.sort(Comparator.comparing(
                ChuyenXe::getThoiGianKhoiHanh,
                Comparator.nullsLast(String::compareTo)
        ));
        return ketQua;
    }

    /** Trả về các chuyến có giá vé nằm trong khoảng đã chọn. */
    public List<ChuyenXe> locTheoKhoangGia(long giaTu, long giaDen) {
        if (giaTu < 0 || giaDen < giaTu) {
            throw new IllegalArgumentException("Khoảng giá không hợp lệ.");
        }
        List<ChuyenXe> ketQua = new ArrayList<>();
        for (ChuyenXe chuyenXe : danhSachChuyenXe) {
            long gia = chuyenXe.getGiaVeCoBan();
            if (gia >= giaTu && gia <= giaDen) {
                ketQua.add(chuyenXe);
            }
        }
        return ketQua;
    }

    public ChuyenXe timTheoMa(String maChuyenXe) {
        if (maChuyenXe == null || maChuyenXe.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chuyến xe không được để trống.");
        }
        for (ChuyenXe chuyenXe : danhSachChuyenXe) {
            if (maChuyenXe.trim().equalsIgnoreCase(chuyenXe.getMaChuyenXe())) {
                return chuyenXe;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy chuyến xe.");
    }

    /** Thêm chuyến mới; mã chuyến phải duy nhất. */
    public void themChuyenXe(ChuyenXe chuyenXe) {
        kiemTraThongTinChuyenXe(chuyenXe);
        if (daTonTaiMa(chuyenXe.getMaChuyenXe(), null)) {
            throw new IllegalArgumentException("Mã chuyến xe đã tồn tại.");
        }
        danhSachChuyenXe.add(chuyenXe);
    }

    /** Cập nhật thông tin tuyến, giờ, xe và giá; giữ nguyên danh sách ghế hiện tại. */
    public void capNhatChuyenXe(
            String maChuyenXe,
            String diemDi,
            String diemDen,
            String thoiGianKhoiHanh,
            String bienSoXe,
            long giaVeCoBan
    ) {
        ChuyenXe chuyenXe = timTheoMa(maChuyenXe);
        kiemTraThongTinChinh(diemDi, diemDen, thoiGianKhoiHanh,
                bienSoXe, chuyenXe.getTongSoGhe(), giaVeCoBan);

        chuyenXe.setDiemDi(diemDi.trim());
        chuyenXe.setDiemDen(diemDen.trim());
        chuyenXe.setThoiGianKhoiHanh(thoiGianKhoiHanh.trim());
        chuyenXe.setBienSoXe(bienSoXe.trim());
        chuyenXe.setGiaVeCoBan(giaVeCoBan);
    }

    /** Chỉ xóa chuyến chưa có ghế nào được đặt. */
    public void xoaChuyenXe(String maChuyenXe) {
        ChuyenXe chuyenXe = timTheoMa(maChuyenXe);
        for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
            if (ghe != null && DA_DAT.equals(ghe.getTrangThaiGhe())) {
                throw new IllegalStateException(
                        "Không thể xóa chuyến đã có ghế được đặt."
                );
            }
        }
        danhSachChuyenXe.remove(chuyenXe);
    }

    public int demSoChuyen() {
        return danhSachChuyenXe.size();
    }

    private void kiemTraThongTinChuyenXe(ChuyenXe chuyenXe) {
        if (chuyenXe == null) {
            throw new IllegalArgumentException("Chuyến xe không được để trống.");
        }
        if (chuyenXe.getMaChuyenXe() == null
                || chuyenXe.getMaChuyenXe().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chuyến xe không được để trống.");
        }
        kiemTraThongTinChinh(chuyenXe.getDiemDi(), chuyenXe.getDiemDen(),
                chuyenXe.getThoiGianKhoiHanh(), chuyenXe.getBienSoXe(),
                chuyenXe.getTongSoGhe(), chuyenXe.getGiaVeCoBan());
    }

    private void kiemTraThongTinChinh(
            String diemDi,
            String diemDen,
            String thoiGian,
            String bienSoXe,
            int tongSoGhe,
            long giaVe
    ) {
        if (laTieuChiRong(diemDi) || laTieuChiRong(diemDen)
                || diemDi.trim().equalsIgnoreCase(diemDen.trim())) {
            throw new IllegalArgumentException("Điểm đi/đến không hợp lệ.");
        }
        if (laTieuChiRong(bienSoXe) || laTieuChiRong(thoiGian)) {
            throw new IllegalArgumentException(
                    "Thời gian khởi hành và biển số xe là bắt buộc."
            );
        }
        try {
            LocalDateTime.parse(thoiGian.trim(), DINH_DANG_GIO);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Thời gian phải có định dạng yyyy-MM-dd HH:mm."
            );
        }
        if (tongSoGhe <= 0 || giaVe <= 0) {
            throw new IllegalArgumentException(
                    "Tổng số ghế và giá vé phải lớn hơn 0."
            );
        }
    }

    private boolean daTonTaiMa(String ma, String boQuaMa) {
        for (ChuyenXe chuyenXe : danhSachChuyenXe) {
            boolean cungMa = ma.equalsIgnoreCase(chuyenXe.getMaChuyenXe());
            boolean khongBoQua = boQuaMa == null
                    || !boQuaMa.equalsIgnoreCase(chuyenXe.getMaChuyenXe());
            if (cungMa && khongBoQua) {
                return true;
            }
        }
        return false;
    }

    private boolean laTieuChiRong(String giaTri) {
        return giaTri == null || giaTri.trim().isEmpty();
    }
}
