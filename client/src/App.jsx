import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "./styles/global.css";
import MainLayout from "./components/layout/MainLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import DebtList from "./pages/DebtList";
import DebtForm from "./pages/DebtForm";
import DebtDetail from "./pages/DebtDetail";
import LetterPage from "./pages/LetterPage";

function Forbidden() {
  return (
    <div className="container page" style={{ textAlign: "center", paddingTop: 80 }}>
      <h1 style={{ fontSize: 40, margin: 0 }}>403</h1>
      <p className="muted">Bạn không có quyền truy cập trang này.</p>
      <a href="/" className="btn btn-primary btn-sm">
        Về trang chủ
      </a>
    </div>
  );
}

function NotFound() {
  return (
    <div className="container page" style={{ textAlign: "center", paddingTop: 80 }}>
      <h1 style={{ fontSize: 40, margin: 0 }}>404</h1>
      <p className="muted">Trang bạn tìm không tồn tại.</p>
      <a href="/" className="btn btn-primary btn-sm">
        Về trang chủ
      </a>
    </div>
  );
}

export default function App() {
  return (
    <>
      <ToastContainer
        position="top-right"
        newestOnTop
        autoClose={2600}
        theme="light"
        className="!z-[9999]"
      />
      <BrowserRouter>
        <Routes>
          {                               }
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          {                                                       }
          <Route element={<MainLayout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/dashboard" element={<Navigate to="/" replace />} />
            <Route
              path="/debts"
              element={
                <ProtectedRoute>
                  <DebtList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/debts/new"
              element={
                <ProtectedRoute>
                  <DebtForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="/debts/:id"
              element={
                <ProtectedRoute>
                  <DebtDetail />
                </ProtectedRoute>
              }
            />
            <Route
              path="/debts/:id/edit"
              element={
                <ProtectedRoute>
                  <DebtForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="/letters"
              element={
                <ProtectedRoute>
                  <LetterPage />
                </ProtectedRoute>
              }
            />
            <Route path="/403" element={<Forbidden />} />
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}
