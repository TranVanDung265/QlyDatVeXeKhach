package com.group3.busticket.customers;

import com.group3.busticket.model.KhachHang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Đọc và ghi danh sách khách hàng bằng file văn bản. */
public class KhachHangRepository {
    private final Path duongDanFile =
            Paths.get("data", "khachhang.txt");

    public List<KhachHang> docTatCa() throws IOException {
        List<KhachHang> danhSach = new ArrayList<>();

        if (!Files.exists(duongDanFile)) {
            return danhSach;
        }

        List<String> cacDong = Files.readAllLines(
                duongDanFile,
                StandardCharsets.UTF_8
        );

        for (String dong : cacDong) {
            if (dong.trim().isEmpty()) {
                continue;
            }

            String[] thongTin = dong.split("\\|", -1);

            if (thongTin.length != 5) {
                throw new IOException(
                        "Dòng dữ liệu khách hàng không đúng định dạng: " + dong
                );
            }

            KhachHang khachHang = new KhachHang(
                    thongTin[0],
                    thongTin[1],
                    thongTin[2],
                    thongTin[3],
                    thongTin[4]
            );

            danhSach.add(khachHang);
        }

        return danhSach;
    }

    public void ghiTatCa(List<KhachHang> danhSach) throws IOException {
        if (danhSach == null) {
            throw new IllegalArgumentException(
                    "Danh sách khách hàng không được để trống."
            );
        }

        Files.createDirectories(duongDanFile.getParent());

        List<String> cacDong = new ArrayList<>();

        for (KhachHang khachHang : danhSach) {
            cacDong.add(
                    giaTri(khachHang.getMaKhachHang()) + "|"
                    + giaTri(khachHang.getHoTen()) + "|"
                    + giaTri(khachHang.getSoDienThoai()) + "|"
                    + giaTri(khachHang.getEmail()) + "|"
                    + giaTri(khachHang.getLoaiKhach())
            );
        }

        Files.write(
                duongDanFile,
                cacDong,
                StandardCharsets.UTF_8
        );
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}