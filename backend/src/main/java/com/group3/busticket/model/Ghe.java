package com.group3.busticket.model;

/** Thông tin một ghế trên chuyến xe. */
public class Ghe {
    private String maGhe;
    private int soGhe;
    // Giá trị dự kiến: GHE_THUONG hoặc GHE_VIP.
    private String loaiGhe;
    // Giá trị dự kiến: CON_TRONG hoặc DA_DAT.
    private String trangThaiGhe;

    public Ghe() {
    }

    public Ghe(String maGhe, int soGhe, String loaiGhe, String trangThaiGhe) {
        this.maGhe = maGhe;
        this.soGhe = soGhe;
        this.loaiGhe = loaiGhe;
        this.trangThaiGhe = trangThaiGhe;
    }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    public String getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(String loaiGhe) { this.loaiGhe = loaiGhe; }

    public String getTrangThaiGhe() { return trangThaiGhe; }
    public void setTrangThaiGhe(String trangThaiGhe) { this.trangThaiGhe = trangThaiGhe; }
}
