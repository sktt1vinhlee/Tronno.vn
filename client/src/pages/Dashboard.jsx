import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import { reportApi, debtApi } from "../api/tronno";
import PaymentModal from "../components/common/PaymentModal";
import StatCard from "../components/common/StatCard";
import { PayoffCurveChart, CashflowChart } from "../components/common/Charts";
import {

  DEBT_TYPE_LABEL,
  SCHEDULE_STATUS_BADGE,
  SCHEDULE_STATUS_LABEL,
  countdownLabel,
  formatDate,
  formatMoney,
} from "../utils/tronno";

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [payTarget, setPayTarget] = useState(null);

  const load = async () => {
    try {
      const { data: result } = await reportApi.dashboard();
      setData(result);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không tải được dữ liệu tổng quan");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (loading) return <div className="container page loading">Đang tải tổng quan...</div>;
  if (!data) return <div className="container page empty">Chưa có dữ liệu</div>;

  const alert = data.cashflowAlert;
  const alertClass =
    alert?.level === "DANGER" ? "alert-danger" : alert?.level === "WARNING" ? "alert-warning" : "alert-info";

  const paidPercent =
    data.totalDebt > 0 ? Math.min(100, Math.round((Number(data.totalPaid) / Number(data.totalDebt)) * 100)) : 0;

  return (
    <div className="container page">
      <h1 className="page-title">Các khoản tiền bạn cần trả</h1>
      <p className="page-subtitle">
        Dự kiến trả hết vào:{" "}
        <strong>{data.debtFreeDate ? formatDate(data.debtFreeDate) : "Đã tất toán hết"}</strong>
        {data.daysToDebtFree > 0 && ` (còn ${data.daysToDebtFree} ngày)`}
      </p>
      {alert?.message && (
        <div className={`alert ${alertClass}`} style={{ marginBottom: 18 }}>
          {alert.message}
        </div>
      )}

      <div className="grid grid-4" style={{ marginBottom: 18 }}>
        <StatCard
          label="Tổng nợ ban đầu"
          value={formatMoney(data.totalDebt)}
          hint={`${data.activeDebtCount} khoản chưa trả hết`}
          variant="accent"
        />
        <StatCard
          label="Còn phải trả"
          value={formatMoney(data.totalRemaining)}
          hint={`Đã trả ${formatMoney(data.totalPaid)}`}
        />
        <StatCard
          label="Mỗi tháng cần trả"
          value={formatMoney(data.monthlyPayment)}
          hint={
            data.monthlyIncome > 0
              ? `Chiếm ${Number(data.dti).toFixed(1)}% thu nhập`
              : "Thêm thu nhập trong hồ sơ để xem tỷ lệ"
          }
          variant={Number(data.dti) > 40 ? "warn" : ""}
        />
        <StatCard
          label="Lãi đã trả"
          value={formatMoney(data.totalInterestPaid)}
          hint={`Phí phạt đã trả ${formatMoney(data.totalFeePaid)}`}
        />
      </div>
      <div className="grid grid-2" style={{ marginBottom: 18 }}>
        <div className="card">
          <div className="card-header">
            <h2>Tiến độ trả nợ</h2>
            <span className="muted" style={{ fontSize: 12.5 }}>
              Đã trả {paidPercent}%
            </span>
          </div>
          <div className="card-body">
            <div className="progress" style={{ marginBottom: 14 }}>
              <div className="progress-bar green" style={{ width: `${paidPercent}%` }} />
            </div>
            <PayoffCurveChart data={data.payoffCurve} />
          </div>
        </div>
        <div className="card">
          <div className="card-header">
            <h2>Dự kiến các khoản phải trả</h2>
          </div>
          <div className="card-body">
            <CashflowChart data={data.cashflow} />
          </div>
        </div>
      </div>
      <div className="grid grid-2">
        <div className="card">
          <div className="card-header">
            <h2>Sắp đến hạn trả</h2>
            <Link to="/debts" className="btn btn-outline btn-sm">
              Xem tất cả
            </Link>
          </div>
          <div className="table-wrap">
            {data.upcomingSchedules?.length ? (
              <table className="table">
                <thead>
                  <tr>
                    <th>Kỳ</th>
                    <th>Đến hạn</th>
                    <th className="num">Còn phải trả</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {data.upcomingSchedules.map((s) => (
                    <tr key={s.id}>
                      <td>#{s.installmentNo}</td>
                      <td>
                        {formatDate(s.dueDate)}
                        <div className="muted" style={{ fontSize: 12 }}>
                          {countdownLabel(s.dueDate)}
                        </div>
                      </td>
                      <td className="num">{formatMoney(s.remaining)}</td>
                      <td>
                        <span className={`badge ${SCHEDULE_STATUS_BADGE[s.status]}`}>
                          {SCHEDULE_STATUS_LABEL[s.status]}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty">Không có kỳ trả nào tồn đọng</div>
            )}
          </div>
        </div>
        <div className="card">
          <div className="card-header">
            <h2>Khoản cần trả</h2>
            <Link to="/debts/new" className="btn btn-primary btn-sm">
              + Thêm khoản phải trả
            </Link>
          </div>
          <div className="table-wrap">
            {data.debts?.length ? (
              <table className="table">
                <thead>
                  <tr>
                    <th>Khoản phải trả</th>
                    <th>Loại</th>
                    <th className="num">Còn phải trả</th>
                    <th className="num">Lãi</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {data.debts.map((d) => (
                    <tr key={d.id}>
                      <td>
                        <Link to={`/debts/${d.id}`} style={{ fontWeight: 600 }}>
                          {d.name}
                        </Link>
                      </td>
                      <td className="muted">{DEBT_TYPE_LABEL[d.debtType]}</td>
                      <td className="num">{formatMoney(d.remainingPrincipal)}</td>
                      <td className="num">{Number(d.annualRate).toFixed(2)}%</td>
                      <td>
                        <button
                          type="button"
                          className="btn btn-primary btn-sm"
                          onClick={() => setPayTarget(d)}
                        >
                          Trả nợ
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty">
                Bạn chưa có khoản nào cần trả.{" "}
                <Link to="/debts/new" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
                  Thêm khoản đầu tiên
                </Link>
              </div>
            )}
          </div>
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
