import { useEffect, useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import api, { clearAuth, getStoredAuthToken } from "../api/client";

export default function ProtectedRoute({ roles = [], children }) {
  const location = useLocation();
  const navigate = useNavigate();
  const token = getStoredAuthToken();
  const [state, setState] = useState({ status: token ? "loading" : "guest", role: null });

  useEffect(() => {
    if (!token) {
      setState({ status: "guest", role: null });
      return undefined;
    }
    let cancelled = false;
    api
      .get("/auth/me")
      .then(({ data }) => {
        if (cancelled) return;
        setState({ status: "authed", role: data.role });
      })
      .catch(() => {
        if (cancelled) return;
        clearAuth();
        setState({ status: "guest", role: null });
      });
    return () => {
      cancelled = true;
    };
  }, [token, location.pathname]);

  useEffect(() => {
    if (state.status === "authed" && roles.length > 0 && !roles.includes(state.role)) {
      navigate("/403", { replace: true });
    }
  }, [state, roles, navigate]);

  if (!token || state.status === "guest") {
    const returnTo = `${location.pathname}${location.search}`;
    return <Navigate to={`/login?returnTo=${encodeURIComponent(returnTo)}`} replace />;
  }

  if (state.status === "loading") {
    return <div className="loading">Đang kiểm tra quyền truy cập...</div>;
  }

  if (roles.length > 0 && !roles.includes(state.role)) {
    return null;
  }

  return children;
}
