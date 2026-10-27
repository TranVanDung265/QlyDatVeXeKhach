package com.group3.busticket.model;

/** Thông tin vé; các mã dùng để liên kết tới khách, chuyến và ghế. */
public class Ve {
    private String maVe;
    private String maKhachHang;
    private String maChuyenXe;
    private String maGhe;
    // Giá tiền tính bằng VND.
    private long giaVeCuoiCung;
    // Giá trị dự kiến: CHUA_THANH_TOAN hoặc DA_THANH_TOAN.
    private String trangThaiThanhToan;

    public Ve() {
    }

    public Ve(String maVe, String maKhachHang, String maChuyenXe, String maGhe,
              long giaVeCuoiCung, String trangThaiThanhToan) {
        this.maVe = maVe;
        this.maKhachHang = maKhachHang;
        this.maChuyenXe = maChuyenXe;
        this.maGhe = maGhe;
        this.giaVeCuoiCung = giaVeCuoiCung;
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaChuyenXe() { return maChuyenXe; }
    public void setMaChuyenXe(String maChuyenXe) { this.maChuyenXe = maChuyenXe; }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public long getGiaVeCuoiCung() { return giaVeCuoiCung; }
    public void setGiaVeCuoiCung(long giaVeCuoiCung) { this.giaVeCuoiCung = giaVeCuoiCung; }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }
}
