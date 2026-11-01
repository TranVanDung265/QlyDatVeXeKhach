package com.group3.busticket.bookings;

import com.group3.busticket.customers.KhachHangApiHandler;
import com.group3.busticket.customers.KhachHangService;
import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;
import com.group3.busticket.model.Ve;
import com.group3.busticket.payments.PaymentApiHandler;
import com.group3.busticket.payments.PaymentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class BookingApiServer {
    private final BookingService bookingService;
    private final KhachHangService khachHangService;
    private final PaymentService paymentService;

    /** Dùng khi chỉ cần chạy các API đặt vé hiện có. */
    public BookingApiServer(BookingService bookingService) {
        this(bookingService, null, null);
    }

    /** Dùng khi cần chạy thêm API quản lý khách hàng. */
    public BookingApiServer(
            BookingService bookingService,
            KhachHangService khachHangService
    ) {
        this(bookingService, khachHangService, null);
    }

    /** Dùng khi chạy đồng thời API đặt vé, khách hàng và thanh toán. */
    public BookingApiServer(
            BookingService bookingService,
            KhachHangService khachHangService,
            PaymentService paymentService
    ) {
        if (bookingService == null) {
            throw new IllegalArgumentException(
                    "BookingService không được để trống."
            );
        }
        this.bookingService = bookingService;
        this.khachHangService = khachHangService;
        this.paymentService = paymentService;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Đường dẫn này cụ thể hơn /api/bookings nên sẽ xử lý GET danh sách hôm nay.
        server.createContext("/api/bookings/today", this::handleTodayBookings);
        server.createContext("/api/bookings", this::handleBooking);
        server.createContext("/api/trips", this::handleTrips);
        server.createContext("/api/trips/", this::handleTripSeats);

        if (khachHangService != null) {
            server.createContext(
                    "/api/customers",
                    new KhachHangApiHandler(khachHangService)
            );
        }

        if (paymentService != null) {
            server.createContext(
                    "/api/payments",
                    new PaymentApiHandler(paymentService)
            );
        }

        server.start();
        System.out.println("API đang chạy tại http://localhost:8080");
    }

    private void handleTodayBookings(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Phương thức không được hỗ trợ.\"}");
            return;
        }

        List<Ve> danhSachVe = bookingService.layVeBanTrongNgay();
        sendJson(exchange, 200, veToJson(danhSachVe));
    }

    private void handleBooking(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Phương thức không được hỗ trợ.\"}");
            return;
        }

        try {
            Map<String, String> form = readForm(exchange);

            String maKhachHang = form.get("maKhachHang");
            String maChuyenXe = form.get("maChuyenXe");
            String maGhe = form.get("maGhe");

            if (isEmpty(maKhachHang) || isEmpty(maChuyenXe) || isEmpty(maGhe)) {
                sendJson(exchange, 400,
                        "{\"error\":\"Thiếu thông tin đặt vé.\"}");
                return;
            }

            Ve ve = bookingService.datVe(maKhachHang, maChuyenXe, maGhe);

            String json = "{"
                    + "\"maVe\":\"" + escapeJson(ve.getMaVe()) + "\","
                    + "\"maKhachHang\":\""
                    + escapeJson(ve.getMaKhachHang()) + "\","
                    + "\"maChuyenXe\":\""
                    + escapeJson(ve.getMaChuyenXe()) + "\","
                    + "\"maGhe\":\"" + escapeJson(ve.getMaGhe()) + "\","
                    + "\"giaVeCuoiCung\":" + ve.getGiaVeCuoiCung() + ","
                    + "\"trangThaiThanhToan\":\""
                    + escapeJson(ve.getTrangThaiThanhToan()) + "\","
                    + "\"thoiGianDat\":\""
                    + escapeJson(ve.getThoiGianDat()) + "\""
                    + "}";

            sendJson(exchange, 201, json);
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, errorJson(e.getMessage()));
        } catch (IllegalStateException e) {
            sendJson(exchange, 409, errorJson(e.getMessage()));
        }
    }

    private void handleTrips(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Phương thức không được hỗ trợ.\"}");
            return;
        }

        Map<String, String> query = readQuery(
                exchange.getRequestURI().getRawQuery()
        );
        List<ChuyenXe> danhSach = bookingService.timChuyenXeTheoTuyen(
                query.get("diemDi"),
                query.get("diemDen")
        );

        sendJson(exchange, 200, chuyenXeToJson(danhSach));
    }

    private void handleTripSeats(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Phương thức không được hỗ trợ.\"}");
            return;
        }

        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 5 || !"seats".equals(pathParts[4])) {
            sendJson(exchange, 404,
                    "{\"error\":\"Đường dẫn không tồn tại.\"}");
            return;
        }

        String maChuyenXe = pathParts[3];

        try {
            List<Ghe> danhSachGhe =
                    bookingService.layDanhSachGhe(maChuyenXe);
            sendJson(exchange, 200, gheToJson(danhSachGhe));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 404, errorJson(e.getMessage()));
        }
    }

    private String chuyenXeToJson(List<ChuyenXe> danhSachChuyenXe) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < danhSachChuyenXe.size(); i++) {
            ChuyenXe chuyenXe = danhSachChuyenXe.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"maChuyenXe\":\"")
                    .append(escapeJson(chuyenXe.getMaChuyenXe()))
                    .append("\",")
                    .append("\"diemDi\":\"")
                    .append(escapeJson(chuyenXe.getDiemDi()))
                    .append("\",")
                    .append("\"diemDen\":\"")
                    .append(escapeJson(chuyenXe.getDiemDen()))
                    .append("\",")
                    .append("\"thoiGianKhoiHanh\":\"")
                    .append(escapeJson(chuyenXe.getThoiGianKhoiHanh()))
                    .append("\",")
                    .append("\"bienSoXe\":\"")
                    .append(escapeJson(chuyenXe.getBienSoXe()))
                    .append("\",")
                    .append("\"tongSoGhe\":")
                    .append(chuyenXe.getTongSoGhe())
                    .append(",")
                    .append("\"giaVeCoBan\":")
                    .append(chuyenXe.getGiaVeCoBan())
                    .append("}");
        }

        return json.append("]").toString();
    }

    private String veToJson(List<Ve> danhSachVe) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < danhSachVe.size(); i++) {
            Ve ve = danhSachVe.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"maVe\":\"")
                    .append(escapeJson(ve.getMaVe()))
                    .append("\",")
                    .append("\"maKhachHang\":\"")
                    .append(escapeJson(ve.getMaKhachHang()))
                    .append("\",")
                    .append("\"maChuyenXe\":\"")
                    .append(escapeJson(ve.getMaChuyenXe()))
                    .append("\",")
                    .append("\"maGhe\":\"")
                    .append(escapeJson(ve.getMaGhe()))
                    .append("\",")
                    .append("\"giaVeCuoiCung\":")
                    .append(ve.getGiaVeCuoiCung())
                    .append(",")
                    .append("\"trangThaiThanhToan\":\"")
                    .append(escapeJson(ve.getTrangThaiThanhToan()))
                    .append("\",")
                    .append("\"thoiGianDat\":\"")
                    .append(escapeJson(ve.getThoiGianDat()))
                    .append("\"")
                    .append("}");
        }

        return json.append("]").toString();
    }

    private String gheToJson(List<Ghe> danhSachGhe) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < danhSachGhe.size(); i++) {
            Ghe ghe = danhSachGhe.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"maGhe\":\"")
                    .append(escapeJson(ghe.getMaGhe()))
                    .append("\",")
                    .append("\"soGhe\":")
                    .append(ghe.getSoGhe())
                    .append(",")
                    .append("\"loaiGhe\":\"")
                    .append(escapeJson(ghe.getLoaiGhe()))
                    .append("\",")
                    .append("\"trangThaiGhe\":\"")
                    .append(escapeJson(ghe.getTrangThaiGhe()))
                    .append("\",")
                    .append("\"phuPhi\":")
                    .append(ghe.getPhuPhi())
                    .append("}");
        }

        return json.append("]").toString();
    }

    private Map<String, String> readForm(HttpExchange exchange)
            throws IOException {
        StringBuilder body = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        exchange.getRequestBody(),
                        StandardCharsets.UTF_8
                ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        Map<String, String> form = new HashMap<>();
        String[] pairs = body.toString().split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";
            form.put(key, value);
        }

        return form;
    }

    private Map<String, String> readQuery(String rawQuery)
            throws IOException {
        Map<String, String> query = new LinkedHashMap<>();

        if (rawQuery == null || rawQuery.isEmpty()) {
            return query;
        }

        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";
            query.put(key, value);
        }

        return query;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );
    }

    private void sendJson(HttpExchange exchange, int status, String json)
            throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private String errorJson(String message) {
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
