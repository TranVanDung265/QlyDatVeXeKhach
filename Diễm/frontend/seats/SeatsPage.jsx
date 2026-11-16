import { useEffect, useState } from "react";
import "./SeatsPage.css";

const API_URL = "http://localhost:8080/api/trips";

function SeatsPage() {
  const [danhSachChuyen, setDanhSachChuyen] = useState([]);
  const [maChuyenDangChon, setMaChuyenDangChon] = useState("");
  const [danhSachGhe, setDanhSachGhe] = useState([]);
  const [dangTaiChuyen, setDangTaiChuyen] = useState(true);
  const [dangTaiGhe, setDangTaiGhe] = useState(false);
  const [thongBao, setThongBao] = useState("");

  useEffect(() => {
    let conHieuLuc = true;

    fetch(API_URL)
      .then(async (response) => {
        const result = await response.json();
        if (!response.ok) {
          throw new Error(
            result.error || "Không tải được danh sách chuyến xe.",
          );
        }
        return result;
      })
      .then((result) => {
        if (conHieuLuc) setDanhSachChuyen(result);
      })
      .catch((error) => {
        if (conHieuLuc) setThongBao(error.message);
      })
      .finally(() => {
        if (conHieuLuc) setDangTaiChuyen(false);
      });

    return () => {
      conHieuLuc = false;
    };
  }, []);

  async function xemGhe(maChuyenXe) {
    setMaChuyenDangChon(maChuyenXe);
    setDangTaiGhe(true);
    setThongBao("");
    setDanhSachGhe([]);

    try {
      const response = await fetch(
        `${API_URL}/${encodeURIComponent(maChuyenXe)}/seats`,
      );
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.error || "Không tải được sơ đồ ghế.");
      }
      setDanhSachGhe(result);
    } catch (error) {
      setThongBao(error.message);
    } finally {
      setDangTaiGhe(false);
    }
  }

  const chuyenDangChon = danhSachChuyen.find(
    (chuyen) => chuyen.maChuyenXe === maChuyenDangChon,
  );
  const soGheTrong = danhSachGhe.filter(
    (ghe) => ghe.trangThaiGhe === "CON_TRONG",
  ).length;

  return (
    <section className="seats-page">
      <header className="seats-heading">
        <div>
          <span className="seats-eyebrow">QUẢN LÝ CHỖ NGỒI</span>
          <h1>Sơ đồ ghế</h1>
          <p>Chọn chuyến xe để xem ghế còn trống và ghế đã được đặt.</p>
        </div>
        <div className="seats-heading-icon" aria-hidden="true">
          ♧
        </div>
      </header>

      {thongBao && (
        <p className="seats-message" role="status">
          {thongBao}
        </p>
      )}

      <section className="seats-card">
        <div className="seats-card-heading">
          <div>
            <h2>Chọn chuyến xe</h2>
            <p>Chọn một chuyến để xem sơ đồ ghế tương ứng.</p>
          </div>
          <span className="seats-count">{danhSachChuyen.length} chuyến</span>
        </div>

        {dangTaiChuyen ? (
          <p className="seats-empty">Đang tải danh sách chuyến xe...</p>
        ) : danhSachChuyen.length === 0 ? (
          <p className="seats-empty">Chưa có chuyến xe nào.</p>
        ) : (
          <div className="seats-trip-list">
            {danhSachChuyen.map((chuyen) => (
              <article
                className={`seats-trip-item ${maChuyenDangChon === chuyen.maChuyenXe ? "active" : ""}`}
                key={chuyen.maChuyenXe}
              >
                <div className="seats-trip-icon" aria-hidden="true">
                  ▣
                </div>
                <div className="seats-trip-info">
                  <strong>
                    {chuyen.diemDi} <span>→</span> {chuyen.diemDen}
                  </strong>
                  <small>
                    {chuyen.maChuyenXe} · {chuyen.thoiGianKhoiHanh}
                  </small>
                  <small>Biển số xe: {chuyen.bienSoXe}</small>
                </div>
                <button
                  className="seats-view-button"
                  onClick={() => xemGhe(chuyen.maChuyenXe)}
                  type="button"
                >
                  {maChuyenDangChon === chuyen.maChuyenXe
                    ? "Đang chọn"
                    : "Xem ghế"}
                </button>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="seats-card seats-map-card">
        <div className="seats-card-heading">
          <div>
            <h2>
              {chuyenDangChon
                ? `${chuyenDangChon.diemDi} → ${chuyenDangChon.diemDen}`
                : "Sơ đồ ghế chuyến xe"}
            </h2>
            <p>
              {chuyenDangChon
                ? `${chuyenDangChon.maChuyenXe} · ${soGheTrong}/${danhSachGhe.length} ghế còn trống`
                : "Chọn chuyến xe phía trên để xem trạng thái ghế."}
            </p>
          </div>
          <div className="seats-legend">
            <span>
              <i className="free" /> Còn trống
            </span>
            <span>
              <i className="taken" /> Đã đặt
            </span>
            <span>
              <i className="vip" /> Ghế VIP
            </span>
          </div>
        </div>

        {dangTaiGhe ? (
          <p className="seats-empty">Đang tải sơ đồ ghế...</p>
        ) : danhSachGhe.length > 0 ? (
          <div className="seats-bus">
            <div className="seats-driver">
              ĐẦU XE <span>◉</span>
            </div>
            <div className="seats-grid">
              {danhSachGhe.map((ghe) => {
                const daDat = ghe.trangThaiGhe === "DA_DAT";
                const laVip = ghe.loaiGhe === "GHE_VIP";
                return (
                  <div
                    className={`seat-tile ${daDat ? "booked" : "available"} ${laVip ? "vip" : ""}`}
                    key={ghe.maGhe}
                    title={`${ghe.maGhe} · ${laVip ? "Ghế VIP" : "Ghế thường"} · ${daDat ? "Đã đặt" : "Còn trống"}`}
                  >
                    <strong>{ghe.maGhe}</strong>
                    <small>{daDat ? "Đã đặt" : "Trống"}</small>
                  </div>
                );
              })}
            </div>
          </div>
        ) : (
          <p className="seats-empty">
            Sơ đồ ghế sẽ hiện ở đây sau khi chọn chuyến.
          </p>
        )}
      </section>
    </section>
  );
}

export default SeatsPage;
