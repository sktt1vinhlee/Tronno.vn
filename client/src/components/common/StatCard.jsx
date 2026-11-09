export default function StatCard({ label, value, hint, variant = "" }) {
  return (
    <div className={`stat ${variant}`}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      {hint && <div className="stat-hint">{hint}</div>}
    </div>
  );
}
