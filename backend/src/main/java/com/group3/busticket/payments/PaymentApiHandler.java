package com.group3.busticket.payments;

import com.group3.busticket.model.ThanhToan;
import com.group3.busticket.model.Ve;
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

/** API xem danh sách thanh toán và thanh toán vé. */
public class PaymentApiHandler implements HttpHandler {
    private final PaymentService paymentService;

    public PaymentApiHandler(PaymentService paymentService) {
        if (paymentService == null) {
            throw new IllegalArgumentException("PaymentService không được để trống.");
        }
        this.paymentService = paymentService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("GET".equalsIgnoreCase(method)
                    && "/api/payments/pending".equals(path)) {
                sendJson(exchange, 200, veChuaThanhToanToJson(
                        paymentService.layVeChuaThanhToan()
                ));
                return;
            }

            if ("GET".equalsIgnoreCase(method)
                    && "/api/payments".equals(path)) {
                sendJson(exchange, 200, thanhToanToJson(
                        paymentService.layDanhSachThanhToan()
                ));
                return;
            }

            if ("POST".equalsIgnoreCase(method)
                    && "/api/payments".equals(path)) {
                Map<String, String> form = readForm(exchange);
                String maVe = form.get("maVe");
                String phuongThuc = form.get("phuongThuc");

                if (isEmpty(maVe) || isEmpty(phuongThuc)) {
                    sendJson(exchange, 400,
                            "{\"error\":\"Vui lòng nhập mã vé và phương thức thanh toán.\"}");
                    return;
                }

                ThanhToan thanhToan = paymentService.thanhToanVe(
                        maVe.trim(), phuongThuc.trim()
                );
                sendJson(exchange, 201, motThanhToanToJson(thanhToan));
                return;
            }

            sendJson(exchange, 405,
                    "{\"error\":\"Phương thức hoặc đường dẫn không được hỗ trợ.\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, errorJson(e.getMessage()));
        } catch (IllegalStateException e) {
            sendJson(exchange, 409, errorJson(e.getMessage()));
        }
    }

    private String veChuaThanhToanToJson(List<Ve> danhSachVe) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < danhSachVe.size(); i++) {
            Ve ve = danhSachVe.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                    .append("\"maVe\":\"").append(escapeJson(ve.getMaVe())).append("\",")
                    .append("\"maKhachHang\":\"").append(escapeJson(ve.getMaKhachHang())).append("\",")
                    .append("\"maChuyenXe\":\"").append(escapeJson(ve.getMaChuyenXe())).append("\",")
                    .append("\"maGhe\":\"").append(escapeJson(ve.getMaGhe())).append("\",")
                    .append("\"giaVeCuoiCung\":").append(ve.getGiaVeCuoiCung()).append(",")
                    .append("\"trangThaiThanhToan\":\"")
                    .append(escapeJson(ve.getTrangThaiThanhToan())).append("\"")
                    .append("}");
        }
        return json.append("]").toString();
    }

    private String thanhToanToJson(List<ThanhToan> danhSach) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < danhSach.size(); i++) {
            if (i > 0) json.append(",");
            json.append(motThanhToanToJson(danhSach.get(i)));
        }
        return json.append("]").toString();
    }

    private String motThanhToanToJson(ThanhToan thanhToan) {
        return "{"
                + "\"maThanhToan\":\"" + escapeJson(thanhToan.getMaThanhToan()) + "\","
                + "\"maVe\":\"" + escapeJson(thanhToan.getMaVe()) + "\","
                + "\"phuongThuc\":\"" + escapeJson(thanhToan.getPhuongThuc()) + "\","
                + "\"soTien\":" + thanhToan.getSoTien()
                + "}";
    }

    private Map<String, String> readForm(HttpExchange exchange)
            throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }

        Map<String, String> form = new HashMap<>();
        for (String pair : body.toString().split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8") : "";
            form.put(key, value);
        }
        return form;
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
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
