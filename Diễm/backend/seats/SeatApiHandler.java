package com.group3.busticket.seats;

import com.group3.busticket.model.ChuyenXe;
import com.group3.busticket.model.Ghe;
import com.group3.busticket.trips.TripService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** API xem và quản lý ghế theo chuyến xe. */
public class SeatApiHandler implements HttpHandler {
    private final TripService tripService;
    private final SeatService seatService;

    public SeatApiHandler(TripService tripService, SeatService seatService) {
        if (tripService == null || seatService == null) {
            throw new IllegalArgumentException("Các service ghế/chuyến không được để trống.");
        }
        this.tripService = tripService;
        this.seatService = seatService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String[] path = exchange.getRequestURI().getPath().split("/");
        // /api/trips/{maChuyenXe}/seats[/summary|/{maGhe}]
        if (path.length < 5 || !"seats".equals(path[4])) {
            sendJson(exchange, 404, errorJson("Đường dẫn ghế không tồn tại."));
            return;
        }

        try {
            ChuyenXe chuyenXe = tripService.timTheoMa(
                    URLDecoder.decode(path[3], "UTF-8")
            );
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange, chuyenXe, path);
            } else if ("POST".equalsIgnoreCase(method) && path.length == 5) {
                handleAdd(exchange, chuyenXe);
            } else if ("PUT".equalsIgnoreCase(method) && path.length == 6) {
                handleUpdate(exchange, chuyenXe,
                        URLDecoder.decode(path[5], "UTF-8"));
            } else if ("DELETE".equalsIgnoreCase(method) && path.length == 6) {
                seatService.xoaGhe(chuyenXe,
                        URLDecoder.decode(path[5], "UTF-8"));
                sendJson(exchange, 200, "{\"message\":\"Đã xóa ghế.\"}");
            } else {
                sendJson(exchange, 405,
                        errorJson("Phương thức hoặc đường dẫn ghế không được hỗ trợ."));
            }
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, errorJson("Số ghế hoặc phụ phí không hợp lệ."));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, errorJson(e.getMessage()));
        } catch (IllegalStateException e) {
            sendJson(exchange, 409, errorJson(e.getMessage()));
        }
    }

    private void handleGet(
            HttpExchange exchange,
            ChuyenXe chuyenXe,
            String[] path
    ) throws IOException {
        if (path.length == 6 && "summary".equals(path[5])) {
            String json = "{"
                    + "\"tongSoGhe\":" + chuyenXe.getDanhSachGhe().size() + ","
                    + "\"gheConTrong\":" + seatService.demGheConTrong(chuyenXe) + ","
                    + "\"gheDaDat\":" + seatService.demGheDaDat(chuyenXe) + ","
                    + "\"gheVip\":" + seatService.demGheVip(chuyenXe)
                    + "}";
            sendJson(exchange, 200, json);
            return;
        }
        if (path.length != 5) {
            sendJson(exchange, 404, errorJson("Đường dẫn ghế không tồn tại."));
            return;
        }

        Map<String, String> query = parseQuery(
                exchange.getRequestURI().getRawQuery()
        );
        List<Ghe> danhSach = seatService.layDanhSachGhe(chuyenXe);
        String trangThai = query.get("trangThai");
        String loaiGhe = query.get("loaiGhe");
        if (trangThai != null && !trangThai.isEmpty()) {
            danhSach.retainAll(seatService.layGheTheoTrangThai(chuyenXe, trangThai));
        }
        if (loaiGhe != null && !loaiGhe.isEmpty()) {
            danhSach.retainAll(seatService.layGheTheoLoai(chuyenXe, loaiGhe));
        }
        if ("soGhe".equalsIgnoreCase(query.get("sapXep"))) {
            danhSach = seatService.sapXepTheoSoGhe(chuyenXe);
            if (trangThai != null && !trangThai.isEmpty()) {
                danhSach.retainAll(seatService.layGheTheoTrangThai(chuyenXe, trangThai));
            }
            if (loaiGhe != null && !loaiGhe.isEmpty()) {
                danhSach.retainAll(seatService.layGheTheoLoai(chuyenXe, loaiGhe));
            }
        }
        sendJson(exchange, 200, gheToJson(danhSach));
    }

    private void handleAdd(HttpExchange exchange, ChuyenXe chuyenXe)
            throws IOException {
        Map<String, String> form = readForm(exchange);
        String maGhe = form.get("maGhe");
        String loaiGhe = form.get("loaiGhe");
        String trangThai = form.containsKey("trangThaiGhe")
                ? form.get("trangThaiGhe") : "CON_TRONG";
        int soGhe = Integer.parseInt(form.get("soGhe"));
        long phuPhi = Long.parseLong(
                form.containsKey("phuPhi") ? form.get("phuPhi") : "0"
        );

        Ghe ghe = new Ghe(maGhe, soGhe, loaiGhe, trangThai, phuPhi);
        seatService.themGhe(chuyenXe, ghe);
        sendJson(exchange, 201, gheToJson(motGhe(ghe)));
    }

    private void handleUpdate(
            HttpExchange exchange,
            ChuyenXe chuyenXe,
            String maGhe
    ) throws IOException {
        Map<String, String> form = readForm(exchange);
        String loaiGhe = form.get("loaiGhe");
        long phuPhi = Long.parseLong(
                form.containsKey("phuPhi") ? form.get("phuPhi") : "0"
        );
        seatService.capNhatLoaiGhe(chuyenXe, maGhe, loaiGhe, phuPhi);
        sendJson(exchange, 200,
                gheToJson(motGhe(seatService.timGhe(chuyenXe, maGhe))));
    }

    private List<Ghe> motGhe(Ghe ghe) {
        List<Ghe> ketQua = new ArrayList<>();
        ketQua.add(ghe);
        return ketQua;
    }

    private String gheToJson(List<Ghe> danhSach) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < danhSach.size(); i++) {
            if (i > 0) json.append(",");
            Ghe ghe = danhSach.get(i);
            json.append("{")
                    .append("\"maGhe\":\"").append(escape(ghe.getMaGhe())).append("\",")
                    .append("\"soGhe\":").append(ghe.getSoGhe()).append(",")
                    .append("\"loaiGhe\":\"").append(escape(ghe.getLoaiGhe())).append("\",")
                    .append("\"trangThaiGhe\":\"").append(escape(ghe.getTrangThaiGhe())).append("\",")
                    .append("\"phuPhi\":").append(ghe.getPhuPhi())
                    .append("}");
        }
        return json.append("]").toString();
    }

    private Map<String, String> parseQuery(String rawQuery) throws IOException {
        Map<String, String> query = new HashMap<>();
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

    private Map<String, String> readForm(HttpExchange exchange)
            throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return parseQuery(body.toString());
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"
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
