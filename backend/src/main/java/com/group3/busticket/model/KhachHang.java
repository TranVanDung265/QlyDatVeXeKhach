package com.group3.busticket.model;

/** Thông tin khách hàng đặt vé. */
public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String soDienThoai;
    private String email;
    // Giá trị dự kiến: KHACH_THUONG, THANH_VIEN, VIP.
    private String loaiKhach;

    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String hoTen, String soDienThoai,
                     String email, String loaiKhach) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.loaiKhach = loaiKhach;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLoaiKhach() { return loaiKhach; }
    public void setLoaiKhach(String loaiKhach) { this.loaiKhach = loaiKhach; }
}
