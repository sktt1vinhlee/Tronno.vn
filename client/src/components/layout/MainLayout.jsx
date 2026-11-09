import { useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { clearAuth, getStoredAuthToken, getStoredUser } from "../../api/client";
import { ROLE_LABEL } from "../../utils/tronno";
import "./MainLayout.css";

const NAV = [
  { to: "/", label: "Tổng quan", end: true },
  { to: "/debts", label: "Khoản phải trả" },
  { to: "/letters", label: "Soạn văn" },
];

function Brand() {
  return (
    <Link to="/" className="brand">
      <span className="brand-mark">TN</span>
      <span>
        TRONNO<span className="brand-accent">.vn</span>
      </span>
    </Link>
  );
}

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(getStoredUser());
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  const isLoggedIn = Boolean(getStoredAuthToken());

  useEffect(() => {
    setUser(getStoredUser());
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    const onClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const logout = () => {
    clearAuth();
    setMenuOpen(false);
    navigate("/login", { replace: true });
  };

  return (
    <>
      <header className="site-header">
        <div className="container site-header-inner">
          <Brand />
          <nav className="main-nav">
            {NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="header-actions">
            {isLoggedIn && user ? (
              <div className="user-menu" ref={menuRef}>
                <button
                  type="button"
                  className="user-menu-trigger"
                  onClick={() => setMenuOpen((v) => !v)}
                  aria-expanded={menuOpen}
                >
                  <span className="user-avatar">
                    {(user.fullName || "U").charAt(0).toUpperCase()}
                  </span>
                  <span>{user.fullName?.split(" ").pop() || "Tài khoản"}</span>
                </button>
                {menuOpen && (
                  <div className="user-menu-dropdown">
                    <div className="user-menu-head">
                      <strong>{user.fullName}</strong>
                      <span>{ROLE_LABEL[user.role] || user.role}</span>
                    </div>
                    <button type="button" onClick={logout}>
                      Đăng xuất
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost btn-sm">
                  Đăng nhập
                </Link>
                <Link to="/register" className="btn btn-primary btn-sm">
                  Đăng ký
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main style={{ minHeight: "calc(100vh - var(--header-height) - 140px)" }}>
        <Outlet />
      </main>
      <footer className="site-footer">
        <div className="container site-footer-inner">
          <div>
            <strong style={{ color: "#111827" }}>TRONNO</strong>
            <div>Quản lý các khoản tiền bạn cần trả cho người khác.</div>
          </div>
          <div>
            Mọi tính toán chỉ mang tính tham khảo. Hãy kiểm tra lại hợp đồng với người cho vay.
            <div>© {new Date().getFullYear()} TRONNO</div>
          </div>
        </div>
      </footer>
    </>
  );
}
