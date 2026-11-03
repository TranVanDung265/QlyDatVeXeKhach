package com.group3.busticket.trips;

import com.group3.busticket.model.ChuyenXe;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API tra cứu chuyến xe: lọc tuyến, ngày, khoảng giá và sắp xếp. */
public class TripApiHandler implements HttpHandler {
    private final TripService tripService;

    public TripApiHandler(TripService tripService) {
        if (tripService == null) {
            throw new IllegalArgumentException("TripService không được để trống.");
        }
        this.tripService = tripService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"API chuyến xe hiện chỉ hỗ trợ GET.\"}");
            return;
        }

        try {
            Map<String, String> query = parseQuery(
                    exchange.getRequestURI().getRawQuery()
            );
            List<ChuyenXe> ketQua = tripService.timChuyenXeTheoTuyen(
                    query.get("diemDi"), query.get("diemDen")
            );

            String ngay = query.get("ngay");
            if (ngay != null && !ngay.trim().isEmpty()) {
                List<ChuyenXe> theoNgay = tripService.layChuyenXeTheoNgay(ngay);
                ketQua.retainAll(theoNgay);
            }

            String giaTu = query.get("giaTu");
            String giaDen = query.get("giaDen");
            if (giaTu != null || giaDen != null) {
                long min = giaTu == null || giaTu.isEmpty()
                        ? 0 : Long.parseLong(giaTu);
                long max = giaDen == null || giaDen.isEmpty()
                        ? Long.MAX_VALUE : Long.parseLong(giaDen);
                List<ChuyenXe> theoGia = tripService.locTheoKhoangGia(min, max);
                ketQua.retainAll(theoGia);
            }

            String sapXep = query.get("sapXep");
            if ("gio".equalsIgnoreCase(sapXep)) {
                ketQua.sort(Comparator.comparing(
                        ChuyenXe::getThoiGianKhoiHanh,
                        Comparator.nullsLast(String::compareTo)
                ));
            } else if ("gia".equalsIgnoreCase(sapXep)) {
                ketQua.sort(Comparator.comparingLong(ChuyenXe::getGiaVeCoBan));
            } else if (sapXep != null && !sapXep.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "sapXep chỉ nhận giá trị gio hoặc gia."
                );
            }

            sendJson(exchange, 200, danhSachToJson(ketQua));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400,
                    "{\"error\":\"Khoảng giá phải là số nguyên VND.\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, errorJson(e.getMessage()));
        }
    }

    private Map<String, String> parseQuery(String rawQuery) throws IOException {
        Map<String, String> query = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return query;

        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8") : "";
            query.put(key, value);
        }
        return query;
    }

    private String danhSachToJson(List<ChuyenXe> danhSach) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < danhSach.size(); i++) {
            ChuyenXe chuyenXe = danhSach.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                    .append("\"maChuyenXe\":\"").append(escape(chuyenXe.getMaChuyenXe())).append("\",")
                    .append("\"diemDi\":\"").append(escape(chuyenXe.getDiemDi())).append("\",")
                    .append("\"diemDen\":\"").append(escape(chuyenXe.getDiemDen())).append("\",")
                    .append("\"thoiGianKhoiHanh\":\"").append(escape(chuyenXe.getThoiGianKhoiHanh())).append("\",")
                    .append("\"bienSoXe\":\"").append(escape(chuyenXe.getBienSoXe())).append("\",")
                    .append("\"tongSoGhe\":").append(chuyenXe.getTongSoGhe()).append(",")
                    .append("\"giaVeCoBan\":").append(chuyenXe.getGiaVeCoBan())
                    .append("}");
        }
        return json.append("]").toString();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );
    }

    private void sendJson(HttpExchange exchange, int status, String json)
            throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private String errorJson(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
