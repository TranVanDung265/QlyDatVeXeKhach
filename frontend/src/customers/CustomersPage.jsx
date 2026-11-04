import { useEffect, useState } from "react";
import "./CustomersPage.css";

const API_URL = "http://localhost:8080/api/customers";

const FORM_MOI = {
  hoTen: "",
  soDienThoai: "",
  email: "",
  loaiKhach: "KHACH_THUONG",
};

async function taiDanhSachKhachHang() {
  const response = await fetch(API_URL);
  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.error || "Không tải được khách hàng.");
  }

  return data;
}

function tenLoaiKhach(loaiKhach) {
  const tenLoai = {
    KHACH_THUONG: "Khách thường",
    THANH_VIEN: "Thành viên",
    VIP: "VIP",
  };
  return tenLoai[loaiKhach] || loaiKhach;
}

function CustomersPage() {
  const [danhSach, setDanhSach] = useState([]);
  const [form, setForm] = useState(FORM_MOI);
  const [maDangSua, setMaDangSua] = useState("");
  const [thongBao, setThongBao] = useState("");
  const [dangTai, setDangTai] = useState(true);
  const [dangLuu, setDangLuu] = useState(false);

  useEffect(() => {
    let cancelled = false;

    taiDanhSachKhachHang()
      .then((data) => {
        if (!cancelled) {
          setDanhSach(data);
          setThongBao("");
        }
      })
      .catch((error) => {
        if (!cancelled) setThongBao(error.message);
      })
      .finally(() => {
        if (!cancelled) setDangTai(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  function capNhatForm(event) {
    const { name, value } = event.target;
    setForm((hienTai) => ({ ...hienTai, [name]: value }));
  }

  function batDauSua(khachHang) {
    setMaDangSua(khachHang.maKhachHang);
    setForm({
      hoTen: khachHang.hoTen || "",
      soDienThoai: khachHang.soDienThoai || "",
      email: khachHang.email || "",
      loaiKhach: khachHang.loaiKhach || "KHACH_THUONG",
    });
    setThongBao("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function huySua() {
    setMaDangSua("");
    setForm(FORM_MOI);
    setThongBao("");
  }

  async function xuLyLuu(event) {
    event.preventDefault();
    setDangLuu(true);
    setThongBao("");

    try {
      const url = maDangSua
        ? `${API_URL}/${encodeURIComponent(maDangSua)}`
        : API_URL;

      const response = await fetch(url, {
        method: maDangSua ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: new URLSearchParams(form).toString(),
      });
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Không lưu được khách hàng.");
      }

      setDanhSach(await taiDanhSachKhachHang());
      setThongBao(
        maDangSua
          ? "Cập nhật khách hàng thành công."
          : `Thêm khách hàng thành công. Mã khách hàng: ${result.maKhachHang}`,
      );
      setMaDangSua("");
      setForm(FORM_MOI);
    } catch (error) {
      setThongBao(error.message);
    } finally {
      setDangLuu(false);
    }
  }

  async function xoaKhachHang(maKhachHang) {
    if (!window.confirm(`Bạn có chắc muốn xóa khách hàng ${maKhachHang}?`)) {
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/${encodeURIComponent(maKhachHang)}`,
        { method: "DELETE" },
      );
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Không xóa được khách hàng.");
      }

      setDanhSach(await taiDanhSachKhachHang());
      setThongBao("Xóa khách hàng thành công.");

      if (maDangSua === maKhachHang) {
        setMaDangSua("");
        setForm(FORM_MOI);
      }
    } catch (error) {
      setThongBao(error.message);
    }
  }

  return (
    <section className="customers-page">
      <header className="customers-heading">
        <div>
          <span className="customers-eyebrow">QUẢN LÝ HỆ THỐNG</span>
          <h1>Khách hàng</h1>
          <p>Thêm mới và cập nhật thông tin khách hàng của nhà xe.</p>
        </div>
        <div className="customers-heading-icon" aria-hidden="true">
          ♙
        </div>
      </header>

      <article className="customers-card customers-form-card">
        <div className="customers-card-heading">
          <div>
            <h2>{maDangSua ? "Cập nhật khách hàng" : "Thêm khách hàng"}</h2>
            <p>Điền thông tin liên hệ và chọn loại khách phù hợp.</p>
          </div>
          <span className="customers-card-icon" aria-hidden="true">
            ＋
          </span>
        </div>

        <form className="customers-form" onSubmit={xuLyLuu}>
          <label className="customers-field">
            <span>Họ và tên</span>
            <input
              name="hoTen"
              placeholder="Ví dụ: Nguyễn Văn An"
              value={form.hoTen}
              onChange={capNhatForm}
              autoComplete="name"
              required
            />
          </label>

          <label className="customers-field">
            <span>Số điện thoại</span>
            <input
              name="soDienThoai"
              type="tel"
              placeholder="Ví dụ: 0901234567"
              value={form.soDienThoai}
              onChange={capNhatForm}
              autoComplete="tel"
              required
            />
          </label>

          <label className="customers-field">
            <span>Email</span>
            <input
              name="email"
              type="email"
              placeholder="vidu@email.com"
              value={form.email}
              onChange={capNhatForm}
              autoComplete="email"
              required
            />
          </label>

          <label className="customers-field">
            <span>Loại khách</span>
            <select
              name="loaiKhach"
              value={form.loaiKhach}
              onChange={capNhatForm}
            >
              <option value="KHACH_THUONG">Khách thường</option>
              <option value="THANH_VIEN">Thành viên</option>
              <option value="VIP">VIP</option>
            </select>
          </label>

          <div className="customers-form-actions">
            <button
              className="customers-primary-button"
              type="submit"
              disabled={dangLuu}
            >
              {dangLuu
                ? "Đang lưu..."
                : maDangSua
                  ? "Lưu cập nhật"
                  : "＋ Thêm khách hàng"}
            </button>
            {maDangSua && (
              <button
                className="customers-secondary-button"
                onClick={huySua}
                type="button"
              >
                Hủy sửa
              </button>
            )}
          </div>
        </form>

        {thongBao && (
          <p className="customers-message" role="status">
            {thongBao}
          </p>
        )}
      </article>

      <section className="customers-card customers-list-card">
        <div className="customers-list-heading">
          <div>
            <h2>Danh sách khách hàng</h2>
            <p>Thông tin khách hàng đang được lưu trong hệ thống.</p>
          </div>
          <span className="customers-count">{danhSach.length} khách hàng</span>
        </div>

        <div className="customers-table-wrap">
          <table className="customers-table">
            <thead>
              <tr>
                <th>Mã khách hàng</th>
                <th>Họ tên</th>
                <th>Số điện thoại</th>
                <th>Email</th>
                <th>Loại khách</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {danhSach.map((khachHang) => (
                <tr key={khachHang.maKhachHang}>
                  <td>
                    <span className="customers-id">
                      {khachHang.maKhachHang}
                    </span>
                  </td>
                  <td className="customers-name">{khachHang.hoTen}</td>
                  <td>{khachHang.soDienThoai}</td>
                  <td>{khachHang.email}</td>
                  <td>
                    <span
                      className={`customers-type ${khachHang.loaiKhach?.toLowerCase()}`}
                    >
                      {tenLoaiKhach(khachHang.loaiKhach)}
                    </span>
                  </td>
                  <td>
                    <div className="customers-row-actions">
                      <button
                        className="customers-edit-button"
                        onClick={() => batDauSua(khachHang)}
                        type="button"
                      >
                        Sửa
                      </button>
                      <button
                        className="customers-delete-button"
                        onClick={() => xoaKhachHang(khachHang.maKhachHang)}
                        type="button"
                      >
                        Xóa
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {!dangTai && danhSach.length === 0 && (
                <tr>
                  <td className="customers-empty" colSpan="6">
                    {thongBao || "Chưa có khách hàng nào."}
                  </td>
                </tr>
              )}

              {dangTai && (
                <tr>
                  <td className="customers-empty" colSpan="6">
                    Đang tải danh sách khách hàng...
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

export default CustomersPage;
