import { useEffect, useState } from "react";
import "./TripsPage.css";

const API_URL = "http://localhost:8080/api/trips";

function formatTien(soTien) {
    return new Intl.NumberFormat("vi-VN").format(soTien || 0) + " đ";
}

function TripsPage() {
    const [danhSachChuyen, setDanhSachChuyen] = useState([]);
    const [diemDi, setDiemDi] = useState("");
    const [diemDen, setDiemDen] = useState("");
    const [ngayDi, setNgayDi] = useState("");
    const [dangTai, setDangTai] = useState(true);
    const [thongBao, setThongBao] = useState("");
    const [laLoi, setLaLoi] = useState(false);

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
                if (conHieuLuc) {
                    setThongBao(error.message);
                    setLaLoi(true);
                }
            })
            .finally(() => {
                if (conHieuLuc) setDangTai(false);
            });

        return () => {
            conHieuLuc = false;
        };
    }, []);

    async function timChuyen(event) {
        event.preventDefault();
        setDangTai(true);
        setThongBao("");
        setLaLoi(false);

        try {
            const query = new URLSearchParams();
            if (diemDi.trim()) query.set("diemDi", diemDi.trim());
            if (diemDen.trim()) query.set("diemDen", diemDen.trim());

            const response = await fetch(`${API_URL}?${query.toString()}`);
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.error || "Không tìm được chuyến xe.");
            }

            setDanhSachChuyen(
                ngayDi
                    ? result.filter((item) => item.thoiGianKhoiHanh?.startsWith(ngayDi))
                    : result,
            );
        } catch (error) {
            setThongBao(error.message);
            setLaLoi(true);
            setDanhSachChuyen([]);
        } finally {
            setDangTai(false);
        }
    }

    return (
        <section className="trips-page">
            <header className="trips-heading">
                <div>
                    <span className="trips-eyebrow">QUẢN LÝ VẬN HÀNH</span>
                    <h1>Chuyến xe</h1>
                    <p>Tìm chuyến theo điểm đi, điểm đến và ngày khởi hành.</p>
                </div>
                <div className="trips-heading-icon" aria-hidden="true">
                    ▣
                </div>
            </header>

            <section className="trips-card">
                <h2>Tìm chuyến xe</h2>
                <form className="trips-search-form" onSubmit={timChuyen}>
                    <label>
                        Điểm đi
                        <input
                            value={diemDi}
                            onChange={(event) => setDiemDi(event.target.value)}
                            placeholder="Ví dụ: Hà Nội"
                        />
                    </label>
                    <label>
                        Điểm đến
                        <input
                            value={diemDen}
                            onChange={(event) => setDiemDen(event.target.value)}
                            placeholder="Ví dụ: Hải Phòng"
                        />
                    </label>
                    <label>
                        Ngày đi
                        <input
                            type="date"
                            value={ngayDi}
                            onChange={(event) => setNgayDi(event.target.value)}
                        />
                    </label>
                    <button
                        className="trips-search-button"
                        type="submit"
                        disabled={dangTai}
                    >
                        {dangTai ? "Đang tìm..." : "⌕ Tìm chuyến"}
                    </button>
                </form>
            </section>

            {thongBao && (
                <p
                    className={`trips-message ${laLoi ? "error" : "success"}`}
                    role="status"
                >
                    {thongBao}
                </p>
            )}

            <section className="trips-card">
                <div className="trips-list-heading">
                    <div>
                        <h2>Danh sách chuyến xe</h2>
                        <p>{danhSachChuyen.length} chuyến xe</p>
                    </div>
                </div>

                {dangTai ? (
                    <p className="trips-empty">Đang tải danh sách chuyến xe...</p>
                ) : danhSachChuyen.length === 0 ? (
                    <p className="trips-empty">Không tìm thấy chuyến xe phù hợp.</p>
                ) : (
                    <div className="trips-table-wrap">
                        <table className="trips-table">
                            <thead>
                            <tr>
                                <th>Mã chuyến</th>
                                <th>Tuyến đường</th>
                                <th>Khởi hành</th>
                                <th>Biển số xe</th>
                                <th>Giá vé cơ bản</th>
                            </tr>
                            </thead>
                            <tbody>
                            {danhSachChuyen.map((chuyen) => (
                                <tr key={chuyen.maChuyenXe}>
                                    <td>
                                        <span className="trips-id">{chuyen.maChuyenXe}</span>
                                    </td>
                                    <td className="trips-route">
                                        {chuyen.diemDi} <span>→</span> {chuyen.diemDen}
                                    </td>
                                    <td>{chuyen.thoiGianKhoiHanh}</td>
                                    <td>{chuyen.bienSoXe}</td>
                                    <td className="trips-price">
                                        {formatTien(chuyen.giaVeCoBan)}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </section>
    );
}

export default TripsPage;