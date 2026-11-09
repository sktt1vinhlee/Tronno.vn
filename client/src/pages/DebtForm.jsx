import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { creditorApi, debtApi } from "../api/tronno";
import {
  AMOUNT_STEP,
  CREDITOR_KIND_LABEL,
  DEBT_TYPE_LABEL,
  EARLY_FEE_TYPE_LABEL,
  INTEREST_METHOD_LABEL,
  LATE_FEE_TYPE_LABEL,
  SCHEDULE_TYPE_LABEL,
  TERM_TYPE_LABEL,
  validateRoundAmount,
} from "../utils/tronno";

const today = () => new Date().toISOString().slice(0, 10);

const EMPTY = {
  name: "",
  debtType: "BANK_LOAN",
  creditorId: "",
  creditorName: "",
  creditorKind: "PERSON",
  creditorPhone: "",
  creditorEmail: "",
  principal: "",
  annualRate: "",
  interestMethod: "REDUCING",
  scheduleType: "EQUAL_PRINCIPAL",
  disbursementDate: today(),
  termType: "FIXED_DATE",
  termMonths: 12,
  maturityDate: "",
  lateFeeType: "NONE",
  lateFeeRate: "",
  lateFeeAmount: "",
  earlyFeeType: "NONE",
  earlyFeeRate: "",
  earlyFeeAmount: "",
  graceDays: 0,
  note: "",
  contractUrl: "",
};

export default function DebtForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState(EMPTY);
  const [creditors, setCreditors] = useState([]);
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    creditorApi
      .list()
      .then(({ data }) => setCreditors(data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    debtApi
      .get(id)
      .then(({ data }) => {
        setForm({
          name: data.name || "",
          debtType: data.debtType || "BANK_LOAN",
          creditorId: data.creditorId || "",
          creditorName: "",
          creditorKind: "PERSON",
          creditorPhone: "",
          creditorEmail: "",
          principal: data.principal ?? "",
          annualRate: data.annualRate ?? "",
          interestMethod: data.interestMethod || "REDUCING",
          scheduleType: data.scheduleType || "EQUAL_PRINCIPAL",
          disbursementDate: data.disbursementDate || today(),
          termType: data.termType || "FIXED_DATE",
          termMonths: data.termMonths ?? 12,
          maturityDate: data.maturityDate || "",
          lateFeeType: data.lateFeeType || "NONE",
          lateFeeRate: data.lateFeeRate ?? "",
          lateFeeAmount: data.lateFeeAmount ?? "",
          earlyFeeType: data.earlyFeeType || "NONE",
          earlyFeeRate: data.earlyFeeRate ?? "",
          earlyFeeAmount: data.earlyFeeAmount ?? "",
          graceDays: data.graceDays ?? 0,
          note: data.note || "",
          contractUrl: data.contractUrl || "",
        });
      })
      .catch(() => toast.error("Không tìm thấy khoản nợ"))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  // Khi chọn chủ nợ có sẵn thì không cần nhập tay.
  const selectCreditor = (e) => {
    const creditorId = e.target.value;
    const picked = creditors.find((c) => String(c.id) === String(creditorId));
    setForm((f) => ({
      ...f,
      creditorId,
      creditorName: picked ? picked.name : "",
      creditorKind: picked ? picked.kind : f.creditorKind,
      creditorPhone: picked ? picked.phone || "" : "",
      creditorEmail: picked ? picked.email || "" : "",
    }));
  };

  const submit = async (e) => {
    e.preventDefault();
    setError("");

    const amountError = validateRoundAmount(form.principal);
    if (amountError) {
      setError(amountError);
      return;
    }

    setSaving(true);
    try {
      const payload = {
        ...form,
        creditorId: form.creditorId ? Number(form.creditorId) : null,
        creditorName: form.creditorId ? null : form.creditorName.trim() || null,
        principal: Number(form.principal),
        annualRate: Number(form.annualRate || 0),
        termMonths: Number(form.termMonths),
        termType: form.termType || "FIXED_DATE",
        maturityDate:
          form.termType === "FIXED_DATE" && form.maturityDate ? form.maturityDate : null,
        graceDays: Number(form.graceDays || 0),
        lateFeeRate: Number(form.lateFeeRate || 0),
        lateFeeAmount: Number(form.lateFeeAmount || 0),
        earlyFeeRate: Number(form.earlyFeeRate || 0),
        earlyFeeAmount: Number(form.earlyFeeAmount || 0),
      };
      if (isEdit) {
        await debtApi.update(id, payload);
        toast.success("Đã cập nhật khoản nợ và tái tính lịch trả");
        navigate(`/debts/${id}`);
      } else {
        const { data } = await debtApi.create(payload);
        toast.success("Đã tạo khoản nợ và sinh lịch trả");
        navigate(`/debts/${data.id}`);
      }
    } catch (err) {
      setError(err?.response?.data?.message || "Không lưu được khoản nợ");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="container page loading">Đang tải...</div>;

  return (
    <div className="container page" style={{ maxWidth: 880 }}>
      <h1 className="page-title">{isEdit ? "Sửa khoản phải trả" : "Thêm khoản phải trả"}</h1>
      <p className="page-subtitle">
        Ghi lại khoản tiền bạn đang nợ và người bạn cần trả. Tất cả thông tin nhập tay.
      </p>
      <form onSubmit={submit}>
        {error && (
          <div className="alert alert-danger" style={{ marginBottom: 14 }}>
            {error}
          </div>
        )}

        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h2>Thông tin khoản cần trả</h2>
          </div>
          <div className="card-body">
            <div className="grid grid-2">
              <div className="field">
                <label htmlFor="name">Tên khoản</label>
                <input
                  id="name"
                  className="input"
                  required
                  placeholder="VD: Tiền vay ngân hàng, tiền mượn bạn"
                  value={form.name}
                  onChange={set("name")}
                />
              </div>
              <div className="field">
                <label htmlFor="debtType">Loại nợ</label>
                <select id="debtType" className="select" value={form.debtType} onChange={set("debtType")}>
                  {Object.entries(DEBT_TYPE_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="grid grid-2">
              <div className="field">
                <label htmlFor="principal">Số tiền ban đầu</label>
                <input
                  id="principal"
                  className="input"
                  type="number"
                  min={AMOUNT_STEP}
                  step={AMOUNT_STEP}
                  required
                  value={form.principal}
                  onChange={set("principal")}
                />
                <span className="field-hint">
                  Số tròn, tối thiểu 1.000 đồng (VD: 35.000, 60.000).
                </span>
              </div>
              <div className="field">
                <label htmlFor="annualRate">Lãi suất năm (%)</label>
                <input
                  id="annualRate"
                  className="input"
                  type="number"
                  min="0"
                  step="0.01"
                  required
                  value={form.annualRate}
                  onChange={set("annualRate")}
                />
                <span className="field-hint">Nhập 0 cho trả góp 0%</span>
              </div>
            </div>
          </div>
        </div>

        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h2>Người bạn cần trả</h2>
          </div>
          <div className="card-body">
            <div className="field" style={{ marginBottom: 14 }}>
              <label htmlFor="creditorId">Chọn chủ nợ đã có</label>
              <select id="creditorId" className="select" value={form.creditorId} onChange={selectCreditor}>
                <option value="">-- Nhập tay chủ nợ mới --</option>
                {creditors.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({CREDITOR_KIND_LABEL[c.kind]})
                  </option>
                ))}
              </select>
              <span className="field-hint">
                Để trống nếu bạn nhập tay chủ nợ bên dưới.
              </span>
            </div>
            {!form.creditorId && (
              <>
                <div className="grid grid-2">
                  <div className="field">
                    <label htmlFor="creditorName">Tên chủ nợ</label>
                    <input
                      id="creditorName"
                      className="input"
                      required
                      placeholder="VD: Ngân hàng Techcombank, bạn Minh Anh"
                      value={form.creditorName}
                      onChange={set("creditorName")}
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="creditorKind">Loại chủ nợ</label>
                    <select
                      id="creditorKind"
                      className="select"
                      value={form.creditorKind}
                      onChange={set("creditorKind")}
                    >
                      {Object.entries(CREDITOR_KIND_LABEL).map(([k, v]) => (
                        <option key={k} value={k}>
                          {v}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="grid grid-2">
                  <div className="field">
                    <label htmlFor="creditorPhone">Số điện thoại</label>
                    <input
                      id="creditorPhone"
                      className="input"
                      value={form.creditorPhone}
                      onChange={set("creditorPhone")}
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="creditorEmail">Email</label>
                    <input
                      id="creditorEmail"
                      className="input"
                      type="email"
                      value={form.creditorEmail}
                      onChange={set("creditorEmail")}
                    />
                    <span className="field-hint">
                      Email của chủ nợ, chỉ để lưu thông tin liên hệ.
                    </span>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>

        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h2>Kỳ hạn và cách tính</h2>
          </div>
          <div className="card-body">
            <div className="grid grid-3">
              <div className="field">
                <label htmlFor="disbursementDate">Ngày bắt đầu khoản nợ</label>
                <input
                  id="disbursementDate"
                  className="input"
                  type="date"
                  required
                  value={form.disbursementDate}
                  onChange={set("disbursementDate")}
                />
              </div>
              <div className="field">
                <label htmlFor="termType">Kiểu thời hạn trả</label>
                <select
                  id="termType"
                  className="select"
                  value={form.termType}
                  onChange={set("termType")}
                >
                  {Object.entries(TERM_TYPE_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
              {form.termType === "FIXED_DATE" ? (
                <div className="field">
                  <label htmlFor="maturityDate">Ngày đáo hạn</label>
                  <input
                    id="maturityDate"
                    className="input"
                    type="date"
                    required
                    min={form.disbursementDate}
                    value={form.maturityDate}
                    onChange={set("maturityDate")}
                  />
                  <span className="field-hint">
                    Hệ thống tự chia lịch trả theo tháng từ ngày bắt đầu đến ngày này.
                  </span>
                </div>
              ) : (
                <div className="field">
                  <label htmlFor="termMonths">Tổng thời hạn (tháng)</label>
                  <input
                    id="termMonths"
                    className="input"
                    type="number"
                    min="3"
                    max="480"
                    step="3"
                    required
                    value={form.termMonths}
                    onChange={set("termMonths")}
                  />
                  <span className="field-hint">
                    Trả theo quý: mỗi kỳ 3 tháng một lần.
                  </span>
                </div>
              )}
              <div className="field">
                <label htmlFor="graceDays">Số ngày ân hạn</label>
                <input
                  id="graceDays"
                  className="input"
                  type="number"
                  min="0"
                  value={form.graceDays}
                  onChange={set("graceDays")}
                />
              </div>
            </div>
            <div className="grid grid-2">
              <div className="field">
                <label htmlFor="interestMethod">Kiểu tính lãi</label>
                <select
                  id="interestMethod"
                  className="select"
                  value={form.interestMethod}
                  onChange={set("interestMethod")}
                >
                  {Object.entries(INTEREST_METHOD_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label htmlFor="scheduleType">Cách trả</label>
                <select
                  id="scheduleType"
                  className="select"
                  value={form.scheduleType}
                  onChange={set("scheduleType")}
                >
                  {Object.entries(SCHEDULE_TYPE_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        </div>

        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h2>Phí và ghi chú</h2>
          </div>
          <div className="card-body">
            <div className="grid grid-3">
              <div className="field">
                <label htmlFor="lateFeeType">Phí phạt trễ hạn</label>
                <select id="lateFeeType" className="select" value={form.lateFeeType} onChange={set("lateFeeType")}>
                  {Object.entries(LATE_FEE_TYPE_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
              {form.lateFeeType === "PERCENT_PER_DAY" && (
                <div className="field">
                  <label htmlFor="lateFeeRate">Phí trễ (%/ngày)</label>
                  <input
                    id="lateFeeRate"
                    className="input"
                    type="number"
                    min="0"
                    step="0.001"
                    value={form.lateFeeRate}
                    onChange={set("lateFeeRate")}
                  />
                  <span className="field-hint">0.03 = 0,03%/ngày</span>
                </div>
              )}
              {form.lateFeeType === "FIXED_PER_DAY" && (
                <div className="field">
                  <label htmlFor="lateFeeAmount">Phí trễ (đồng/ngày)</label>
                  <input
                    id="lateFeeAmount"
                    className="input"
                    type="number"
                    min="0"
                    step="1000"
                    value={form.lateFeeAmount}
                    onChange={set("lateFeeAmount")}
                  />
                </div>
              )}
              <div className="field">
                <label htmlFor="earlyFeeType">Phí tất toán sớm</label>
                <select id="earlyFeeType" className="select" value={form.earlyFeeType} onChange={set("earlyFeeType")}>
                  {Object.entries(EARLY_FEE_TYPE_LABEL).map(([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>
              {form.earlyFeeType === "PERCENT" && (
                <div className="field">
                  <label htmlFor="earlyFeeRate">Phí tất toán sớm (%)</label>
                  <input
                    id="earlyFeeRate"
                    className="input"
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.earlyFeeRate}
                    onChange={set("earlyFeeRate")}
                  />
                </div>
              )}
              {form.earlyFeeType === "FLAT" && (
                <div className="field">
                  <label htmlFor="earlyFeeAmount">Phí tất toán sớm (đồng)</label>
                  <input
                    id="earlyFeeAmount"
                    className="input"
                    type="number"
                    min="0"
                    step="1000"
                    value={form.earlyFeeAmount}
                    onChange={set("earlyFeeAmount")}
                  />
                </div>
              )}
            </div>
            <div className="field">
              <label htmlFor="note">Ghi chú</label>
              <textarea
                id="note"
                className="textarea"
                rows={3}
                placeholder="VD: hợp đồng số 123, họp trả góp ngày 5 hàng tháng"
                value={form.note}
                onChange={set("note")}
              />
            </div>
            <div className="field">
              <label htmlFor="contractUrl">Link hợp đồng / ảnh</label>
              <input
                id="contractUrl"
                className="input"
                placeholder="https://..."
                value={form.contractUrl}
                onChange={set("contractUrl")}
              />
            </div>
          </div>
        </div>

        <div className="row">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? "Đang lưu..." : isEdit ? "Lưu thay đổi" : "Thêm khoản"}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
            Huỷ
          </button>
        </div>
      </form>
    </div>
  );
}
