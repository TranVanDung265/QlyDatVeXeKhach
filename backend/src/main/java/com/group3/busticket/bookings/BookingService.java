package com.group3.busticket.bookings;

import com.group3.busticket.customers.KhachHangService;
import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;
import com.group3.busticket.model.KhachHang;
import com.group3.busticket.model.Ve;
import com.group3.busticket.repository.VeRepository;
import com.group3.busticket.seats.SeatService;
import com.group3.busticket.trips.TripService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingService {
    private static final String CON_TRONG = "CON_TRONG";
    private static final String CHUA_THANH_TOAN = "CHUA_THANH_TOAN";
    private static final int GIAM_THANH_VIEN = 5;
    private static final int GIAM_VIP = 10;
    private static final DateTimeFormatter DINH_DANG_GIO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<KhachHang> danhSachKhachHang;
    private final KhachHangService khachHangService;
    private final TripService tripService;
    private final SeatService seatService;
    private final List<Ve> danhSachVe;
    private final VeRepository veRepository;

    /** Constructor tương thích với cách khởi tạo cũ bằng danh sách chuyến. */
    public BookingService(List<KhachHang> danhSachKhachHang,
                          List<ChuyenXe> danhSachChuyenXe,
                          List<Ve> danhSachVe,
                          VeRepository veRepository) {
        this(danhSachKhachHang, null, new TripService(danhSachChuyenXe),
                new SeatService(), danhSachVe, veRepository);
    }

    /** Constructor tương thích khi khách hàng do KhachHangService quản lý. */
    public BookingService(KhachHangService khachHangService,
                          List<ChuyenXe> danhSachChuyenXe,
                          List<Ve> danhSachVe,
                          VeRepository veRepository) {
        this(null, khachHangService, new TripService(danhSachChuyenXe),
                new SeatService(), danhSachVe, veRepository);
    }

    /** Constructor mới nhận riêng dịch vụ chuyến xe và dịch vụ ghế. */
    public BookingService(KhachHangService khachHangService,
                          TripService tripService,
                          SeatService seatService,
                          List<Ve> danhSachVe,
                          VeRepository veRepository) {
        this(null, khachHangService, tripService, seatService,
                danhSachVe, veRepository);
    }

    public BookingService(List<KhachHang> danhSachKhachHang,
                          TripService tripService,
                          SeatService seatService,
                          List<Ve> danhSachVe,
                          VeRepository veRepository) {
        this(danhSachKhachHang, null, tripService, seatService,
                danhSachVe, veRepository);
    }

    private BookingService(List<KhachHang> danhSachKhachHang,
                           KhachHangService khachHangService,
                           TripService tripService,
                           SeatService seatService,
                           List<Ve> danhSachVe,
                           VeRepository veRepository) {
        if ((danhSachKhachHang == null && khachHangService == null)
                || tripService == null || seatService == null
                || danhSachVe == null || veRepository == null) {
            throw new IllegalArgumentException(
                    "Danh sách và dịch vụ dùng chung không được để trống.");
        }
        this.danhSachKhachHang = danhSachKhachHang;
        this.khachHangService = khachHangService;
        this.tripService = tripService;
        this.seatService = seatService;
        this.danhSachVe = danhSachVe;
        this.veRepository = veRepository;
    }

    public Ve datVe(String maKhachHang, String maChuyenXe, String maGhe) {
        KhachHang khachHang = timKhachHang(maKhachHang);
        ChuyenXe chuyenXe = tripService.timTheoMa(maChuyenXe);
        Ghe ghe = seatService.timGhe(chuyenXe, maGhe);

        if (!CON_TRONG.equals(ghe.getTrangThaiGhe())) {
            throw new IllegalStateException("Ghế này đã được đặt.");
        }
        for (Ve ve : danhSachVe) {
            if (maChuyenXe.equals(ve.getMaChuyenXe())
                    && maGhe.equals(ve.getMaGhe())) {
                throw new IllegalStateException("Ghế này đã có vé.");
            }
        }

        long giaVe = tinhGiaVe(khachHang, chuyenXe, ghe);
        String maVe = "VE-" + UUID.randomUUID();
        String thoiGianDat = LocalDateTime.now().format(DINH_DANG_GIO);
        Ve ve = new Ve(maVe, maKhachHang, maChuyenXe, maGhe,
                giaVe, CHUA_THANH_TOAN, thoiGianDat);

        danhSachVe.add(ve);
        seatService.datGhe(ghe);
        try {
            veRepository.ghiTatCa(danhSachVe);
        } catch (IOException e) {
            danhSachVe.remove(ve);
            seatService.huyDatGhe(ghe);
            throw new IllegalStateException("Không thể lưu vé vào file.", e);
        }
        return ve;
    }

    private KhachHang timKhachHang(String maKhachHang) {
        if (khachHangService != null) {
            return khachHangService.timTheoMa(maKhachHang);
        }
        if (maKhachHang != null) {
            for (KhachHang khachHang : danhSachKhachHang) {
                if (maKhachHang.equals(khachHang.getMaKhachHang())) {
                    return khachHang;
                }
            }
        }
        throw new IllegalArgumentException("Không tìm thấy khách hàng.");
    }

    public List<Ghe> layDanhSachGhe(String maChuyenXe) {
        return seatService.layDanhSachGhe(tripService.timTheoMa(maChuyenXe));
    }

    public List<Ghe> layGheConTrong(String maChuyenXe) {
        return seatService.layGheConTrong(tripService.timTheoMa(maChuyenXe));
    }

    public List<ChuyenXe> layDanhSachChuyenXe() {
        return tripService.layDanhSachChuyenXe();
    }

    public List<ChuyenXe> timChuyenXeTheoTuyen(String diemDi, String diemDen) {
        return tripService.timChuyenXeTheoTuyen(diemDi, diemDen);
    }

    public List<Ve> layVeBanTrongNgay() {
        String ngayHomNay = LocalDate.now().toString();
        List<Ve> ketQua = new ArrayList<>();
        for (Ve ve : danhSachVe) {
            if (ve.getThoiGianDat() != null
                    && ve.getThoiGianDat().startsWith(ngayHomNay)) {
                ketQua.add(ve);
            }
        }
        return ketQua;
    }

    private long tinhGiaVe(KhachHang khachHang, ChuyenXe chuyenXe, Ghe ghe) {
        if (chuyenXe.getGiaVeCoBan() <= 0) {
            throw new IllegalArgumentException(
                    "Giá vé cơ bản phải lớn hơn 0.");
        }
        if (ghe.getPhuPhi() < 0) {
            throw new IllegalArgumentException("Phụ phí ghế không được âm.");
        }
        if ("GHE_VIP".equals(ghe.getLoaiGhe()) && ghe.getPhuPhi() <= 0) {
            throw new IllegalArgumentException(
                    "Ghế VIP phải có phụ phí lớn hơn 0.");
        }

        long giaGoc = chuyenXe.getGiaVeCoBan() + ghe.getPhuPhi();
        int phanTramGiam = 0;
        if ("THANH_VIEN".equals(khachHang.getLoaiKhach())) {
            phanTramGiam = GIAM_THANH_VIEN;
        } else if ("VIP".equals(khachHang.getLoaiKhach())) {
            phanTramGiam = GIAM_VIP;
        }

        long giaCuoi = giaGoc * (100 - phanTramGiam) / 100;
        if (giaCuoi <= 0) {
            throw new IllegalArgumentException(
                    "Giá vé cuối cùng phải lớn hơn 0.");
        }
        return giaCuoi;
    }
}
