package com.group3.busticket.customers;

import com.group3.busticket.model.KhachHang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Xử lý các thao tác quản lý khách hàng. */
public class KhachHangService {
    private final KhachHangRepository repository;
    private List<KhachHang> danhSachKhachHang;

    public KhachHangService(KhachHangRepository repository)
            throws IOException {
        if (repository == null) {
            throw new IllegalArgumentException(
                    "Repository khách hàng không được để trống."
            );
        }

        this.repository = repository;
        this.danhSachKhachHang = repository.docTatCa();
    }

    public List<KhachHang> layDanhSachKhachHang() {
        return new ArrayList<>(danhSachKhachHang);
    }

    public KhachHang timTheoMa(String maKhachHang) {
        for (KhachHang khachHang : danhSachKhachHang) {
            if (khachHang.getMaKhachHang().equals(maKhachHang)) {
                return khachHang;
            }
        }

        throw new IllegalArgumentException("Không tìm thấy khách hàng.");
    }

    public KhachHang themKhachHang(
            String hoTen,
            String soDienThoai,
            String email,
            String loaiKhach
    ) throws IOException {
        kiemTraThongTin(hoTen, soDienThoai, email, loaiKhach);

        KhachHang khachHang = new KhachHang(
                taoMaKhachHang(),
                hoTen.trim(),
                soDienThoai.trim(),
                email.trim(),
                loaiKhach.trim()
        );

        List<KhachHang> danhSachCu =
                new ArrayList<>(danhSachKhachHang);
        danhSachKhachHang.add(khachHang);

        try {
            repository.ghiTatCa(danhSachKhachHang);
        } catch (IOException e) {
            danhSachKhachHang = danhSachCu;
            throw e;
        }

        return khachHang;
    }

    public void capNhatKhachHang(
            String maKhachHang,
            String hoTen,
            String soDienThoai,
            String email,
            String loaiKhach
    ) throws IOException {
        kiemTraThongTin(hoTen, soDienThoai, email, loaiKhach);

        int viTri = timViTriTheoMa(maKhachHang);

        KhachHang khachHangMoi = new KhachHang(
                maKhachHang,
                hoTen.trim(),
                soDienThoai.trim(),
                email.trim(),
                loaiKhach.trim()
        );

        List<KhachHang> danhSachCu =
                new ArrayList<>(danhSachKhachHang);
        danhSachKhachHang.set(viTri, khachHangMoi);

        try {
            repository.ghiTatCa(danhSachKhachHang);
        } catch (IOException e) {
            danhSachKhachHang = danhSachCu;
            throw e;
        }
    }

    public void xoaKhachHang(String maKhachHang) throws IOException {
        int viTri = timViTriTheoMa(maKhachHang);

        List<KhachHang> danhSachCu =
                new ArrayList<>(danhSachKhachHang);
        danhSachKhachHang.remove(viTri);

        try {
            repository.ghiTatCa(danhSachKhachHang);
        } catch (IOException e) {
            danhSachKhachHang = danhSachCu;
            throw e;
        }
    }

    private int timViTriTheoMa(String maKhachHang) {
        for (int i = 0; i < danhSachKhachHang.size(); i++) {
            if (danhSachKhachHang.get(i).getMaKhachHang()
                    .equals(maKhachHang)) {
                return i;
            }
        }

        throw new IllegalArgumentException("Không tìm thấy khách hàng.");
    }

    private String taoMaKhachHang() {
        int maLonNhat = 0;

        for (KhachHang khachHang : danhSachKhachHang) {
            String ma = khachHang.getMaKhachHang();

            if (ma != null && ma.startsWith("KH")) {
                try {
                    int so = Integer.parseInt(ma.substring(2));
                    maLonNhat = Math.max(maLonNhat, so);
                } catch (NumberFormatException ignored) {
                    // Bỏ qua mã không có phần số hợp lệ.
                }
            }
        }

        return String.format("KH%03d", maLonNhat + 1);
    }

    private void kiemTraThongTin(
            String hoTen,
            String soDienThoai,
            String email,
            String loaiKhach
    ) {
        if (rong(hoTen) || rong(soDienThoai)
                || rong(email) || rong(loaiKhach)) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ thông tin khách hàng."
            );
        }

        if (!"KHACH_THUONG".equals(loaiKhach)
                && !"THANH_VIEN".equals(loaiKhach)
                && !"VIP".equals(loaiKhach)) {
            throw new IllegalArgumentException(
                    "Loại khách phải là KHACH_THUONG, THANH_VIEN hoặc VIP."
            );
        }
    }

    private boolean rong(String giaTri) {
        return giaTri == null || giaTri.trim().isEmpty();
    }
}