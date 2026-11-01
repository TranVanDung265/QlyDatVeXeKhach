import { useCallback, useEffect, useState } from "react";
import "./PaymentPage.css";

const API_URL = "http://localhost:8080/api/payments";

function formatTien(soTien) {
  return new Intl.NumberFormat("vi-VN").format(soTien || 0) + " đ";
}

function tenPhuongThuc(phuongThuc) {
  const ten = {
    TIEN_MAT: "Tiền mặt",
    CHUYEN_KHOAN: "Chuyển khoản",
    VI_DIEN_TU: "Ví điện tử",
  };
  return ten[phuongThuc] || phuongThuc;
}

function PaymentPage() {
  const [veChoThanhToan, setVeChoThanhToan] = useState([]);
  const [lichSu, setLichSu] = useState([]);
  const [phuongThucTheoVe, setPhuongThucTheoVe] = useState({});
  const [thongBao, setThongBao] = useState("");
  const [laThongBaoLoi, setLaThongBaoLoi] = useState(false);
  const [dangTai, setDangTai] = useState(true);
  const [maVeDangXuLy, setMaVeDangXuLy] = useState("");

  const taiDuLieu = useCallback(async () => {
    const [pendingResponse, historyResponse] = await Promise.all([
      fetch(`${API_URL}/pending`),
      fetch(API_URL),
    ]);
    const [pendingData, historyData] = await Promise.all([
      pendingResponse.json(),
      historyResponse.json(),
    ]);

    if (!pendingResponse.ok) {
      throw new Error(pendingData.error || "Không tải được vé chờ thanh toán.");
    }
    if (!historyResponse.ok) {
      throw new Error(
        historyData.error || "Không tải được lịch sử thanh toán.",
      );
    }

    setVeChoThanhToan(pendingData);
    setLichSu(historyData);
  }, []);

  useEffect(() => {
    let conHieuLuc = true;

    async function taiLanDau() {
      try {
        await taiDuLieu();
        if (conHieuLuc) {
          setThongBao("");
          setLaThongBaoLoi(false);
        }
      } catch (error) {
        if (conHieuLuc) {
          setThongBao(error.message);
          setLaThongBaoLoi(true);
        }
      } finally {
        if (conHieuLuc) setDangTai(false);
      }
    }

    taiLanDau();
    return () => {
      conHieuLuc = false;
    };
  }, [taiDuLieu]);

  function capNhatPhuongThuc(maVe, phuongThuc) {
    setPhuongThucTheoVe((hienTai) => ({
      ...hienTai,
      [maVe]: phuongThuc,
    }));
  }

  async function xacNhanThanhToan(ve) {
    const phuongThuc = phuongThucTheoVe[ve.maVe] || "TIEN_MAT";
    setMaVeDangXuLy(ve.maVe);
    setThongBao("");

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: new URLSearchParams({
          maVe: ve.maVe,
          phuongThuc,
        }).toString(),
      });
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Thanh toán không thành công.");
      }

      await taiDuLieu();
      setThongBao(
        `Thanh toán vé ${result.maVe} thành công bằng ${tenPhuongThuc(result.phuongThuc)}.`,
      );
      setLaThongBaoLoi(false);
    } catch (error) {
      setThongBao(
        error.message === "Failed to fetch"
          ? "Không kết nối được backend. Hãy kiểm tra Terminal đang chạy API."
          : error.message,
      );
      setLaThongBaoLoi(true);
    } finally {
      setMaVeDangXuLy("");
    }
  }

  return (
    <section className="payment-page">
      <header className="payment-heading">
        <div>
          <span className="payment-eyebrow">THU NGÂN</span>
          <h1>Thanh toán</h1>
          <p>Chọn phương thức và xác nhận thanh toán cho vé đang chờ.</p>
        </div>
        <div className="payment-heading-icon" aria-hidden="true">
          ▤
        </div>
      </header>

      {thongBao && (
        <p
          className={`payment-message ${laThongBaoLoi ? "error" : "success"}`}
          role="status"
        >
          {thongBao}
        </p>
      )}

      <section className="payment-card">
        <div className="payment-section-heading">
          <div>
            <h2>Vé chờ thanh toán</h2>
            <p>Các vé đặt thành công nhưng chưa hoàn tất thanh toán.</p>
          </div>
          <span className="payment-count">{veChoThanhToan.length} vé</span>
        </div>

        <div className="payment-table-wrap">
          <table className="payment-table">
            <thead>
              <tr>
                <th>Mã vé</th>
                <th>Khách hàng</th>
                <th>Chuyến xe</th>
                <th>Ghế</th>
                <th>Số tiền</th>
                <th>Phương thức</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {veChoThanhToan.map((ve) => (
                <tr key={ve.maVe}>
                  <td>
                    <span className="payment-ticket-id">{ve.maVe}</span>
                  </td>
                  <td>{ve.maKhachHang}</td>
                  <td>{ve.maChuyenXe}</td>
                  <td>{ve.maGhe}</td>
                  <td className="payment-amount">
                    {formatTien(ve.giaVeCuoiCung)}
                  </td>
                  <td>
                    <select
                      className="payment-method-select"
                      aria-label={`Phương thức thanh toán vé ${ve.maVe}`}
                      value={phuongThucTheoVe[ve.maVe] || "TIEN_MAT"}
                      onChange={(event) =>
                        capNhatPhuongThuc(ve.maVe, event.target.value)
                      }
                    >
                      <option value="TIEN_MAT">Tiền mặt</option>
                      <option value="CHUYEN_KHOAN">Chuyển khoản</option>
                      <option value="VI_DIEN_TU">Ví điện tử</option>
                    </select>
                  </td>
                  <td>
                    <button
                      className="payment-confirm-button"
                      disabled={Boolean(maVeDangXuLy)}
                      onClick={() => xacNhanThanhToan(ve)}
                      type="button"
                    >
                      {maVeDangXuLy === ve.maVe ? "Đang xử lý..." : "Xác nhận"}
                    </button>
                  </td>
                </tr>
              ))}

              {!dangTai && veChoThanhToan.length === 0 && (
                <tr>
                  <td className="payment-empty" colSpan="7">
                    Hiện không có vé nào đang chờ thanh toán.
                  </td>
                </tr>
              )}
              {dangTai && (
                <tr>
                  <td className="payment-empty" colSpan="7">
                    Đang tải dữ liệu...
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="payment-card">
        <div className="payment-section-heading">
          <div>
            <h2>Lịch sử thanh toán</h2>
            <p>Các giao dịch đã được ghi vào file dữ liệu.</p>
          </div>
          <span className="payment-count">{lichSu.length} giao dịch</span>
        </div>

        <div className="payment-table-wrap">
          <table className="payment-table payment-history-table">
            <thead>
              <tr>
                <th>Mã thanh toán</th>
                <th>Mã vé</th>
                <th>Phương thức</th>
                <th>Số tiền</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {lichSu.map((thanhToan) => (
                <tr key={thanhToan.maThanhToan}>
                  <td>
                    <span className="payment-ticket-id">
                      {thanhToan.maThanhToan}
                    </span>
                  </td>
                  <td>{thanhToan.maVe}</td>
                  <td>{tenPhuongThuc(thanhToan.phuongThuc)}</td>
                  <td className="payment-amount">
                    {formatTien(thanhToan.soTien)}
                  </td>
                  <td>
                    <span className="payment-paid-badge">Đã thanh toán</span>
                  </td>
                </tr>
              ))}
              {!dangTai && lichSu.length === 0 && (
                <tr>
                  <td className="payment-empty" colSpan="5">
                    Chưa có giao dịch nào.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

export default PaymentPage;
