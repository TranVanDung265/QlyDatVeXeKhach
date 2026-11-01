package com.group3.busticket;

import com.group3.busticket.bookings.BookingApiServer;
import com.group3.busticket.bookings.BookingService;
import com.group3.busticket.customers.KhachHangRepository;
import com.group3.busticket.customers.KhachHangService;
import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;
import com.group3.busticket.model.ThanhToan;
import com.group3.busticket.model.Ve;
import com.group3.busticket.payments.PaymentService;
import com.group3.busticket.payments.ThanhToanRepository;
import com.group3.busticket.repository.VeRepository;
import com.group3.busticket.seats.SeatService;
import com.group3.busticket.trips.TripService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        KhachHangService khachHangService = new KhachHangService(
                new KhachHangRepository()
        );

        // Tạo dữ liệu mẫu trong lần chạy đầu tiên nếu chưa có khách hàng.
        if (khachHangService.layDanhSachKhachHang().isEmpty()) {
            khachHangService.themKhachHang(
                    "Nguyễn Văn An", "0900000001", "an@example.com", "KHACH_THUONG"
            );
            khachHangService.themKhachHang(
                    "Trần Thị Bình", "0900000002", "binh@example.com", "THANH_VIEN"
            );
            khachHangService.themKhachHang(
                    "Lê Minh Tuấn", "0900000003", "tuan@example.com", "VIP"
            );
        }

        List<ChuyenXe> danhSachChuyenXe = Arrays.asList(
                new ChuyenXe(
                        "CX001", "Hà Nội", "Hải Phòng",
                        "2026-11-01 08:00", "29A-12345",
                        16, 150000L, taoDanhSachGhe("1B", "2C", "3A", "4D")
                ),
                new ChuyenXe(
                        "CX002", "Hải Phòng", "Hà Nội",
                        "2026-11-01 14:00", "15B-67890",
                        16, 150000L, taoDanhSachGhe("1A", "2D", "3C")
                ),
                new ChuyenXe(
                        "CX003", "Hà Nội", "Ninh Bình",
                        "2026-11-02 07:30", "29B-24680",
                        16, 100000L, taoDanhSachGhe("1C", "2A")
                ),
                new ChuyenXe(
                        "CX004", "Ninh Bình", "Hà Nội",
                        "2026-11-02 15:00", "35A-13579",
                        16, 100000L, taoDanhSachGhe("1B", "3D")
                ),
                new ChuyenXe(
                        "CX005", "Hà Nội", "Đà Nẵng",
                        "2026-11-03 06:00", "29C-11223",
                        16, 550000L, taoDanhSachGhe("1A", "2D", "3B")
                ),
                new ChuyenXe(
                        "CX006", "Đà Nẵng", "Huế",
                        "2026-11-03 13:30", "43A-44556",
                        16, 100000L, taoDanhSachGhe("2B", "4D")
                ),
                new ChuyenXe(
                        "CX007", "Huế", "Đà Nẵng",
                        "2026-11-04 09:00", "75B-77889",
                        16, 100000L, taoDanhSachGhe("1D", "3A")
                ),
                new ChuyenXe(
                        "CX008", "TP. Hồ Chí Minh", "Cần Thơ",
                        "2026-11-04 07:00", "51A-33445",
                        16, 180000L, taoDanhSachGhe("1B", "2C", "4D")
                ),
                new ChuyenXe(
                        "CX009", "Cần Thơ", "TP. Hồ Chí Minh",
                        "2026-11-05 15:30", "65A-66778",
                        16, 180000L, taoDanhSachGhe("2A", "3C")
                ),
                new ChuyenXe(
                        "CX010", "Hà Nội", "TP. Hồ Chí Minh",
                        "2026-11-05 20:00", "29D-99001",
                        16, 900000L, taoDanhSachGhe("1A", "1D", "2B", "4C")
                )
        );

        TripService tripService = new TripService(danhSachChuyenXe);
        SeatService seatService = new SeatService();

        VeRepository veRepository = new VeRepository("data/ve.txt");
        List<Ve> danhSachVe = veRepository.docTatCa();

        // Đồng bộ ghế đã đặt theo các vé đang lưu.
        for (Ve ve : danhSachVe) {
            for (ChuyenXe chuyenXe : danhSachChuyenXe) {
                if (ve.getMaChuyenXe().equals(chuyenXe.getMaChuyenXe())) {
                    for (Ghe ghe : chuyenXe.getDanhSachGhe()) {
                        if (ve.getMaGhe().equals(ghe.getMaGhe())) {
                            ghe.setTrangThaiGhe("DA_DAT");
                        }
                    }
                }
            }
        }

        BookingService bookingService = new BookingService(
                khachHangService,
                tripService,
                seatService,
                danhSachVe,
                veRepository
        );

        ThanhToanRepository thanhToanRepository =
                new ThanhToanRepository("data/thanhtoan.txt");
        List<ThanhToan> danhSachThanhToan =
                thanhToanRepository.docTatCa();

        PaymentService paymentService = new PaymentService(
                danhSachVe,
                danhSachThanhToan,
                veRepository,
                thanhToanRepository
        );

        BookingApiServer apiServer = new BookingApiServer(
                bookingService,
                khachHangService,
                paymentService
        );
        apiServer.start();
    }

    private static List<Ghe> taoDanhSachGhe(String... gheDaDat) {
        Set<String> tapGheDaDat = new HashSet<>(Arrays.asList(gheDaDat));
        List<Ghe> danhSachGhe = new ArrayList<>();
        String[] cacCot = {"A", "B", "C", "D"};
        int soThuTuGhe = 1;

        for (int hang = 1; hang <= 4; hang++) {
            for (String cot : cacCot) {
                String maGhe = hang + cot;
                boolean laGheVip = "D".equals(cot);
                String loaiGhe = laGheVip ? "GHE_VIP" : "GHE_THUONG";
                String trangThai = tapGheDaDat.contains(maGhe)
                        ? "DA_DAT"
                        : "CON_TRONG";
                long phuPhi = laGheVip ? 50000L : 0L;

                danhSachGhe.add(new Ghe(
                        maGhe, soThuTuGhe, loaiGhe, trangThai, phuPhi
                ));
                soThuTuGhe++;
            }
        }

        return danhSachGhe;
    }
}
