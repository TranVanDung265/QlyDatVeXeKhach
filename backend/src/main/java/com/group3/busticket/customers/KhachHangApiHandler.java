package com.group3.busticket.customers;

import com.group3.busticket.model.KhachHang;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KhachHangApiHandler implements HttpHandler {
    private static final String DUONG_DAN = "/api/customers";

    private final KhachHangService khachHangService;

    public KhachHangApiHandler(KhachHangService khachHangService) {
        if (khachHangService == null) {
            throw new IllegalArgumentException(
                    "KhachHangService không được để trống."
            );
        }
        this.khachHangService = khachHangService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        themCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String maKhachHang = layMaKhachHangTuDuongDan(path);
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                xuLyGet(exchange, maKhachHang);
            } else if ("POST".equalsIgnoreCase(method)) {
                if (maKhachHang != null) {
                    guiJson(exchange, 404,
                            "{\"error\":\"Đường dẫn không tồn tại.\"}");
                } else {
                    xuLyPost(exchange);
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                if (maKhachHang == null) {
                    guiJson(exchange, 400,
                            "{\"error\":\"Thiếu mã khách hàng.\"}");
                } else {
                    xuLyPut(exchange, maKhachHang);
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (maKhachHang == null) {
                    guiJson(exchange, 400,
                            "{\"error\":\"Thiếu mã khách hàng.\"}");
                } else {
                    khachHangService.xoaKhachHang(maKhachHang);
                    guiJson(exchange, 200,
                            "{\"message\":\"Đã xóa khách hàng.\"}");
                }
            } else {
                guiJson(exchange, 405,
                        "{\"error\":\"Phương thức không được hỗ trợ.\"}");
            }
        } catch (IllegalArgumentException e) {
            guiJson(exchange, 400, errorJson(e.getMessage()));
        } catch (IOException e) {
            guiJson(exchange, 500,
                    "{\"error\":\"Không thể đọc hoặc lưu dữ liệu khách hàng.\"}");
        }
    }

    private void xuLyGet(
            HttpExchange exchange,
            String maKhachHang
    ) throws IOException {
        if (maKhachHang == null) {
            List<KhachHang> danhSach =
                    khachHangService.layDanhSachKhachHang();
            guiJson(exchange, 200, danhSachToJson(danhSach));
        } else {
            KhachHang khachHang =
                    khachHangService.timTheoMa(maKhachHang);
            guiJson(exchange, 200, khachHangToJson(khachHang));
        }
    }

    private void xuLyPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = docForm(exchange);

        KhachHang khachHang = khachHangService.themKhachHang(
                form.get("hoTen"),
                form.get("soDienThoai"),
                form.get("email"),
                form.get("loaiKhach")
        );

        guiJson(exchange, 201, khachHangToJson(khachHang));
    }

    private void xuLyPut(
            HttpExchange exchange,
            String maKhachHang
    ) throws IOException {
        Map<String, String> form = docForm(exchange);

        khachHangService.capNhatKhachHang(
                maKhachHang,
                form.get("hoTen"),
                form.get("soDienThoai"),
                form.get("email"),
                form.get("loaiKhach")
        );

        guiJson(exchange, 200,
                "{\"message\":\"Đã cập nhật khách hàng.\"}");
    }

    private String danhSachToJson(List<KhachHang> danhSach) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < danhSach.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(khachHangToJson(danhSach.get(i)));
        }

        return json.append("]").toString();
    }

    private String khachHangToJson(KhachHang khachHang) {
        return "{"
                + "\"maKhachHang\":\""
                + escapeJson(khachHang.getMaKhachHang()) + "\","
                + "\"hoTen\":\""
                + escapeJson(khachHang.getHoTen()) + "\","
                + "\"soDienThoai\":\""
                + escapeJson(khachHang.getSoDienThoai()) + "\","
                + "\"email\":\""
                + escapeJson(khachHang.getEmail()) + "\","
                + "\"loaiKhach\":\""
                + escapeJson(khachHang.getLoaiKhach()) + "\""
                + "}";
    }

    private Map<String, String> docForm(HttpExchange exchange)
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

        for (String pair : body.toString().split("&")) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";

            form.put(key, value);
        }

        return form;
    }

    private String layMaKhachHangTuDuongDan(String path) {
        if (DUONG_DAN.equals(path)) {
            return null;
        }

        String tienTo = DUONG_DAN + "/";
        if (path.startsWith(tienTo)) {
            String ma = path.substring(tienTo.length());
            if (!ma.isEmpty() && !ma.contains("/")) {
                return ma;
            }
        }

        return null;
    }

    private void themCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );
    }

    private void guiJson(
            HttpExchange exchange,
            int status,
            String json
    ) throws IOException {
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

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}