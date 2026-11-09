import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
});

const normalizeToken = (value) => {
  if (typeof value !== "string") return null;
  let token = value.trim();
  if (token.startsWith('"') && token.endsWith('"')) token = token.slice(1, -1).trim();
  while (/^Bearer\s+/i.test(token)) token = token.replace(/^Bearer\s+/i, "").trim();
  return token || null;
};

export const getStoredAuthToken = () => normalizeToken(localStorage.getItem("token"));

export const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    return null;
  }
};

export const saveAuth = (data) => {
  if (data?.token) localStorage.setItem("token", data.token);
  localStorage.setItem(
    "user",
    JSON.stringify({
      id: data.id,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
      phone: data.phone,
      address: data.address,
      avatarUrl: data.avatarUrl,
      monthlyIncome: data.monthlyIncome,
    }),
  );
};

export const clearAuth = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
};

api.interceptors.request.use((config) => {
  const token = getStoredAuthToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || "";
    const isLogin = /\/auth\/login(?:\?|$)/i.test(url);
    const isMe = /\/auth\/me(?:\?|$)/i.test(url);
    const isPublic = /\/public\//i.test(url);
    if (err.response?.status === 401 && !isLogin && !isMe && !isPublic) {
      clearAuth();
      if (window.location.pathname !== "/login") {
        const returnTo = `${window.location.pathname}${window.location.search}`;
        window.location.href = `/login?returnTo=${encodeURIComponent(returnTo)}`;
      }
    }
    return Promise.reject(err);
  },
);

export function apiErrorMessage(err, fallback = "Đã có lỗi xảy ra, vui lòng thử lại.") {
  const data = err?.response?.data;
  if (!data) return err?.message || fallback;
  if (typeof data === "string") return data;
  if (data.errors && typeof data.errors === "object") {
    const first = Object.values(data.errors).find((v) => typeof v === "string");
    if (first) return first;
  }
  return data.message || data.error || fallback;
}

export default api;
