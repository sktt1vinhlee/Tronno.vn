import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import api, { apiErrorMessage, saveAuth } from "../api/client";
import "./Auth.css";

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    monthlyIncome: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (form.password !== form.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp");
      return;
    }
    setLoading(true);
    try {
      const { data } = await api.post("/auth/register", {
        ...form,
        monthlyIncome: form.monthlyIncome ? Number(form.monthlyIncome) : null,
      });
      saveAuth(data);
      toast.success("Tạo tài khoản thành công!");
      navigate("/", { replace: true });
    } catch (err) {
      setError(apiErrorMessage(err, "Đăng ký thất bại"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="brand-mark">TN</span>
          <div>
            <strong>TRONNO</strong>
            <span>Quản lý khoản tiền bạn cần trả</span>
          </div>
        </div>
        <h1 className="auth-title">Tạo tài khoản</h1>
        <p className="auth-subtitle">Tạo tài khoản để theo dõi tiền bạn cần trả</p>
        {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

        <form onSubmit={submit}>
          <div className="field">
            <label htmlFor="fullName">Họ và tên</label>
            <input id="fullName" className="input" required value={form.fullName} onChange={set("fullName")} />
          </div>
          <div className="grid grid-2">
            <div className="field">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                className="input"
                type="email"
                autoComplete="email"
                required
                value={form.email}
                onChange={set("email")}
              />
            </div>
            <div className="field">
              <label htmlFor="phone">Số điện thoại</label>
              <input id="phone" className="input" value={form.phone} onChange={set("phone")} />
            </div>
          </div>
          <div className="field">
            <label htmlFor="monthlyIncome">Thu nhập hàng tháng (đồng)</label>
            <input
              id="monthlyIncome"
              className="input"
              type="number"
              min="0"
              step="100000"
              placeholder="Để xem khoản trả hàng tháng chiếm bao nhiêu thu nhập"
              value={form.monthlyIncome}
              onChange={set("monthlyIncome")}
            />
          </div>
          <div className="grid grid-2">
            <div className="field">
              <label htmlFor="password">Mật khẩu</label>
              <input
                id="password"
                className="input"
                type="password"
                autoComplete="new-password"
                required
                value={form.password}
                onChange={set("password")}
              />
              <span className="field-hint">Tối thiểu 8 ký tự, có chữ hoa, số và ký tự đặc biệt</span>
            </div>
            <div className="field">
              <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
              <input
                id="confirmPassword"
                className="input"
                type="password"
                autoComplete="new-password"
                required
                value={form.confirmPassword}
                onChange={set("confirmPassword")}
              />
            </div>
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? "Đang tạo tài khoản..." : "Tạo tài khoản"}
          </button>
        </form>
        <p className="auth-switch">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
