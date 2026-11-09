import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { debtApi, letterApi } from "../api/tronno";
import { DEBT_TYPE_LABEL, formatMoney } from "../utils/tronno";

const TONES = [
  { key: "LIGHT", label: "Nhẹ nhàng", hint: "Giữ quan hệ tốt, ưu tiên thương lượng" },
  { key: "MEDIUM", label: "Trung bình", hint: "Xin gia hạn, nhận rõ nghĩa vụ" },
  { key: "HARD", label: "Dứt khoát", hint: "Thể hiện thiện chí, đề xuất mốc mới" },
];

export default function LetterPage() {
  const [tab, setTab] = useState("letter");

  const [debts, setDebts] = useState([]);
  const [letterForm, setLetterForm] = useState({ debtId: "", tone: "MEDIUM" });
  const [letter, setLetter] = useState(null);
  const [generating, setGenerating] = useState(false);

  const [rateForm, setRateForm] = useState({
    annualRate: "",
    amount: "",
    termMonths: 12,
    appLoan: false,
  });
  const [rateResult, setRateResult] = useState(null);
  const [checking, setChecking] = useState(false);

  useEffect(() => {
    debtApi
      .list()
      .then(({ data }) => setDebts(data.filter((d) => d.status === "ACTIVE")))
      .catch(() => {});
  }, []);

  const generate = async () => {
    setGenerating(true);
    try {
      const { data } = await letterApi.compose({
        debtId: letterForm.debtId ? Number(letterForm.debtId) : null,
        tone: letterForm.tone,
      });
      setLetter(data);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không soạn được văn bản");
    } finally {
      setGenerating(false);
    }
  };

  const checkRate = async () => {
    setChecking(true);
    try {
      const { data } = await letterApi.rateCheck({
        annualRate: Number(rateForm.annualRate),
        amount: Number(rateForm.amount),
        termMonths: Number(rateForm.termMonths),
        appLoan: rateForm.appLoan,
      });
      setRateResult(data);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Không kiểm tra được lãi suất");
    } finally {
      setChecking(false);
    }
  };

  const copy = async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      toast.success("Đã sao chép nội dung");
    } catch {
      toast.error("Trình duyệt không cho phép sao chép");
    }
  };

  const severityClass = {
    OK: "alert-success",
    WARNING: "alert-warning",
    DANGER: "alert-danger",
  };

  return (
    <div className="container page">
      <h1 className="page-title">Soạn văn</h1>
      <p className="page-subtitle">
        Soạn tin nhắn gửi người bạn cần trả và kiểm tra lãi suất khoản vay.
      </p>
      <div className="tabs">
        <button
          type="button"
          className={`tab ${tab === "letter" ? "active" : ""}`}
          onClick={() => setTab("letter")}
        >
          Soạn thông báo
        </button>
        <button
          type="button"
          className={`tab ${tab === "rate" ? "active" : ""}`}
          onClick={() => setTab("rate")}
        >
          Cảnh báo tín dụng đen
        </button>
      </div>
      {tab === "letter" && (
        <div className="grid grid-2">
          <div className="card">
            <div className="card-header">
              <h2>Tuỳ chọn</h2>
            </div>
            <div className="card-body">
              <div className="field">
                <label htmlFor="letterDebt">Khoản nợ liên quan</label>
                <select
                  id="letterDebt"
                  className="select"
                  value={letterForm.debtId}
                  onChange={(e) => setLetterForm((f) => ({ ...f, debtId: e.target.value }))}
                >
                  <option value="">Không chỉ định</option>
                  {debts.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name} - {DEBT_TYPE_LABEL[d.debtType]} - {formatMoney(d.remainingPrincipal)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Giọng văn</label>
                {TONES.map((t) => (
                  <label
                    key={t.key}
                    className="row"
                    style={{
                      gap: 10,
                      padding: "10px 12px",
                      border: `1px solid ${
                        letterForm.tone === t.key ? "var(--color-primary)" : "var(--color-border)"
                      }`,
                      borderRadius: 10,
                      marginBottom: 8,
                      cursor: "pointer",
                      background: letterForm.tone === t.key ? "var(--color-primary-soft)" : "#fff",
                    }}
                  >
                    <input
                      type="radio"
                      name="tone"
                      checked={letterForm.tone === t.key}
                      onChange={() => setLetterForm((f) => ({ ...f, tone: t.key }))}
                    />
                    <span>
                      <strong style={{ display: "block", fontSize: 14 }}>{t.label}</strong>
                      <span className="muted" style={{ fontSize: 12.5 }}>
                        {t.hint}
                      </span>
                    </span>
                  </label>
                ))}
              </div>
              <button
                type="button"
                className="btn btn-primary btn-block"
                onClick={generate}
                disabled={generating}
              >
                {generating ? "Đang soạn..." : "Soạn văn bản"}
              </button>
              <div className="alert alert-warning" style={{ marginTop: 16, fontSize: 12.5 }}>
                Nội dung do hệ thống soạn thảo tự động chỉ mang tính tham khảo. Thông báo chậm thanh toán vẫn là
                vi phạm hợp đồng nếu không có sự đồng thuận của người cho vay.
              </div>
            </div>
          </div>
          <div className="card">
            <div className="card-header">
              <h2>Văn bản</h2>
              {letter && (
                <button type="button" className="btn btn-outline btn-sm" onClick={() => copy(letter.body)}>
                  Sao chép
                </button>
              )}
            </div>
            <div className="card-body">
              {letter ? (
                <>
                  <h3 style={{ marginTop: 0, fontSize: 16 }}>{letter.title}</h3>
                  <pre
                    style={{
                      whiteSpace: "pre-wrap",
                      fontFamily: "inherit",
                      background: "#f9fafb",
                      border: "1px solid var(--color-border)",
                      borderRadius: 10,
                      padding: 16,
                      fontSize: 14,
                      lineHeight: 1.6,
                    }}
                  >
                    {letter.body}
                  </pre>
                  <div className="detail-spec" style={{ marginTop: 14 }}>
                    <span>Cách gửi</span>
                    <span>{letter.delivery}</span>
                  </div>
                  <div className="alert alert-info" style={{ marginTop: 12, fontSize: 12.5 }}>
                    {letter.disclaimer}
                  </div>
                </>
              ) : (
                <div className="empty">Chọn giọng văn và bấm "Soạn văn bản"</div>
              )}
            </div>
          </div>
        </div>
      )}

      {tab === "rate" && (
        <div className="grid grid-2">
          <div className="card">
            <div className="card-header">
              <h2>Kiểm tra lãi suất</h2>
            </div>
            <div className="card-body">
              <div className="field">
                <label htmlFor="rcRate">Lãi suất (%/năm)</label>
                <input
                  id="rcRate"
                  className="input"
                  type="number"
                  min="0"
                  step="0.01"
                  value={rateForm.annualRate}
                  onChange={(e) => setRateForm((f) => ({ ...f, annualRate: e.target.value }))}
                />
              </div>
              <div className="field">
                <label htmlFor="rcAmount">Số tiền vay</label>
                <input
                  id="rcAmount"
                  className="input"
                  type="number"
                  min="0"
                  step="1000"
                  value={rateForm.amount}
                  onChange={(e) => setRateForm((f) => ({ ...f, amount: e.target.value }))}
                />
              </div>
              <div className="field">
                <label htmlFor="rcTerm">Kỳ hạn (tháng)</label>
                <input
                  id="rcTerm"
                  className="input"
                  type="number"
                  min="1"
                  max="480"
                  value={rateForm.termMonths}
                  onChange={(e) => setRateForm((f) => ({ ...f, termMonths: e.target.value }))}
                />
              </div>
              <label className="row" style={{ gap: 8, marginBottom: 14, fontSize: 14 }}>
                <input
                  type="checkbox"
                  checked={rateForm.appLoan}
                  onChange={(e) => setRateForm((f) => ({ ...f, appLoan: e.target.checked }))}
                />
                <span>Đây là khoản vay qua ứng dụng</span>
              </label>
              <button
                type="button"
                className="btn btn-primary btn-block"
                onClick={checkRate}
                disabled={checking || !rateForm.annualRate || !rateForm.amount}
              >
                {checking ? "Đang kiểm tra..." : "Kiểm tra"}
              </button>
            </div>
          </div>
          <div className="card">
            <div className="card-header">
              <h2>Kết quả</h2>
            </div>
            <div className="card-body">
              {rateResult ? (
                <>
                  <div className={`alert ${severityClass[rateResult.severity]}`}>{rateResult.message}</div>
                  <div className="detail-spec" style={{ marginTop: 14 }}>
                    <span>Lãi suất kiểm tra</span>
                    <span>{Number(rateResult.annualRate).toFixed(2)}%/năm</span>
                  </div>
                  <div className="detail-spec">
                    <span>Trần lãi suất pháp lý</span>
                    <span>{Number(rateResult.legalCapPercent).toFixed(0)}%/năm</span>
                  </div>
                  <div className="detail-spec">
                    <span>Lãi hàng tháng ước tính</span>
                    <span>{formatMoney(rateResult.monthlyInterest)}</span>
                  </div>
                  <div className="detail-spec">
                    <span>Tổng lãi cả kỳ</span>
                    <span>{formatMoney(rateResult.totalInterest)}</span>
                  </div>
                  <div className="detail-spec">
                    <span>Lãi tối đa theo trần pháp lý</span>
                    <span>{formatMoney(rateResult.maxLegalMonthlyInterest)}</span>
                  </div>
                  <div className="alert alert-info" style={{ marginTop: 14, fontSize: 13 }}>
                    <strong>Hướng xử lý:</strong> {rateResult.guidance}
                  </div>
                  <div className="alert alert-warning" style={{ marginTop: 10, fontSize: 12.5 }}>
                    {rateResult.disclaimer}
                  </div>
                </>
              ) : (
                <div className="empty">Nhập lãi suất và số tiền để kiểm tra</div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
