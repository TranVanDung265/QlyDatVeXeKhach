package com.group3.busticket.model;

/** Thông tin thanh toán cho một vé. */
public class ThanhToan {
    private String maThanhToan;
    private String maVe;
    // Giá trị dự kiến: TIEN_MAT, CHUYEN_KHOAN hoặc VI_DIEN_TU.
    private String phuongThuc;
    // Số tiền tính bằng VND.
    private long soTien;

    public ThanhToan() {
    }

    public ThanhToan(String maThanhToan, String maVe, String phuongThuc, long soTien) {
        this.maThanhToan = maThanhToan;
        this.maVe = maVe;
        this.phuongThuc = phuongThuc;
        this.soTien = soTien;
    }

    public String getMaThanhToan() { return maThanhToan; }
    public void setMaThanhToan(String maThanhToan) { this.maThanhToan = maThanhToan; }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public long getSoTien() { return soTien; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
}
