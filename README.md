# QlyDatVeXeKhach – Hệ thống đặt vé xe khách

Website quản lý khách hàng, chuyến xe, ghế, đặt vé và thanh toán.
## Công nghệ và công cụ

- Backend: Java 8
- Frontend: React
- IDE: Visual Studio Code
- Quản lý mã nguồn: Git và GitHub
- Dữ liệu: file trong thư mục `data/`

Cả nhóm cần dùng thống nhất Java 8. Frontend cần cài Node.js; nhóm sẽ thống nhất phiên bản Node.js trước khi khởi tạo giao diện.

## Cấu trúc dự án

```text
QlyDatVeXeKhach/
├── backend/
│   └── src/main/
│       ├── java/com/group3/busticket/
│       │   ├── Main.java
│       │   ├── model/     # Dữ liệu dùng chung
│       │   │   ├── KhachHang.java
│       │   │   ├── ChuyenXe.java
│       │   │   ├── Ghe.java
│       │   │   ├── Ve.java
│       │   │   └── ThanhToan.java
│       │   ├── customers/     # Thành viên 2: Quản lý khách hàng
│       │   ├── trips/         # Thành viên 3: Quản lý chuyến xe
│       │   ├── seats/         # Thành viên 4: Quản lý ghế
│       │   ├── bookings/      # Thành viên 1: Đặt vé và xem vé đã bán trong ngày
│       │   ├── payments/      # Thành viên 5: Thanh toán
│       │   └── common/        # Đọc/ghi file, tạo mã, tiện ích chung
│       └── resources/
├── frontend/src/
│   ├── components/            # Menu, thanh điều hướng dùng chung
│   ├── customers/             # Thành viên 2: Giao diện khách hàng
│   ├── trips/                 # Thành viên 3: Giao diện chuyến xe
│   ├── seats/                 # Thành viên 4: Giao diện ghế
│   ├── bookings/              # Thành viên 1: Giao diện đặt vé, vé bán trong ngày
│   └── payments/              # Thành viên 5: Giao diện thanh toán
├── data/                      # File lưu dữ liệu
├── docs/                      # Báo cáo, hướng dẫn API, sơ đồ lớp
└── README.md
```

## Phân công

| Người        | Phần phụ trách                                         | Thư mục chính                                          | Nhánh                     |
| ------------ | ------------------------------------------------------ | ------------------------------------------------------ | ------------------------- |
| Nhóm trưởng  | Dữ liệu dùng chung, đặt vé và xem vé đã bán trong ngày | `model/`, `common/`, `bookings/` ở backend và frontend | `feature/leader-bookings` |
| Thành viên 2 | Quản lý khách hàng                                     | `customers/` ở backend và frontend                     | `feature/customers`       |
| Thành viên 3 | Quản lý chuyến xe                                      | `trips/` ở backend và frontend                         | `feature/trips`           |
| Thành viên 4 | Quản lý ghế                                            | `seats/` ở backend và frontend                         | `feature/seats`           |
| Thành viên 5 | Thanh toán                                             | `payments/` ở backend và frontend                      | `feature/payments`        |

Nhóm trưởng tạo và thống nhất các lớp dữ liệu ban đầu trong `model/`. Các thành viên cần trao đổi trước khi sửa lớp dùng chung hoặc định dạng file dữ liệu.

## Lần đầu lấy dự án và tạo nhánh cá nhân

Cài Git và VS Code, sau đó mở Terminal trong VS Code. Mỗi thành viên chạy:

```bash
git clone https://github.com/TranVanDung265/QlyDatVeXeKhach.git
cd QlyDatVeXeKhach
git switch main
git pull origin main
```

Sau đó, mỗi người chỉ chạy **một lệnh tạo nhánh** tương ứng với phần việc của mình:

**Nhóm trưởng:**

```bash
git switch -c feature/leader-bookings
```

**Thành viên 2:**

```bash
git switch -c feature/customers
```

**Thành viên 3:**

```bash
git switch -c feature/trips
```

**Thành viên 4:**

```bash
git switch -c feature/seats
```

**Thành viên 5:**

```bash
git switch -c feature/payments
```

Đẩy nhánh lên GitHub lần đầu. Thay `TEN_NHANH_CUA_BAN` bằng tên nhánh của mình:

```bash
git push -u origin TEN_NHANH_CUA_BAN
```

Ví dụ nhóm trưởng chạy:

```bash
git push -u origin feature/leader-bookings
```

## Quy trình mỗi lần code

Trước khi sửa code, kiểm tra nhánh hiện tại:

```bash
git branch --show-current
```

Kết quả phải là nhánh cá nhân của bạn, không phải `main`. Sau khi làm xong một phần:

```bash
git add .
git commit -m "Mo ta ngan gon thay doi"
git push
```

Ví dụ:

```bash
git add .
git commit -m "Add customer management"
git push
```

## Cập nhật thay đổi mới từ main

Sau khi code mới được gộp vào `main`, cập nhật nhánh cá nhân của mình:

```bash
git switch main
git pull origin main
git switch TEN_NHANH_CUA_BAN
git merge main
```

Ví dụ thành viên 2 chạy:

```bash
git switch main
git pull origin main
git switch feature/customers
git merge main
```

Lệnh `git merge main` ở đây đưa thay đổi từ `main` vào nhánh cá nhân đang đứng. Nếu có file chưa commit, hãy commit chúng trước khi đổi nhánh. Nếu Git báo xung đột, báo nhóm trưởng để cùng xử lý.

## Gửi code để gộp vào main

Khi hoàn thành một phần:

1. Đẩy commit lên nhánh cá nhân bằng `git push`.
2. Trên GitHub, tạo Pull Request từ nhánh cá nhân vào `main`.
3. Nhóm trưởng kiểm tra code rồi mới gộp Pull Request.

Không commit hoặc push trực tiếp lên `main` khi đang phát triển chức năng.

## Dữ liệu dùng chung dự kiến

- `KhachHang`: `maKhachHang`, `hoTen`, `soDienThoai`, `email`, `loaiKhach`
- `ChuyenXe`: `maChuyenXe`, `diemDi`, `diemDen`, `thoiGianKhoiHanh`, `bienSoXe`, danh sách ghế
- `Ghe`: `maGhe`, `soGhe`, `loaiGhe`, `trangThaiGhe`
- `Ve`: `maVe`, thông tin khách hàng, chuyến xe, ghế, `giaVeCuoiCung`, `trangThaiThanhToan`
- `ThanhToan`: `maThanhToan`, `maVe`, `phuongThuc`, `soTien`

Các giá trị trạng thái, quy tắc tính giá và định dạng lưu file sẽ được thống nhất trong `docs/` trước khi các chức năng sử dụng chung.
