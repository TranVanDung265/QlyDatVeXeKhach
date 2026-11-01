package com.group3.busticket.repository;

import com.group3.busticket.model.Ve;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Đọc và ghi danh sách vé bằng File I/O. */
public class VeRepository {
    private final Path duongDan;

    public VeRepository(String duongDan) {
        this.duongDan = Paths.get(duongDan);
    }

    public List<Ve> docTatCa() throws IOException {
        List<Ve> danhSachVe = new ArrayList<>();

        if (!Files.exists(duongDan)) {
            return danhSachVe;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(duongDan, StandardCharsets.UTF_8)) {
            String dong;
            int soDong = 0;

            while ((dong = reader.readLine()) != null) {
                soDong++;
                String[] cot = dong.split("\\|", -1);

                if (cot.length != 7) {
                    throw new IOException(
                            "Dữ liệu vé sai định dạng ở dòng " + soDong
                    );
                }

                try {
                    long giaVe = Long.parseLong(cot[4]);
                    String thoiGianDat = cot[6].isEmpty() ? null : cot[6];

                    danhSachVe.add(new Ve(
                            cot[0], cot[1], cot[2], cot[3],
                            giaVe, cot[5], thoiGianDat
                    ));
                } catch (NumberFormatException e) {
                    throw new IOException(
                            "Giá vé sai định dạng ở dòng " + soDong, e
                    );
                }
            }
        }

        return danhSachVe;
    }

    public void ghiTatCa(List<Ve> danhSachVe) throws IOException {
        Path thuMucCha = duongDan.getParent();

        if (thuMucCha != null) {
            Files.createDirectories(thuMucCha);
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(duongDan, StandardCharsets.UTF_8)) {
            for (Ve ve : danhSachVe) {
                writer.write(
                        giaTri(ve.getMaVe()) + "|"
                        + giaTri(ve.getMaKhachHang()) + "|"
                        + giaTri(ve.getMaChuyenXe()) + "|"
                        + giaTri(ve.getMaGhe()) + "|"
                        + ve.getGiaVeCuoiCung() + "|"
                        + giaTri(ve.getTrangThaiThanhToan()) + "|"
                        + giaTri(ve.getThoiGianDat())
                );
                writer.newLine();
            }
        }
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}