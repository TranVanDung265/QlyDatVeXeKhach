package com.group3.busticket.payments;

import com.group3.busticket.model.ThanhToan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Đọc và ghi danh sách thanh toán vào file văn bản. */
public class ThanhToanRepository {
    private final Path duongDanFile;

    public ThanhToanRepository(String duongDanFile) {
        if (duongDanFile == null || duongDanFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn file không được để trống.");
        }
        this.duongDanFile = Paths.get(duongDanFile);
    }

    /** Đọc các dòng theo dạng: mã thanh toán|mã vé|phương thức|số tiền. */
    public List<ThanhToan> docTatCa() throws IOException {
        List<ThanhToan> danhSach = new ArrayList<>();

        if (!Files.exists(duongDanFile)) {
            return danhSach;
        }

        List<String> cacDong = Files.readAllLines(
                duongDanFile,
                StandardCharsets.UTF_8
        );

        for (String dong : cacDong) {
            if (dong == null || dong.trim().isEmpty()) {
                continue;
            }

            String[] cot = dong.split("\\|", -1);
            if (cot.length != 4) {
                continue;
            }

            try {
                danhSach.add(new ThanhToan(
                        cot[0],
                        cot[1],
                        cot[2],
                        Long.parseLong(cot[3])
                ));
            } catch (NumberFormatException e) {
                // Bỏ qua dòng lỗi để một dữ liệu hỏng không làm dừng toàn bộ ứng dụng.
            }
        }

        return danhSach;
    }

    /** Ghi đè file bằng toàn bộ danh sách thanh toán hiện tại. */
    public void ghiTatCa(List<ThanhToan> danhSach) throws IOException {
        if (danhSach == null) {
            throw new IllegalArgumentException("Danh sách thanh toán không được null.");
        }

        Path thuMucCha = duongDanFile.getParent();
        if (thuMucCha != null) {
            Files.createDirectories(thuMucCha);
        }

        List<String> cacDong = new ArrayList<>();
        for (ThanhToan thanhToan : danhSach) {
            if (thanhToan == null) {
                continue;
            }
            cacDong.add(anToan(thanhToan.getMaThanhToan()) + "|"
                    + anToan(thanhToan.getMaVe()) + "|"
                    + anToan(thanhToan.getPhuongThuc()) + "|"
                    + thanhToan.getSoTien());
        }

        Files.write(
                duongDanFile,
                cacDong,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private String anToan(String giaTri) {
        if (giaTri == null) {
            return "";
        }
        // Không cho dấu phân cách làm lệch các cột trong file.
        return giaTri.replace("|", " ").replace("\n", " ").replace("\r", " ");
    }
}
