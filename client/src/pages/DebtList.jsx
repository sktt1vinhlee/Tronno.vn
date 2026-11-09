import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import { debtApi } from "../api/tronno";
import PaymentModal from "../components/common/PaymentModal";
import {

  DEBT_STATUS_LABEL,
  DEBT_TYPE_LABEL,
  countdownLabel,
  formatDate,
  formatMoney,
} from "../utils/tronno";

const STATUS_BADGE = { ACTIVE: "badge-blue", PAID: "badge-green", CLOSED: "badge-gray" };

export default function DebtList() {
  const [debts, setDebts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [keyword, setKeyword] = useState("");
  const [payTarget, setPayTarget] = useState(null);

  const load = async () => {
    try {
      const { data } = await debtApi.list();
      setDebts(data);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không tải được danh sách khoản nợ");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const visible = useMemo(() => {
    return debts.filter((d) => {
      if (filter !== "ALL" && d.status !== filter) return false;
      if (!keyword.trim()) return true;
      const kw = keyword.trim().toLowerCase();
      return (
        d.name?.toLowerCase().includes(kw) ||
        d.creditorName?.toLowerCase().includes(kw) ||
        DEBT_TYPE_LABEL[d.debtType]?.toLowerCase().includes(kw)
      );
    });
  }, [debts, filter, keyword]);

  const totals = useMemo(() => {
    const active = debts.filter((d) => d.status === "ACTIVE");
    return {
      count: active.length,
      remaining: active.reduce((sum, d) => sum + Number(d.remainingPrincipal || 0), 0),
      monthly: active.reduce((sum, d) => sum + Number(d.installmentAmount || 0), 0),
    };
  }, [debts]);

  if (loading) return <div className="container page loading">Đang tải khoản nợ...</div>;

  return (
    <div className="container page">
      <div className="row-between" style={{ marginBottom: 18 }}>
        <div>
          <h1 className="page-title" style={{ marginBottom: 2 }}>
            Khoản phải trả
          </h1>
          <p className="page-subtitle" style={{ margin: 0 }}>
            {totals.count} khoản chưa trả hết · Còn phải trả{" "}
            <strong>{formatMoney(totals.remaining)}</strong> · Mỗi tháng{" "}
            <strong>{formatMoney(totals.monthly)}</strong>
          </p>
        </div>
        <Link to="/debts/new" className="btn btn-primary">
          + Thêm khoản phải trả
        </Link>
      </div>
      <div className="card">
        <div className="card-header">
          <div className="tabs" style={{ margin: 0, border: 0 }}>
            {[
              { key: "ALL", label: "Tất cả" },
              { key: "ACTIVE", label: "Đang có" },
              { key: "PAID", label: "Đã tất toán" },
              { key: "CLOSED", label: "Đã đóng" },
            ].map((t) => (
              <button
                key={t.key}
                type="button"
                className={`tab ${filter === t.key ? "active" : ""}`}
                onClick={() => setFilter(t.key)}
              >
                {t.label}
              </button>
            ))}
          </div>
          <input
            className="input"
            style={{ maxWidth: 260 }}
            placeholder="Tìm khoản hoặc người bạn nợ..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
        <div className="table-wrap">
          {visible.length ? (
            <table className="table">
              <thead>
                <tr>
                  <th>Khoản phải trả</th>
                  <th>Loại</th>
                  <th className="num">Số tiền ban đầu</th>
                  <th className="num">Lãi suất</th>
                  <th className="num">Dư nợ</th>
                  <th>Đến hạn</th>
                  <th>Trạng thái</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {visible.map((d) => (
                  <tr key={d.id}>
                    <td>
                      <Link to={`/debts/${d.id}`} style={{ fontWeight: 600 }}>
                        {d.name}
                      </Link>
                      {d.creditorName && (
                        <div className="muted" style={{ fontSize: 12 }}>
                          {d.creditorName}
                        </div>
                      )}
                    </td>
                    <td className="muted">{DEBT_TYPE_LABEL[d.debtType]}</td>
                    <td className="num">{formatMoney(d.principal)}</td>
                    <td className="num">{Number(d.annualRate).toFixed(2)}%</td>
                    <td className="num">{formatMoney(d.remainingPrincipal)}</td>
                    <td>
                      {d.nextDueDate ? (
                        <>
                          {formatDate(d.nextDueDate)}
                          <div className="muted" style={{ fontSize: 12 }}>
                            {countdownLabel(d.nextDueDate)}
                          </div>
                        </>
                      ) : (
                        <span className="muted">-</span>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${STATUS_BADGE[d.status]}`}>
                        {DEBT_STATUS_LABEL[d.status]}
                      </span>
                    </td>
                    <td>
                      {d.status === "ACTIVE" && (
                        <button
                          type="button"
                          className="btn btn-primary btn-sm"
                          onClick={() => setPayTarget(d)}
                        >
                          Trả nợ
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="empty">
              {debts.length === 0 ? (
                <>
                  Bạn chưa có khoản nào cần trả.{" "}
                  <Link to="/debts/new" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
                    Thêm khoản đầu tiên
                  </Link>
                </>
              ) : (
                "Không có khoản nợ nào khớp bộ lọc"
              )}
            </div>
          )}
        </div>
      </div>
      <PaymentModal
        open={Boolean(payTarget)}
        debtId={payTarget?.id}
        debtName={payTarget?.name}
        onClose={() => setPayTarget(null)}
        onPaid={load}
      />
    </div>
  );
}
