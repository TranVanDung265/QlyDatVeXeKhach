import { useEffect, useState } from "react";
import "./BookingPage.css";

const seatNumbers = [
  ["1A", "1B", "1C", "1D"],
  ["2A", "2B", "2C", "2D"],
  ["3A", "3B", "3C", "3D"],
  ["4A", "4B", "4C", "4D"],
];

function formatTien(soTien) {
  return new Intl.NumberFormat("vi-VN").format(soTien) + " đ";
}

function BookingPage() {
  const [customers, setCustomers] = useState([]);
  const [dangTaiKhachHang, setDangTaiKhachHang] = useState(true);
  const [loiTaiKhachHang, setLoiTaiKhachHang] = useState("");
  const [trips, setTrips] = useState([]);
  const [dangTaiChuyenXe, setDangTaiChuyenXe] = useState(true);
  const [loiTaiChuyenXe, setLoiTaiChuyenXe] = useState("");

  const [maKhachHang, setMaKhachHang] = useState("");
  const [maChuyenXe, setMaChuyenXe] = useState("");
  const [soGheDangChon, setSoGheDangChon] = useState("");
  const [thongBao, setThongBao] = useState("");

  const [gheTheoChuyen, setGheTheoChuyen] = useState({});

  useEffect(() => {
    let conHieuLuc = true;

    async function taiDanhSachKhachHang() {
      try {
        const response = await fetch("http://localhost:8080/api/customers");
        const result = await response.json();

        if (!response.ok) {
          throw new Error(result.error || "Không tải được khách hàng.");
        }

        if (conHieuLuc) {
          setCustomers(result);
          setLoiTaiKhachHang("");
        }
      } catch (error) {
        if (conHieuLuc) {
          setLoiTaiKhachHang(error.message);
        }
      } finally {
        if (conHieuLuc) {
          setDangTaiKhachHang(false);
        }
      }
    }

    taiDanhSachKhachHang();

    return () => {
      conHieuLuc = false;
    };
  }, []);

  useEffect(() => {
    let conHieuLuc = true;

    async function taiDanhSachChuyenXe() {
      try {
        const response = await fetch("http://localhost:8080/api/trips");
        const result = await response.json();

        if (!response.ok) {
          throw new Error(result.error || "Không tải được chuyến xe.");
        }

        if (conHieuLuc) {
          setTrips(result);
          setLoiTaiChuyenXe("");
        }
      } catch (error) {
        if (conHieuLuc) {
          setLoiTaiChuyenXe(error.message);
        }
      } finally {
        if (conHieuLuc) {
          setDangTaiChuyenXe(false);
        }
      }
    }

    taiDanhSachChuyenXe();

    return () => {
      conHieuLuc = false;
    };
  }, []);

  useEffect(() => {
    if (!maChuyenXe) {
      return;
    }

    let conHieuLuc = true;

    async function taiDanhSachGhe() {
      try {
        const response = await fetch(
          `http://localhost:8080/api/trips/${maChuyenXe}/seats`,
        );
        const result = await response.json();

        if (!response.ok) {
          throw new Error(result.error || "Không tải được danh sách ghế.");
        }

        if (conHieuLuc) {
          setGheTheoChuyen((hienTai) => ({
            ...hienTai,
            [maChuyenXe]: result.map((item) => ({
              ...item,
              soGhe: item.maGhe,
            })),
          }));
        }
      } catch (error) {
        if (conHieuLuc) {
          setThongBao(error.message);
        }
      }
    }

    taiDanhSachGhe();

    return () => {
      conHieuLuc = false;
    };
  }, [maChuyenXe]);

  const khachHang = customers.find((item) => item.maKhachHang === maKhachHang);
  const chuyenXe = trips.find(
    (item) => item.maChuyenXe === maChuyenXe,
  );
  const danhSachGhe = maChuyenXe
    ? gheTheoChuyen[maChuyenXe] || []
    : [];
  const ghe = danhSachGhe.find((item) => item.soGhe === soGheDangChon);

  const phanTramGiam =
    khachHang?.loaiKhach === "VIP"
      ? 10
      : khachHang?.loaiKhach === "THANH_VIEN"
        ? 5
        : 0;

  const giaVeTruocGiam = chuyenXe
    ? chuyenXe.giaVeCoBan + (ghe?.phuPhi ?? 0)
    : 0;

  const giaVeCuoiCung = Math.round(
    (giaVeTruocGiam * (100 - phanTramGiam)) / 100,
  );

  async function xacNhanDatVe() {
    if (!khachHang) {
      setThongBao("Vui lòng chọn khách hàng.");
      return;
    }

    if (!chuyenXe) {
      setThongBao("Vui lòng chọn chuyến xe.");
      return;
    }

    if (!ghe || ghe.trangThaiGhe !== "CON_TRONG") {
      setThongBao("Vui lòng chọn một ghế còn trống.");
      return;
    }

    try {
      const formData = new URLSearchParams({
        maKhachHang,
        maChuyenXe,
        maGhe: soGheDangChon,
      });

      const response = await fetch("http://localhost:8080/api/bookings", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: formData.toString(),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Đặt vé không thành công.");
      }

      setGheTheoChuyen((hienTai) => ({
        ...hienTai,
        [maChuyenXe]: (hienTai[maChuyenXe] || danhSachGhe).map((item) =>
          item.soGhe === soGheDangChon
            ? { ...item, trangThaiGhe: "DA_DAT" }
            : item,
        ),
      }));

      setThongBao(
        `Đặt vé thành công! Mã vé: ${result.maVe}. Giá vé: ${formatTien(result.giaVeCuoiCung)}. Vé chưa thanh toán.`,
      );
      setSoGheDangChon("");
    } catch (error) {
      setThongBao(
        error.message === "Failed to fetch"
          ? "Không kết nối được backend Java. Hãy kiểm tra Terminal chạy API."
          : error.message,
      );
    }
  }

  return (
    <section className="booking-page">
      <div className="booking-heading">
        <div>
          <h1>Đặt vé</h1>
          <p>Chọn khách hàng, chuyến xe và ghế cần đặt.</p>
        </div>
      </div>

      <div className="booking-layout">
        <article className="booking-card">
          <h2>Thông tin đặt vé</h2>

          <label className="booking-field">
            Khách hàng
            <select
              value={maKhachHang}
              onChange={(event) => {
                setMaKhachHang(event.target.value);
                setThongBao("");
              }}
              disabled={dangTaiKhachHang || customers.length === 0}
            >
              <option value="">
                {dangTaiKhachHang
                  ? "Đang tải khách hàng..."
                  : "-- Chọn khách hàng --"}
              </option>
              {customers.map((item) => (
                <option key={item.maKhachHang} value={item.maKhachHang}>
                  {item.hoTen} — {item.loaiKhach}
                </option>
              ))}
            </select>
          </label>

          {loiTaiKhachHang && (
            <p className="booking-message" role="alert">
              {loiTaiKhachHang}
            </p>
          )}

          <label className="booking-field">
            Chuyến xe
            <select
              value={maChuyenXe}
              onChange={(event) => {
                setMaChuyenXe(event.target.value);
                setSoGheDangChon("");
                setThongBao("");
              }}
            >
              <option value="">
                {dangTaiChuyenXe
                  ? "Đang tải chuyến xe..."
                  : "-- Chọn chuyến xe --"}
              </option>
              {trips.map((item) => (
                <option key={item.maChuyenXe} value={item.maChuyenXe}>
                  {item.diemDi} → {item.diemDen} | {item.thoiGianKhoiHanh}
                </option>
              ))}
            </select>
          </label>

          {loiTaiChuyenXe && (
            <p className="booking-message" role="alert">
              {loiTaiChuyenXe}
            </p>
          )}

          {chuyenXe && (
            <div className="booking-trip-info">
              <strong>
                {chuyenXe.diemDi} → {chuyenXe.diemDen}
              </strong>
              <span>Mã chuyến: {chuyenXe.maChuyenXe}</span>
              <span>Khởi hành: {chuyenXe.thoiGianKhoiHanh}</span>
              <span>Biển số xe: {chuyenXe.bienSoXe}</span>
            </div>
          )}

          <div className="booking-price">
            <div>
              <span>Giá vé cơ bản</span>
              <strong>{formatTien(chuyenXe?.giaVeCoBan ?? 0)}</strong>
            </div>
            <div>
              <span>Phụ phí ghế</span>
              <strong>{formatTien(ghe?.phuPhi ?? 0)}</strong>
            </div>
            <div>
              <span>Ưu đãi khách hàng</span>
              <strong>{phanTramGiam}%</strong>
            </div>
            <div className="booking-total">
              <span>Giá vé cuối cùng</span>
              <strong>{formatTien(giaVeCuoiCung)}</strong>
            </div>
          </div>

          <button
            className="booking-submit"
            onClick={xacNhanDatVe}
            type="button"
            disabled={!customers.length}
          >
            Xác nhận đặt vé
          </button>

          {thongBao && (
            <p className="booking-message" role="status">
              {thongBao}
            </p>
          )}
        </article>

        <article className="booking-card">
          <div className="booking-seat-heading">
            <div>
              <h2>Chọn ghế</h2>
              <p>Chọn chuyến xe trước để xem sơ đồ ghế.</p>
            </div>

            <div className="booking-legend">
              <span>
                <i className="seat-dot free" /> Ghế trống
              </span>
              <span>
                <i className="seat-dot chosen" /> Đang chọn
              </span>
              <span>
                <i className="seat-dot taken" /> Đã đặt
              </span>
            </div>
          </div>

          {chuyenXe ? (
            <div className="booking-bus">
              <div className="booking-driver">
                ĐẦU XE <span>◉</span>
              </div>

              <div className="booking-seat-grid">
                {seatNumbers.flat().map((soGhe) => {
                  const thongTinGhe = danhSachGhe.find(
                    (item) => item.soGhe === soGhe,
                  );
                  const daDat = thongTinGhe?.trangThaiGhe === "DA_DAT";
                  const dangChon = soGheDangChon === soGhe;

                  return (
                    <button
                      key={soGhe}
                      type="button"
                      disabled={daDat}
                      className={[
                        "booking-seat",
                        thongTinGhe?.loaiGhe === "GHE_VIP" ? "vip" : "",
                        daDat ? "taken" : "",
                        dangChon ? "chosen" : "",
                      ].join(" ")}
                      onClick={() => {
                        setSoGheDangChon(soGhe);
                        setThongBao("");
                      }}
                      aria-label={`Ghế ${soGhe}${daDat ? ", đã đặt" : ""}`}
                    >
                      {soGhe}
                    </button>
                  );
                })}
              </div>
            </div>
          ) : (
            <p className="booking-empty">Chưa chọn chuyến xe.</p>
          )}
        </article>
      </div>
    </section>
  );
}

export default BookingPage;
