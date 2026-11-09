import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { debtApi } from "../../api/tronno";
import Modal from "./Modal";
import { countdownLabel, formatDate, formatMoney, validateRoundAmount } from "../../utils/tronno";

/**
 * Modal ghi nhận thanh toán - dùng chung cho trang danh sách và trang chi tiết khoản nợ.
 */
export default function PaymentModal({ open, debtId, debtName, onClose, onPaid }) {
  const [schedule, setSchedule] = useState([]);
  const [form, setForm] = useState({
    amount: "",
    paidAt: new Date().toISOString().slice(0, 10),
    method: "CASH",
    note: "",
  });
  const [scheduleId, setScheduleId] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open || !debtId) return;
    setForm((f) => ({ ...f, amount: "", note: "", paidAt: new Date().toISOString().slice(0, 10) }));
    setScheduleId("");
    debtApi
      .schedule(debtId)
      .then(({ data }) => setSchedule(data || []))
      .catch(() => setSchedule([]));
  }, [open, debtId]);

  const openSchedule = schedule.find((s) => s.status !== "PAID");

  const submit = async () => {
    const amountError = validateRoundAmount(form.amount);
    if (amountError) {
      toast.error(amountError);
      return;
    }
    setSaving(true);
    try {
      const { data } = await debtApi.pay(debtId, {
        amount: Number(form.amount),
        paidAt: form.paidAt,
        method: form.method,
        note: form.note || null,
        scheduleId: scheduleId ? Number(scheduleId) : null,
      });
      toast.success(
        data.surplus > 0
          ? `Đã ghi nhận ${formatMoney(data.appliedAmount)}, còn dư ${formatMoney(data.surplus)}`
          : `Đã ghi nhận ${formatMoney(data.appliedAmount)}`,
      );
      onPaid?.();
      onClose?.();
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không ghi nhận được thanh toán");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open={open}
      title={debtName ? `Ghi nhận thanh toán - ${debtName}` : "Ghi nhận thanh toán"}
      onClose={onClose}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Huỷ
          </button>
          <button type="button" className="btn btn-primary" onClick={submit} disabled={saving}>
            {saving ? "Đang lưu..." : "Ghi nhận"}
          </button>
        </>
      }
    >
      <div className="alert alert-info" style={{ marginBottom: 16 }}>
        Tiền sẽ được phân bổ theo thứ tự: <strong>phí phạt trễ → phí → lãi → gốc</strong>. Phần trả dư được giữ lại,
        không bị mất.
      </div>
      <div className="grid grid-2">
        <div className="field">
          <label htmlFor="pmAmount">Số tiền thanh toán</label>
          <input
            id="pmAmount"
            className="input"
            type="number"
            min="1000"
            step="1000"
            autoFocus
            value={form.amount}
            onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))}
          />
          <span className="field-hint">Số tròn, tối thiểu 1.000 đồng.</span>
        </div>
        <div className="field">
          <label htmlFor="pmDate">Ngày thanh toán</label>
          <input
            id="pmDate"
            className="input"
            type="date"
            value={form.paidAt}
            onChange={(e) => setForm((f) => ({ ...f, paidAt: e.target.value }))}
          />
        </div>
      </div>
      <div className="grid grid-2">
        <div className="field">
          <label htmlFor="pmSchedule">Ghi vào kỳ trả</label>
          <select
            id="pmSchedule"
            className="select"
            value={scheduleId}
            onChange={(e) => setScheduleId(e.target.value)}
          >
            <option value="">Tự động (kỳ cũ nhất trước)</option>
            {schedule
              .filter((s) => s.status !== "PAID")
              .map((s) => (
                <option key={s.id} value={s.id}>
                  Kỳ #{s.installmentNo} - đến hạn {formatDate(s.dueDate)} - còn {formatMoney(s.remaining)}
                </option>
              ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="pmMethod">Hình thức</label>
          <select
            id="pmMethod"
            className="select"
            value={form.method}
            onChange={(e) => setForm((f) => ({ ...f, method: e.target.value }))}
          >
            <option value="CASH">Tiền mặt</option>
            <option value="TRANSFER">Chuyển khoản</option>
            <option value="CARD">Thẻ</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>
      </div>
      <div className="field">
        <label htmlFor="pmNote">Ghi chú</label>
        <input
          id="pmNote"
          className="input"
          value={form.note}
          onChange={(e) => setForm((f) => ({ ...f, note: e.target.value }))}
        />
      </div>
      {openSchedule && (
        <div className="alert alert-warning">
          Kỳ sắp đến hạn: <strong>#{openSchedule.installmentNo}</strong> - {formatDate(openSchedule.dueDate)} - còn{" "}
          {formatMoney(openSchedule.remaining)} ({countdownLabel(openSchedule.dueDate)})
          {Number(openSchedule.currentLateFee) > 0 && (
            <div>Phí phạt trễ hiện tại: {formatMoney(openSchedule.currentLateFee)}</div>
          )}
        </div>
      )}
    </Modal>
  );
}
