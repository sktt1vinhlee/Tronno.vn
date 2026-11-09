import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { debtApi, reminderApi } from "../api/tronno";
import Modal from "../components/common/Modal";
import PaymentModal from "../components/common/PaymentModal";
import StatCard from "../components/common/StatCard";
import {

  ALLOCATION_COMPONENT_LABEL,
  DEBT_STATUS_LABEL,
  DEBT_TYPE_LABEL,
  EARLY_FEE_TYPE_LABEL,
  INTEREST_METHOD_LABEL,
  LATE_FEE_TYPE_LABEL,
  REMINDER_TYPE_LABEL,
  SCHEDULE_STATUS_BADGE,
  SCHEDULE_STATUS_LABEL,
  SCHEDULE_TYPE_LABEL,
  countdownLabel,
  formatDate,
  formatMoney,
} from "../utils/tronno";

const STATUS_BADGE = { ACTIVE: "badge-blue", PAID: "badge-green", CLOSED: "badge-gray" };

export default function DebtDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [debt, setDebt] = useState(null);
  const [schedule, setSchedule] = useState([]);
  const [payments, setPayments] = useState([]);
  const [rateHistory, setRateHistory] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState("schedule");

  const [payOpen, setPayOpen] = useState(false);

  const [rateOpen, setRateOpen] = useState(false);
  const [rateForm, setRateForm] = useState({
    annualRate: "",
    effectiveFrom: new Date().toISOString().slice(0, 10),
    note: "",
  });

  const [extendOpen, setExtendOpen] = useState(false);
  const [extendForm, setExtendForm] = useState({ extraMonths: 6, newAnnualRate: "", note: "" });

  const [saving, setSaving] = useState(false);

  const load = async () => {
    try {
      const [d, s, p, r, rem] = await Promise.all([
        debtApi.get(id),
        debtApi.schedule(id),
        debtApi.payments(id),
        debtApi.rateHistory(id),
        reminderApi.listByDebt(id),
      ]);
      setDebt(d.data);
      setSchedule(s.data);
      setPayments(p.data);
      setRateHistory(r.data);
      setReminders(rem.data);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không tải được chi tiết khoản nợ");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  if (loading) return <div className="container page loading">Đang tải...</div>;
  if (!debt) return <div className="container page empty">Không tìm thấy khoản nợ</div>;

  const paidAmount = Number(debt.totalPaid || 0);
  const progress = Number(debt.principal) > 0 ? Math.min(100, (paidAmount / Number(debt.principal)) * 100) : 0;

  const reversePayment = async (paymentId) => {
    const reason = window.prompt("Lý do đảo giao dịch (sẽ được lưu vào sổ cái):");
    if (reason === null) return;
    try {
      await debtApi.reversePayment(paymentId, reason);
      toast.success("Đã tạo bút toán đảo, lịch sử được giữ nguyên");
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không đảo được giao dịch");
    }
  };

  const changeRate = async () => {
    setSaving(true);
    try {
      await debtApi.changeRate(id, {
        annualRate: Number(rateForm.annualRate),
        effectiveFrom: rateForm.effectiveFrom,
        note: rateForm.note || null,
      });
      toast.success("Đã cập nhật lãi suất và tái tính lịch trả");
      setRateOpen(false);
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không cập nhật được lãi suất");
    } finally {
      setSaving(false);
    }
  };

  const extendDebt = async () => {
    setSaving(true);
    try {
      await debtApi.extend(id, {
        extraMonths: Number(extendForm.extraMonths),
        newAnnualRate: extendForm.newAnnualRate ? Number(extendForm.newAnnualRate) : null,
        note: extendForm.note || null,
      });
      toast.success("Đã gia hạn khoản nợ");
      setExtendOpen(false);
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không gia hạn được");
    } finally {
      setSaving(false);
    }
  };

  const markPaid = async () => {
    try {
      await debtApi.changeStatus(id, "PAID");
      toast.success("Đã đánh dấu tất toán");
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không cập nhật được");
    }
  };

  return (
    <div className="container page">
      <div className="row-between" style={{ marginBottom: 16 }}>        <div>
          <div className="row" style={{ gap: 10 }}>
            <h1 className="page-title" style={{ margin: 0 }}>
              {debt.name}
            </h1>
            <span className={`badge ${STATUS_BADGE[debt.status]}`}>{DEBT_STATUS_LABEL[debt.status]}</span>
          </div>
          <p className="page-subtitle" style={{ margin: "4px 0 0" }}>
            {DEBT_TYPE_LABEL[debt.debtType]}
            {debt.creditorName ? ` · ${debt.creditorName}` : ""} · Giải ngân{" "}
            {formatDate(debt.disbursementDate)} ·{" "}
            {debt.termType === "QUARTERLY"
              ? `Trả theo quý · ${debt.termMonths} tháng (${Math.round(debt.termMonths / 3)} kỳ)`
              : debt.maturityDate
                ? `Đáo hạn ${formatDate(debt.maturityDate)} · ${debt.termMonths} tháng`
                : `Kỳ hạn ${debt.termMonths} tháng`}
          </p>
        </div>
        <div className="row">
          <button type="button" className="btn btn-primary" onClick={() => setPayOpen(true)}>
            Ghi nhận thanh toán
          </button>
          <button type="button" className="btn btn-outline" onClick={() => setRateOpen(true)}>
            Đổi lãi suất
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => setExtendOpen(true)}>
            Gia hạn
          </button>
          {debt.status === "ACTIVE" && (
            <button type="button" className="btn btn-ghost" onClick={markPaid}>
              Đánh dấu tất toán
            </button>
          )}
        </div>
      </div>
      <div className="grid grid-4" style={{ marginBottom: 16 }}>
        <StatCard
          label="Khoản ban đầu"
          value={formatMoney(debt.principal)}
          hint={`Lãi ${Number(debt.annualRate).toFixed(2)}%/năm`}
        />
        <StatCard
          label="Còn phải trả"
          value={formatMoney(debt.remainingPrincipal)}
          hint={`Đã trả ${formatMoney(debt.totalPaid)}`}
          variant="accent"
        />
        <StatCard
          label="Kỳ trả tiếp theo"
          value={debt.nextDueDate ? formatDate(debt.nextDueDate) : "-"}
          hint={
            debt.nextDueDate
              ? `${countdownLabel(debt.nextDueDate)} · ${formatMoney(debt.nextDueAmount)}`
              : "Đã tất toán"
          }
          variant={debt.nextDueDate && new Date(debt.nextDueDate) < new Date() ? "warn" : ""}
        />
        <StatCard
          label="Ngày hết nợ dự kiến"
          value={debt.debtFreeDate ? formatDate(debt.debtFreeDate) : "-"}
          hint={`Lãi đã trả ${formatMoney(debt.totalInterestPaid)}`}
        />
      </div>
      <div className="card">
        <div className="card-header">
          <div className="tabs" style={{ margin: 0, border: 0 }}>
            {[
              { key: "schedule", label: `Lịch trả (${schedule.length})` },
              { key: "payments", label: `Thanh toán (${payments.length})` },
              { key: "rates", label: `Lãi suất (${rateHistory.length})` },
              { key: "reminders", label: `Nhắc hạn (${reminders.length})` },
              { key: "terms", label: "Điều khoản" },
            ].map((t) => (
              <button
                key={t.key}
                type="button"
                className={`tab ${tab === t.key ? "active" : ""}`}
                onClick={() => setTab(t.key)}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
        {tab === "schedule" && (
          <div className="table-wrap">
            {schedule.length ? (
              <table className="table">
                <thead>
                  <tr>
                    <th>Kỳ</th>
                    <th>Chu kỳ</th>
                    <th>Đến hạn</th>
                    <th className="num">Gốc</th>
                    <th className="num">Lãi</th>
                    <th className="num">Phí trễ</th>
                    <th className="num">Tổng</th>
                    <th className="num">Đã trả</th>
                    <th className="num">Còn lại</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {schedule.map((s) => (
                    <tr key={s.id}>
                      <td>#{s.installmentNo}</td>
                      <td className="muted" style={{ fontSize: 12.5 }}>
                        {formatDate(s.periodStart)} → {formatDate(s.periodEnd)}
                      </td>
                      <td>
                        {formatDate(s.dueDate)}
                        {s.status !== "PAID" && (
                          <div className="muted" style={{ fontSize: 12 }}>{countdownLabel(s.dueDate)}</div>
                        )}
                      </td>
                      <td className="num">{formatMoney(s.principalDue)}</td>
                      <td className="num">{formatMoney(s.interestDue)}</td>
                      <td className="num">
                        {Number(s.currentLateFee) > 0 ? (
                          <span style={{ color: "var(--color-danger)", fontWeight: 600 }}>
                            {formatMoney(s.currentLateFee)}
                          </span>
                        ) : (
                          "-"
                        )}
                      </td>
                      <td className="num">{formatMoney(s.totalDue)}</td>
                      <td className="num">{formatMoney(s.paidAmount)}</td>
                      <td className="num">
                        <strong>{formatMoney(s.remaining)}</strong>
                      </td>
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
              <div className="empty">Chưa có lịch trả</div>
            )}
          </div>
        )}

        {tab === "payments" && (
          <div className="table-wrap">
            {payments.length ? (
              <table className="table">
                <thead>
                  <tr>
                    <th>Ngày trả</th>
                    <th className="num">Số tiền</th>
                    <th className="num">Phân bổ</th>
                    <th>Trạng thái</th>
                    <th>Ghi chú</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map((p) => (
                    <tr key={p.id}>
                      <td>{formatDate(p.paidAt)}</td>
                      <td className="num">{formatMoney(p.amount)}</td>
                      <td className="num">
                        {p.allocations?.length ? (
                          <div style={{ display: "flex", flexDirection: "column", gap: 2, fontSize: 12 }}>
                            {p.allocations.map((a, i) => (
                              <span key={i}>
                                {ALLOCATION_COMPONENT_LABEL[a.component]}: {formatMoney(a.amount)}
                              </span>
                            ))}
                          </div>
                        ) : (
                          "-"
                        )}
                      </td>
                      <td>
                        <span className={`badge ${p.status === "REVERSED" ? "badge-red" : "badge-green"}`}>
                          {p.status === "REVERSED" ? "Đã đảo" : "Đã ghi nhận"}
                        </span>
                      </td>
                      <td className="muted">{p.note || "-"}</td>
                      <td>
                        {p.status !== "REVERSED" && (
                          <button
                            type="button"
                            className="btn btn-ghost btn-sm"
                            onClick={() => reversePayment(p.id)}
                          >
                            Bút toán đảo
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty">Chưa có khoản trả nào</div>
            )}
          </div>
        )}

        {tab === "rates" && (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Lãi suất</th>
                  <th>Hiệu lực từ</th>
                  <th>Đến</th>
                  <th>Ghi chú</th>
                </tr>
              </thead>
              <tbody>
                {rateHistory.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <strong>{Number(r.annualRate).toFixed(2)}%</strong>/năm
                    </td>
                    <td>{formatDate(r.effectiveFrom)}</td>
                    <td>{r.effectiveTo ? formatDate(r.effectiveTo) : "Hiện tại"}</td>
                    <td className="muted">{r.note || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === "reminders" && (
          <div className="card-body">
            <div className="alert alert-info" style={{ marginBottom: 14 }}>
              Hệ thống tự động nhắc trả nợ mỗi ngày: bắt đầu từ 5 ngày trước hạn và tiếp tục nhắc
              mỗi ngày cho tới khi khoản nợ được thanh toán. Email nhắc nợ được gửi tới chính bạn
              (địa chỉ email của tài khoản đang đăng nhập).
            </div>
            <div className="table-wrap">
              {reminders.length ? (
                <table className="table">
                  <thead>
                    <tr>
                      <th>Mức nhắc</th>
                      <th>Ngày nhắc</th>
                      <th className="num">Độ ưu tiên</th>
                      <th>Trạng thái</th>
                      <th>Nội dung</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reminders.map((r) => (
                      <tr key={r.id}>
                        <td>{REMINDER_TYPE_LABEL[r.remindType] || r.remindType}</td>
                        <td>{formatDate(r.remindAt)}</td>
                        <td className="num">{r.priority}</td>
                        <td>
                          <span className="badge badge-gray">{r.status}</span>
                        </td>
                        <td className="muted" style={{ fontSize: 12.5, whiteSpace: "pre-line" }}>
                          {r.message}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="empty">Chưa có thông báo nhắc hạn nào</div>
              )}
            </div>
          </div>
        )}

        {tab === "terms" && (
          <div className="card-body">
            <div className="grid grid-2">
              <div>
                <div className="detail-spec">
                  <span>Kiểu tính lãi</span>
                  <span>{INTEREST_METHOD_LABEL[debt.interestMethod]}</span>
                </div>
                <div className="detail-spec">
                  <span>Kiểu lịch trả</span>
                  <span>{SCHEDULE_TYPE_LABEL[debt.scheduleType]}</span>
                </div>
                <div className="detail-spec">
                  <span>Số tiền mỗi kỳ</span>
                  <span>{formatMoney(debt.installmentAmount)}</span>
                </div>
                <div className="detail-spec">
                  <span>Ngày giải ngân</span>
                  <span>{formatDate(debt.disbursementDate)}</span>
                </div>
                <div className="detail-spec">
                  <span>Ngày ân hạn</span>
                  <span>{debt.graceDays} ngày</span>
                </div>
              </div>
              <div>
                <div className="detail-spec">
                  <span>Phí phạt trễ</span>
                  <span>
                    {LATE_FEE_TYPE_LABEL[debt.lateFeeType]}
                    {debt.lateFeeType === "PERCENT_PER_DAY" &&
                      ` (${Number(debt.lateFeeRate).toFixed(3)}%/ngày)`}
                    {debt.lateFeeType === "FIXED_PER_DAY" &&
                      ` (${formatMoney(debt.lateFeeAmount)}/ngày)`}
                  </span>
                </div>
                <div className="detail-spec">
                  <span>Phí tất toán sớm</span>
                  <span>
                    {EARLY_FEE_TYPE_LABEL[debt.earlyFeeType]}
                    {debt.earlyFeeType === "PERCENT" && ` (${Number(debt.earlyFeeRate).toFixed(2)}%)`}
                    {debt.earlyFeeType === "FLAT" && ` (${formatMoney(debt.earlyFeeAmount)})`}
                  </span>
                </div>
                <div className="detail-spec">
                  <span>Đã trả</span>
                  <span>{formatMoney(debt.totalPaid)}</span>
                </div>
                <div className="detail-spec">
                  <span>Lãi đã trả</span>
                  <span>{formatMoney(debt.totalInterestPaid)}</span>
                </div>
                <div className="detail-spec">
                  <span>Ghi chú</span>
                  <span>{debt.note || "-"}</span>
                </div>
                {debt.contractUrl && (
                  <div className="detail-spec">
                    <span>Hợp đồng</span>
                    <span>
                      <a
                        href={debt.contractUrl}
                        target="_blank"
                        rel="noreferrer"
                        style={{ color: "var(--color-primary)" }}
                      >
                        Mở tài liệu
                      </a>
                    </span>
                  </div>
                )}
              </div>
            </div>
            <div style={{ marginTop: 16 }}>
              <div className="row-between" style={{ marginBottom: 6 }}>
                <span className="muted" style={{ fontSize: 13 }}>
                  Tiến độ tất toán
                </span>
                <span style={{ fontSize: 13, fontWeight: 700 }}>{progress.toFixed(1)}%</span>
              </div>
              <div className="progress">
                <div className="progress-bar green" style={{ width: `${progress}%` }} />
              </div>
            </div>
            <div className="row" style={{ marginTop: 18 }}>
              <Link to={`/debts/${debt.id}/edit`} className="btn btn-outline">
                Sửa khoản nợ
              </Link>
              <button type="button" className="btn btn-ghost" onClick={() => navigate("/debts")}>
                Quay lại danh sách
              </button>
            </div>
          </div>
        )}
      </div>
      <PaymentModal
        open={payOpen}
        debtId={debt?.id}
        debtName={debt?.name}
        onClose={() => setPayOpen(false)}
        onPaid={load}
      />
      <Modal
        open={rateOpen}
        title="Điều chỉnh lãi suất (lãi thả nổi)"
        onClose={() => setRateOpen(false)}
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setRateOpen(false)}>
              Huỷ
            </button>
            <button type="button" className="btn btn-primary" onClick={changeRate} disabled={saving}>
              {saving ? "Đang lưu..." : "Áp dụng"}
            </button>
          </>
        }
      >
        <div className="alert alert-info" style={{ marginBottom: 16 }}>
          Lịch trả sẽ được tái tính. Các kỳ đã trả xong vẫn giữ nguyên lịch sử.
        </div>
        <div className="grid grid-2">
          <div className="field">
            <label htmlFor="newRate">Lãi suất mới (%/năm)</label>
            <input
              id="newRate"
              className="input"
              type="number"
              min="0"
              step="0.01"
              value={rateForm.annualRate}
              onChange={(e) => setRateForm((f) => ({ ...f, annualRate: e.target.value }))}
            />
          </div>
          <div className="field">
            <label htmlFor="rateFrom">Hiệu lực từ ngày</label>
            <input
              id="rateFrom"
              className="input"
              type="date"
              value={rateForm.effectiveFrom}
              onChange={(e) => setRateForm((f) => ({ ...f, effectiveFrom: e.target.value }))}
            />
          </div>
        </div>
        <div className="field">
          <label htmlFor="rateNote">Ghi chú</label>
          <input
            id="rateNote"
            className="input"
            placeholder="VD: Ngân hàng điều chỉnh lãi suất theo thông báo"
            value={rateForm.note}
            onChange={(e) => setRateForm((f) => ({ ...f, note: e.target.value }))}
          />
        </div>
      </Modal>
      <Modal
        open={extendOpen}
        title="Gia hạn khoản nợ"
        onClose={() => setExtendOpen(false)}
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setExtendOpen(false)}>
              Huỷ
            </button>
            <button type="button" className="btn btn-primary" onClick={extendDebt} disabled={saving}>
              {saving ? "Đang lưu..." : "Gia hạn"}
            </button>
          </>
        }
      >
        <div className="field">
          <label htmlFor="extraMonths">Số tháng gia hạn thêm</label>
          <input
            id="extraMonths"
            className="input"
            type="number"
            min="1"
            max="120"
            value={extendForm.extraMonths}
            onChange={(e) => setExtendForm((f) => ({ ...f, extraMonths: e.target.value }))}
          />
        </div>
        <div className="field">
          <label htmlFor="extendRate">Lãi suất mới (%/năm) - để trống nếu giữ nguyên</label>
          <input
            id="extendRate"
            className="input"
            type="number"
            min="0"
            step="0.01"
            value={extendForm.newAnnualRate}
            onChange={(e) => setExtendForm((f) => ({ ...f, newAnnualRate: e.target.value }))}
          />
        </div>
        <div className="field">
          <label htmlFor="extendNote">Ghi chú</label>
          <input
            id="extendNote"
            className="input"
            value={extendForm.note}
            onChange={(e) => setExtendForm((f) => ({ ...f, note: e.target.value }))}
          />
        </div>
      </Modal>
    </div>
  );
}
