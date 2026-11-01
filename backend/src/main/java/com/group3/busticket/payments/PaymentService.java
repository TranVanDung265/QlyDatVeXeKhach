package com.group3.busticket.payments;

import com.group3.busticket.model.ThanhToan;
import com.group3.busticket.model.Ve;
import com.group3.busticket.repository.VeRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Xử lý thanh toán vé và cập nhật dữ liệu lưu trên file. */
public class PaymentService {
    private static final String CHUA_THANH_TOAN = "CHUA_THANH_TOAN";
    private static final String DA_THANH_TOAN = "DA_THANH_TOAN";

    private final List<Ve> danhSachVe;
    private final List<ThanhToan> danhSachThanhToan;
    private final VeRepository veRepository;
    private final ThanhToanRepository thanhToanRepository;

    public PaymentService(
            List<Ve> danhSachVe,
            List<ThanhToan> danhSachThanhToan,
            VeRepository veRepository,
            ThanhToanRepository thanhToanRepository
    ) {
        if (danhSachVe == null || danhSachThanhToan == null
                || veRepository == null || thanhToanRepository == null) {
            throw new IllegalArgumentException(
                    "Danh sách và repository thanh toán không được để trống."
            );
        }

        this.danhSachVe = danhSachVe;
        this.danhSachThanhToan = danhSachThanhToan;
        this.veRepository = veRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    /** Thanh toán một vé bằng tiền mặt, chuyển khoản hoặc ví điện tử. */
    public ThanhToan thanhToanVe(String maVe, String phuongThuc) {
        if (maVe == null || maVe.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vé không được để trống.");
        }

        if (!laPhuongThucHopLe(phuongThuc)) {
            throw new IllegalArgumentException(
                    "Phương thức phải là TIEN_MAT, CHUYEN_KHOAN hoặc VI_DIEN_TU."
            );
        }

        Ve ve = timVe(maVe.trim());
        if (DA_THANH_TOAN.equals(ve.getTrangThaiThanhToan())) {
            throw new IllegalStateException("Vé này đã được thanh toán.");
        }
        if (!CHUA_THANH_TOAN.equals(ve.getTrangThaiThanhToan())) {
            throw new IllegalStateException("Trạng thái thanh toán của vé không hợp lệ.");
        }
        if (ve.getGiaVeCuoiCung() <= 0) {
            throw new IllegalStateException("Giá vé phải lớn hơn 0.");
        }

        ThanhToan thanhToan = new ThanhToan(
                "TT-" + UUID.randomUUID(),
                ve.getMaVe(),
                phuongThuc,
                ve.getGiaVeCuoiCung()
        );

        ve.setTrangThaiThanhToan(DA_THANH_TOAN);
        danhSachThanhToan.add(thanhToan);

        try {
            veRepository.ghiTatCa(danhSachVe);
            thanhToanRepository.ghiTatCa(danhSachThanhToan);
        } catch (IOException e) {
            // Hoàn tác trong bộ nhớ và đồng bộ lại file nếu ghi một trong hai file lỗi.
            ve.setTrangThaiThanhToan(CHUA_THANH_TOAN);
            danhSachThanhToan.remove(thanhToan);
            try {
                veRepository.ghiTatCa(danhSachVe);
                thanhToanRepository.ghiTatCa(danhSachThanhToan);
            } catch (IOException loiHoanTac) {
                e.addSuppressed(loiHoanTac);
            }
            throw new IllegalStateException("Không thể lưu thông tin thanh toán.", e);
        }

        return thanhToan;
    }

    public List<Ve> layVeChuaThanhToan() {
        List<Ve> ketQua = new ArrayList<>();
        for (Ve ve : danhSachVe) {
            if (CHUA_THANH_TOAN.equals(ve.getTrangThaiThanhToan())) {
                ketQua.add(ve);
            }
        }
        return ketQua;
    }

    public List<ThanhToan> layDanhSachThanhToan() {
        return new ArrayList<>(danhSachThanhToan);
    }

    private Ve timVe(String maVe) {
        for (Ve ve : danhSachVe) {
            if (maVe.equals(ve.getMaVe())) {
                return ve;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vé.");
    }

    private boolean laPhuongThucHopLe(String phuongThuc) {
        return "TIEN_MAT".equals(phuongThuc)
                || "CHUYEN_KHOAN".equals(phuongThuc)
                || "VI_DIEN_TU".equals(phuongThuc);
    }
}
