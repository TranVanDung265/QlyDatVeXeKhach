package com.group3.busticket.model;

import java.util.ArrayList;
import java.util.List;

/** Thông tin chuyến xe và danh sách ghế của chuyến. */
public class ChuyenXe {
    private String maChuyenXe;
    private String diemDi;
    private String diemDen;
    // Định dạng: yyyy-MM-dd HH:mm.
    private String thoiGianKhoiHanh;
    private String bienSoXe;
    private int tongSoGhe;
    private List<Ghe> danhSachGhe = new ArrayList<>();

    public ChuyenXe() {
    }

    public ChuyenXe(String maChuyenXe, String diemDi, String diemDen,
                    String thoiGianKhoiHanh, String bienSoXe, int tongSoGhe,
                    List<Ghe> danhSachGhe) {
        this.maChuyenXe = maChuyenXe;
        this.diemDi = diemDi;
        this.diemDen = diemDen;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.bienSoXe = bienSoXe;
        this.tongSoGhe = tongSoGhe;
        setDanhSachGhe(danhSachGhe);
    }

    public String getMaChuyenXe() { return maChuyenXe; }
    public void setMaChuyenXe(String maChuyenXe) { this.maChuyenXe = maChuyenXe; }

    public String getDiemDi() { return diemDi; }
    public void setDiemDi(String diemDi) { this.diemDi = diemDi; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public String getThoiGianKhoiHanh() { return thoiGianKhoiHanh; }
    public void setThoiGianKhoiHanh(String thoiGianKhoiHanh) {
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
    }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public int getTongSoGhe() { return tongSoGhe; }
    public void setTongSoGhe(int tongSoGhe) { this.tongSoGhe = tongSoGhe; }

    public List<Ghe> getDanhSachGhe() { return danhSachGhe; }
    public void setDanhSachGhe(List<Ghe> danhSachGhe) {
        this.danhSachGhe = danhSachGhe == null ? new ArrayList<Ghe>() : danhSachGhe;
    }
}
