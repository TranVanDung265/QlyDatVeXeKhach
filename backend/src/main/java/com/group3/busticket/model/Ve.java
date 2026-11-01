package com.group3.busticket.model;

/** Thông tin vé; các mã dùng để liên kết tới khách, chuyến và ghế. */
public class Ve {
    private String maVe;
    private String maKhachHang;
    private String maChuyenXe;
    private String maGhe;
    private long giaVeCuoiCung;
    private String trangThaiThanhToan;
    // Định dạng: yyyy-MM-dd HH:mm.
    private String thoiGianDat;

    public Ve() {
    }

    // Giữ constructor cũ để tương thích với code hiện có.
    public Ve(String maVe, String maKhachHang, String maChuyenXe, String maGhe,
              long giaVeCuoiCung, String trangThaiThanhToan) {
        this(maVe, maKhachHang, maChuyenXe, maGhe, giaVeCuoiCung,
                trangThaiThanhToan, null);
    }

    public Ve(String maVe, String maKhachHang, String maChuyenXe, String maGhe,
              long giaVeCuoiCung, String trangThaiThanhToan, String thoiGianDat) {
        this.maVe = maVe;
        this.maKhachHang = maKhachHang;
        this.maChuyenXe = maChuyenXe;
        this.maGhe = maGhe;
        this.giaVeCuoiCung = giaVeCuoiCung;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.thoiGianDat = thoiGianDat;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaChuyenXe() { return maChuyenXe; }
    public void setMaChuyenXe(String maChuyenXe) {
        this.maChuyenXe = maChuyenXe;
    }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public long getGiaVeCuoiCung() { return giaVeCuoiCung; }
    public void setGiaVeCuoiCung(long giaVeCuoiCung) {
        this.giaVeCuoiCung = giaVeCuoiCung;
    }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getThoiGianDat() { return thoiGianDat; }
    public void setThoiGianDat(String thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
    }
}