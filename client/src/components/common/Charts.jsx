import {

  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatCompactMoney, formatMoney } from "../../utils/tronno";

const tooltipStyle = {
  background: "#fff",
  border: "1px solid var(--color-border)",
  borderRadius: 10,
  fontSize: 12.5,
  boxShadow: "0 4px 16px rgba(16,24,40,0.08)",
};

const axisProps = {
  stroke: "#8a91a3",
  fontSize: 11.5,
  tickLine: false,
  axisLine: false,
};

export function PayoffCurveChart({ data }) {
  if (!data?.length) return <div className="empty">Chưa có dữ liệu dốc nợ</div>;
  const chartData = data.map((p) => ({
    date: p.date,
    remaining: Number(p.remaining),
    interest: Number(p.interest),
  }));

  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="payoffFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#4f46e5" stopOpacity={0.35} />
              <stop offset="100%" stopColor="#4f46e5" stopOpacity={0.02} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#eef0f5" vertical={false} />
          <XAxis dataKey="date" {...axisProps} minTickGap={28} />
          <YAxis {...axisProps} tickFormatter={formatCompactMoney} width={64} />
          <Tooltip
            contentStyle={tooltipStyle}
            formatter={(value, name) => [
              formatMoney(value),
              name === "remaining" ? "Còn phải trả" : "Lãi cộng dồn",
            ]}
          />
          <Area
            type="monotone"
            dataKey="remaining"
            stroke="#4f46e5"
            strokeWidth={2.5}
            fill="url(#payoffFill)"
          />
          <Line type="monotone" dataKey="interest" stroke="#f59e0b" strokeWidth={2} dot={false} />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

export function CashflowChart({ data }) {
  if (!data?.length) return <div className="empty">Chưa có dữ liệu dòng tiền</div>;
  const chartData = data.map((m) => ({
    month: m.month,
    due: Number(m.due),
    paid: Number(m.paid),
    remaining: Number(m.remaining),
  }));

  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#eef0f5" vertical={false} />
          <XAxis dataKey="month" {...axisProps} />
          <YAxis {...axisProps} tickFormatter={formatCompactMoney} width={64} />
          <Tooltip
            contentStyle={tooltipStyle}
            formatter={(value, name) => [
              formatMoney(value),
              { due: "Đến hạn", paid: "Đã trả", remaining: "Còn lại" }[name] || name,
            ]}
          />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Bar dataKey="due" fill="#c7d2fe" radius={[4, 4, 0, 0]} name="Đến hạn" />
          <Bar dataKey="paid" fill="#4f46e5" radius={[4, 4, 0, 0]} name="Đã trả" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
