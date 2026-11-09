export const DEBT_TYPE_LABEL = {
  BANK_LOAN: "Vay ngân hàng",
  CREDIT_CARD: "Thẻ tín dụng",
  INSTALLMENT: "Trả góp (có lãi)",
  INSTALLMENT_ZERO: "Trả góp 0%",
  FRIEND: "Vay bạn bè / người thân",
  APP_LOAN: "Vay app",
};

export const INTEREST_METHOD_LABEL = {
  REDUCING: "Dư nợ giảm dần",
  SIMPLE: "Lãi đơn",
  COMPOUND: "Lãi kép",
};

export const SCHEDULE_TYPE_LABEL = {
  EQUAL_PRINCIPAL: "Gốc đều",
  ANNUITY: "Niên kim (trả đều)",
  INTEREST_ONLY: "Chỉ trả lãi, gốc cuối kỳ",
};

export const LATE_FEE_TYPE_LABEL = {
  NONE: "Không phạt",
  PERCENT_PER_DAY: "% trên dư nợ mỗi ngày",
  FIXED_PER_DAY: "Số tiền cố định mỗi ngày",
};

export const EARLY_FEE_TYPE_LABEL = {
  NONE: "Không phạt",
  PERCENT: "% trên gốc còn lại",
  FLAT: "Số tiền cố định",
};

export const DEBT_STATUS_LABEL = {
  ACTIVE: "Đang có",
  PAID: "Đã tất toán",
  CLOSED: "Đã đóng",
};

export const SCHEDULE_STATUS_LABEL = {
  PENDING: "Chưa đến hạn",
  PARTIAL: "Trả một phần",
  PAID: "Đã trả xong",
  OVERDUE: "Quá hạn",
};

export const SCHEDULE_STATUS_BADGE = {
  PENDING: "badge-gray",
  PARTIAL: "badge-amber",
  PAID: "badge-green",
  OVERDUE: "badge-red",
};

export const CREDITOR_KIND_LABEL = {
  BANK: "Ngân hàng",
  FINANCE: "Công ty tài chính",
  APP: "Ứng dụng vay",
  PERSON: "Bạn bè / người thân",
  OTHER: "Khác",
};

export const REMINDER_TYPE_LABEL = {
  BEFORE_DUE: "Sắp tới hạn",
  ON_DUE: "Đến hạn hôm nay",
  OVERDUE: "Đã trễ hạn",
  DAILY: "Nhắc hằng ngày",
};

export const REMINDER_STATUS_LABEL = {
  PENDING: "Chờ gửi",
  SENT: "Đã gửi",
  FAILED: "Gửi lỗi",
  SKIPPED: "Bỏ qua",
};

export const TERM_TYPE_LABEL = {
  FIXED_DATE: "Chọn ngày đáo hạn",
  QUARTERLY: "Trả theo quý (3 tháng/lần)",
};

export const ALLOCATION_COMPONENT_LABEL = {
  LATE_FEE: "Phí phạt trễ",
  FEE: "Phí",
  INTEREST: "Lãi",
  PRINCIPAL: "Gốc",
};

export const ROLE_LABEL = {
  MEMBER: "Người dùng",
  ADMIN: "Quản trị viên",
};

export const formatMoney = (value, withCurrency = true) => {
  const number = Number(value || 0);
  const text = number.toLocaleString("vi-VN", { maximumFractionDigits: 2 });
  return withCurrency ? `${text} ₫` : text;
};

export const formatCompactMoney = (value) => {
  const number = Number(value || 0);
  const abs = Math.abs(number);
  if (abs >= 1_000_000_000) return `${(number / 1_000_000_000).toLocaleString("vi-VN", { maximumFractionDigits: 1 })} tỷ`;
  if (abs >= 1_000_000) return `${(number / 1_000_000).toLocaleString("vi-VN", { maximumFractionDigits: 1 })} tr`;
  if (abs >= 1_000) return `${(number / 1_000).toLocaleString("vi-VN", { maximumFractionDigits: 0 })}k`;
  return number.toLocaleString("vi-VN");
};

export const formatDate = (value) => {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
};

export const formatDateTime = (value) => {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const daysUntil = (value) => {
  if (!value) return null;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  d.setHours(0, 0, 0, 0);
  return Math.round((d - today) / 86400000);
};

export const countdownLabel = (value) => {
  const days = daysUntil(value);
  if (days === null) return "-";
  if (days === 0) return "Hôm nay";
  if (days === 1) return "Ngày mai";
  if (days > 0) return `còn ${days} ngày`;
  return `quá hạn ${Math.abs(days)} ngày`;
};

export function homePathForRole(role) {
  return "/";
}

/** Số tiền phải là số tròn (chia hết cho 1000) và tối thiểu 1.000 đồng. */
export const AMOUNT_STEP = 1000;

/**
 * Kiểm tra số tiền có hợp lệ không (số tròn, >= 1000).
 * @returns {string|null} thông báo lỗi, null nếu hợp lệ
 */
export function validateRoundAmount(value) {
  if (value === "" || value === null || value === undefined) {
    return "Vui lòng nhập số tiền";
  }
  const amount = Number(value);
  if (!Number.isFinite(amount)) {
    return "Số tiền không hợp lệ";
  }
  if (amount < AMOUNT_STEP) {
    return "Số tiền tối thiểu 1.000 đồng";
  }
  if (amount % AMOUNT_STEP !== 0) {
    return "Số tiền phải là số tròn (chia hết cho 1.000)";
  }
  return null;
}
