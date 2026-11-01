import { useEffect, useState } from "react";
import "./App.css";
import "./SidebarBranding.css";
import BookingPage from "./bookings/BookingPage";
import CustomersPage from "./customers/CustomersPage";
import PaymentPage from "./payments/PaymentPage";
import TripsPage from "./trips/TripsPage";
import SeatsPage from "./seats/SeatsPage";

const menuItems = [
  ["Tổng quan", "overview"],
  ["Khách hàng", "customer"],
  ["Chuyến xe", "bus"],
  ["Ghế", "seat"],
  ["Đặt vé", "ticket"],
  ["Thanh toán", "card"],
  ["Vé bán trong ngày", "history"],
];

function NavIcon({ name }) {
  const props = {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    "aria-hidden": true,
  };

  const icons = {
    overview: (
      <>
        <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
        <rect x="13.5" y="3.5" width="7" height="5" rx="1.5" />
        <rect x="13.5" y="11.5" width="7" height="9" rx="1.5" />
        <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
      </>
    ),
    customer: (
      <>
        <circle cx="12" cy="8" r="3.3" />
        <path d="M5 20c.5-3.5 3.2-5.5 7-5.5s6.5 2 7 5.5" />
      </>
    ),
    bus: (
      <>
        <path d="M5 17V7.5C5 5.6 7.2 4 12 4s7 1.6 7 3.5V17" />
        <path d="M5 10h14M5 15h14M7 17v2m10-2v2" />
        <circle cx="8" cy="18" r="1.5" />
        <circle cx="16" cy="18" r="1.5" />
      </>
    ),
    seat: (
      <>
        <path d="M7 4v7c0 2 1.4 3.5 3.5 3.5H17V20" />
        <path d="M7 11h10v4H9.5M9 20h10" />
        <path d="M7 4h3" />
      </>
    ),
    ticket: (
      <>
        <path d="M4 7.5A2.5 2.5 0 0 0 6.5 5h11A2.5 2.5 0 0 0 20 7.5v2a2.5 2.5 0 0 0 0 5v2a2.5 2.5 0 0 0-2.5 2.5h-11A2.5 2.5 0 0 0 4 16.5v-2a2.5 2.5 0 0 0 0-5z" />
        <path d="M12 7v2m0 2v2m0 2v2" />
      </>
    ),
    card: (
      <>
        <rect x="3" y="5" width="18" height="14" rx="2.5" />
        <path d="M3 10h18M7 15h4" />
      </>
    ),
    history: (
      <>
        <path d="M3.5 11a8.5 8.5 0 1 1 2.2 5.7" />
        <path d="M3.5 5.5V11h5.5M12 7v5l3.5 2" />
      </>
    ),
  };

  return <svg {...props}>{icons[name]}</svg>;
}

function BusBrandLogo() {
  return (
    <span className="brand-icon" aria-hidden="true">
      <svg viewBox="0 0 32 32" fill="none">
        <path d="M7 23V10c0-2.2 2.3-3.8 9-3.8S25 7.8 25 10v13" />
        <path d="M7 13h18M7 20h18M10 23v2m12-2v2" />
        <circle cx="11" cy="23" r="2" />
        <circle cx="21" cy="23" r="2" />
      </svg>
    </span>
  );
}

const seatRows = [
  [
    ["1A", "1B", "1C", "1D"],
    ["2A", "2B", "2C", "2D"],
  ],
  [
    ["3A", "3B", "3C", "3D"],
    ["4A", "4B", "4C", "4D"],
  ],
];

const bookedSeats = new Set(["1B", "2C", "3A", "3D", "4D"]);

const tenKhachHang = {
  KH001: "Nguyễn Văn An",
  KH002: "Trần Thị Bình",
  KH003: "Lê Minh Tuấn",
};

const tenChuyenXe = {
  CX001: "Hà Nội → Hải Phòng",
  CX002: "Hải Phòng → Hà Nội",
};

function TicketSalesPage({ tickets, ticketError }) {
  return (
    <section className="panel tickets-panel">
      <div className="tickets-heading">
        <div>
          <h1>Vé bán trong ngày</h1>
          <p>Danh sách vé được đặt trong hôm nay.</p>
        </div>

        <button
          className="print-button"
          onClick={() => window.print()}
          type="button"
        >
          <span>▣</span> In danh sách
        </button>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Mã vé</th>
              <th>Khách hàng</th>
              <th>Chuyến xe</th>
              <th>Ghế</th>
              <th>Thanh toán</th>
            </tr>
          </thead>

          <tbody>
            {tickets.map((ticket) => {
              const daThanhToan = ticket.trangThaiThanhToan === "DA_THANH_TOAN";

              return (
                <tr key={ticket.maVe}>
                  <td className="ticket-id">{ticket.maVe}</td>
                  <td>
                    {tenKhachHang[ticket.maKhachHang] || ticket.maKhachHang}
                  </td>
                  <td>{tenChuyenXe[ticket.maChuyenXe] || ticket.maChuyenXe}</td>
                  <td>{ticket.maGhe}</td>
                  <td>
                    <span
                      className={`payment-status ${daThanhToan ? "paid" : "unpaid"}`}
                    >
                      <i />
                      {daThanhToan ? "Đã thanh toán" : "Chưa thanh toán"}
                    </span>
                  </td>
                </tr>
              );
            })}

            {tickets.length === 0 && (
              <tr>
                <td colSpan="5">
                  {ticketError || "Hôm nay chưa có vé nào được đặt."}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function App() {
  const [activeMenu, setActiveMenu] = useState("Tổng quan");
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [notice, setNotice] = useState("");
  const [tickets, setTickets] = useState([]);
  const [ticketError, setTicketError] = useState("");
  const [diemDi, setDiemDi] = useState("Hà Nội");
  const [diemDen, setDiemDen] = useState("Hải Phòng");
  const [ngayDi, setNgayDi] = useState("2026-11-01");
  const [ketQuaTimChuyen, setKetQuaTimChuyen] = useState([]);
  const [daTimChuyen, setDaTimChuyen] = useState(false);
  const [dangTimChuyen, setDangTimChuyen] = useState(false);
  const [loiTimChuyen, setLoiTimChuyen] = useState("");

  useEffect(() => {
    let cancelled = false;

    fetch("http://localhost:8080/api/bookings/today")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Không tải được danh sách vé.");
        }
        return response.json();
      })
      .then((data) => {
        if (!cancelled) {
          setTickets(data);
          setTicketError("");
        }
      })
      .catch(() => {
        if (!cancelled) {
          setTicketError("Không kết nối được backend để tải danh sách vé.");
        }
      });

    return () => {
      cancelled = true;
    };
  }, [activeMenu]);

  function toggleSeat(seat) {
    setSelectedSeats((current) =>
      current.includes(seat)
        ? current.filter((item) => item !== seat)
        : [...current, seat],
    );
  }

  async function handleSearch(event) {
    event.preventDefault();
    setDangTimChuyen(true);
    setDaTimChuyen(true);
    setLoiTimChuyen("");

    try {
      const query = new URLSearchParams({ diemDi, diemDen });
      const response = await fetch(
        `http://localhost:8080/api/trips?${query.toString()}`,
      );
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Không tìm được chuyến xe.");
      }

      const ketQuaTheoNgay = result.filter((chuyenXe) =>
        chuyenXe.thoiGianKhoiHanh.startsWith(ngayDi),
      );
      setKetQuaTimChuyen(ketQuaTheoNgay);
    } catch (error) {
      setLoiTimChuyen(
        error.message === "Failed to fetch"
          ? "Không kết nối được backend Java. Hãy kiểm tra Terminal chạy API."
          : error.message,
      );
      setKetQuaTimChuyen([]);
    } finally {
      setDangTimChuyen(false);
    }
  }

  function selectMenu(label) {
    setActiveMenu(label);

    const daCoGiaoDien = [
      "Tổng quan",
      "Khách hàng",
      "Chuyến xe",
      "Ghế",
      "Đặt vé",
      "Thanh toán",
      "Vé bán trong ngày",
    ].includes(label);

    setNotice(daCoGiaoDien ? "" : `Mục “${label}” sẽ được tích hợp sau.`);
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <BusBrandLogo />
          <div className="brand-copy">
            <span className="brand-name">Vé Việt</span>
            <span className="brand-caption">QUẢN LÝ NHÀ XE</span>
          </div>
        </div>

        <nav className="main-nav" aria-label="Menu chính">
          {menuItems.map(([label, icon]) => (
            <button
              className={`nav-item ${activeMenu === label ? "active" : ""}`}
              key={label}
              onClick={() => selectMenu(label)}
              type="button"
            >
              <span className="nav-icon">
                <NavIcon name={icon} />
              </span>
              <span>{label}</span>
            </button>
          ))}
        </nav>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <label className="global-search">
            <span>⌕</span>
            <input
              aria-label="Tìm kiếm"
              placeholder="Tìm khách hàng, chuyến xe, mã vé..."
            />
          </label>

          <div className="user-menu">
            <button className="bell" type="button" aria-label="Thông báo">
              ♟<i />
            </button>
            <span className="avatar">NV</span>
            <span>Nhân viên</span>
            <span className="chevron">⌄</span>
          </div>
        </header>

        <div className="page-content">
          {activeMenu === "Đặt vé" ? (
            <BookingPage />
          ) : activeMenu === "Khách hàng" ? (
            <CustomersPage />
          ) : activeMenu === "Thanh toán" ? (
            <PaymentPage />
          ) : activeMenu === "Chuyến xe" ? (
            <TripsPage />
          ) : activeMenu === "Ghế" ? (
            <SeatsPage />
          ) : activeMenu === "Vé bán trong ngày" ? (
            <TicketSalesPage tickets={tickets} ticketError={ticketError} />
          ) : (
            <>
              <h1>Tổng quan</h1>

              {notice && (
                <div className="notice" role="status">
                  {notice}
                  <button
                    onClick={() => setNotice("")}
                    type="button"
                    aria-label="Đóng"
                  >
                    ×
                  </button>
                </div>
              )}

              <section className="stats-grid" aria-label="Thống kê hôm nay">
                <article className="stat-card">
                  <div className="stat-icon green">◆</div>
                  <div>
                    <p>Vé bán hôm nay</p>
                    <strong>35</strong>
                    <small className="positive">↗ &nbsp;+16%</small>
                  </div>
                </article>

                <article className="stat-card">
                  <div className="stat-icon blue">▤</div>
                  <div>
                    <p>Doanh thu hôm nay</p>
                    <strong>12.250.000 đ</strong>
                    <small className="positive">↗ &nbsp;+22%</small>
                  </div>
                </article>

                <article className="stat-card">
                  <div className="stat-icon cyan">▣</div>
                  <div>
                    <p>Chuyến xe</p>
                    <strong>6</strong>
                    <small>↗ &nbsp;+0%</small>
                  </div>
                </article>

                <article className="stat-card">
                  <div className="stat-icon orange">♧</div>
                  <div>
                    <p>Ghế còn trống</p>
                    <strong>14</strong>
                    <small className="negative">↓ &nbsp;-12%</small>
                  </div>
                </article>
              </section>

              <section className="workspace-grid">
                <article className="panel search-panel">
                  <h2>Tìm chuyến xe</h2>

                  <form className="search-form" onSubmit={handleSearch}>
                    <label>
                      Điểm đi
                      <select
                        value={diemDi}
                        onChange={(event) => setDiemDi(event.target.value)}
                      >
                        <option>Hà Nội</option>
                        <option>Hải Phòng</option>
                        <option>Ninh Bình</option>
                        <option>Đà Nẵng</option>
                        <option>Huế</option>
                        <option>TP. Hồ Chí Minh</option>
                        <option>Cần Thơ</option>
                      </select>
                    </label>

                    <label>
                      Điểm đến
                      <select
                        value={diemDen}
                        onChange={(event) => setDiemDen(event.target.value)}
                      >
                        <option>Hà Nội</option>
                        <option>Hải Phòng</option>
                        <option>Ninh Bình</option>
                        <option>Đà Nẵng</option>
                        <option>Huế</option>
                        <option>TP. Hồ Chí Minh</option>
                        <option>Cần Thơ</option>
                      </select>
                    </label>

                    <label>
                      Ngày đi
                      <input
                        type="date"
                        value={ngayDi}
                        onChange={(event) => setNgayDi(event.target.value)}
                      />
                    </label>

                    <button className="primary-button" type="submit">
                      <span>⌕</span> Tìm chuyến
                    </button>
                  </form>

                  {daTimChuyen && (
                    <div className="search-results" aria-live="polite">
                      <h3>Kết quả tìm kiếm</h3>

                      {dangTimChuyen && <p>Đang tìm chuyến xe...</p>}

                      {!dangTimChuyen && loiTimChuyen && (
                        <p role="alert">{loiTimChuyen}</p>
                      )}

                      {!dangTimChuyen &&
                        !loiTimChuyen &&
                        ketQuaTimChuyen.length === 0 && (
                          <p>Không tìm thấy chuyến xe phù hợp.</p>
                        )}

                      {!dangTimChuyen &&
                        ketQuaTimChuyen.map((chuyenXe) => (
                          <div
                            className="booking-trip-info"
                            key={chuyenXe.maChuyenXe}
                          >
                            <strong>
                              {chuyenXe.diemDi} → {chuyenXe.diemDen}
                            </strong>
                            <span>Mã chuyến: {chuyenXe.maChuyenXe}</span>
                            <span>Khởi hành: {chuyenXe.thoiGianKhoiHanh}</span>
                            <span>Biển số xe: {chuyenXe.bienSoXe}</span>
                            <span>
                              Giá vé cơ bản:{" "}
                              {new Intl.NumberFormat("vi-VN").format(
                                chuyenXe.giaVeCoBan,
                              )}{" "}
                              đ
                            </span>
                          </div>
                        ))}
                    </div>
                  )}
                </article>

                <article className="panel seat-panel">
                  <div className="seat-panel-heading">
                    <h2>Sơ đồ ghế xe</h2>

                    <div className="seat-legend">
                      <span>
                        <i className="legend-seat available" /> Ghế trống
                      </span>
                      <span>
                        <i className="legend-seat booked" /> Ghế đã đặt
                      </span>
                    </div>
                  </div>

                  <div className="bus-shell">
                    <div className="driver-area">
                      <span className="steering">◉</span>
                    </div>

                    <div className="seat-map">
                      {seatRows.map((row, rowIndex) => (
                        <div className="seat-row" key={rowIndex}>
                          {row.map((group, groupIndex) => (
                            <div className="seat-group" key={groupIndex}>
                              {group.map((seat) => {
                                const isBooked = bookedSeats.has(seat);
                                const isSelected = selectedSeats.includes(seat);

                                return (
                                  <button
                                    aria-label={`Ghế ${seat}${isBooked ? ", đã đặt" : ""}`}
                                    className={`seat ${isBooked ? "booked" : ""} ${isSelected ? "selected" : ""}`}
                                    disabled={isBooked}
                                    key={seat}
                                    onClick={() => toggleSeat(seat)}
                                    type="button"
                                  >
                                    {seat}
                                  </button>
                                );
                              })}
                            </div>
                          ))}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="seat-selection">
                    {selectedSeats.length
                      ? `Đang chọn: ${selectedSeats.join(", ")}`
                      : "Chọn ghế trống để xem"}
                    <span>{selectedSeats.length} ghế</span>
                  </div>
                </article>
              </section>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

export default App;
