import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

const STAT_CARDS = [
  { key: "totalPatients",       label: "Total Patients",      color: "bg-blue-500",   icon: "👥" },
  { key: "totalNurses",         label: "Total Nurses",         color: "bg-purple-500", icon: "👩‍⚕️" },
  { key: "todayAppointments",   label: "Today's Appointments", color: "bg-green-500",  icon: "📅" },
  { key: "totalConsultations",  label: "Consultations",        color: "bg-yellow-500", icon: "🩺" },
  { key: "totalPrescriptions",  label: "Prescriptions",        color: "bg-pink-500",   icon: "💊" },
  { key: "totalBills",          label: "Total Bills",          color: "bg-red-500",    icon: "🧾" },
  { key: "totalMedicalRecords", label: "Medical Records",      color: "bg-teal-500",   icon: "📋" },
  { key: "totalAppointments",   label: "All Appointments",     color: "bg-indigo-500", icon: "🗓️" },
];

const ACTION_COLORS = {
  CREATED:    "bg-green-100 text-green-700",
  UPDATED:    "bg-blue-100 text-blue-700",
  DELETED:    "bg-red-100 text-red-700",
  PAYMENT:    "bg-yellow-100 text-yellow-700",
  APPROVED:   "bg-blue-100 text-blue-700",
  REJECTED:   "bg-red-100 text-red-700",
  COMPLETED:  "bg-green-100 text-green-700",
  CANCELLED:  "bg-gray-100 text-gray-700",
  ARCHIVED:   "bg-orange-100 text-orange-700",
  UNARCHIVED: "bg-teal-100 text-teal-700",
};

export default function AdminDashboard() {
  const user     = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");

  const fetchSummary = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await axios.get("/reports/summary");
      setSummary(res.data);
    } catch {
      setError("Failed to load dashboard data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSummary(); }, []);

  const timeAgo = (dateStr) => {
    if (!dateStr) return "—";
    const diff  = Date.now() - new Date(dateStr).getTime();
    const mins  = Math.floor(diff / 60000);
    const hours = Math.floor(mins / 60);
    const days  = Math.floor(hours / 24);
    if (days  > 0) return `${days}d ago`;
    if (hours > 0) return `${hours}h ago`;
    if (mins  > 0) return `${mins}m ago`;
    return "Just now";
  };

  return (
    <Layout>
      {/* Header */}
      <div className="flex justify-between items-start mb-8">
        <div>
          <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">
            Admin Overview
          </p>
          <h1 className="text-3xl font-bold text-gray-800">
            Welcome back, {user.fullName || "Admin"} 👋
          </h1>
          <p className="text-gray-500 mt-1">
            {new Date().toLocaleDateString("en-PH", {
              weekday: "long", year: "numeric",
              month: "long", day: "numeric"
            })}
          </p>
        </div>
        <button onClick={fetchSummary}
          className="border border-gray-300 text-gray-600 px-4 py-2
            rounded-lg text-sm hover:bg-gray-100 flex items-center gap-2">
          🔄 Refresh
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600
          rounded-lg p-4 mb-6 flex justify-between items-center">
          <span>{error}</span>
          <button onClick={fetchSummary}
            className="text-sm underline">Retry</button>
        </div>
      )}

      {/* Stat cards */}
      <section className="mb-8">
        <h2 className="text-base font-semibold text-gray-600 mb-4 uppercase
          tracking-wide">System Overview</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {STAT_CARDS.map(({ key, label, color, icon }) => (
            <div key={key}
              className="bg-white rounded-xl shadow-sm border
                border-gray-100 p-4 hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-3">
                <span className="text-2xl">{icon}</span>
                <span className={`${color} text-white text-xs
                  px-2 py-0.5 rounded-full font-medium`}>
                  live
                </span>
              </div>
              <p className="text-3xl font-bold text-gray-800">
                {loading ? (
                  <span className="inline-block w-10 h-8 bg-gray-200
                    animate-pulse rounded" />
                ) : (
                  summary?.[key] ?? "—"
                )}
              </p>
              <p className="text-sm text-gray-500 mt-1">{label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Middle row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">

        {/* Appointment breakdown */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4 flex
            items-center gap-2">
            📅 Appointment Status
          </h3>
          {loading ? <Skeleton rows={4} /> : (
            <div className="space-y-3">
              <StatusBar label="Pending"
                value={summary?.scheduledAppointments}
                total={summary?.totalAppointments}
                color="bg-yellow-400" />
              <StatusBar label="Approved"
                value={summary?.approvedAppointments}
                total={summary?.totalAppointments}
                color="bg-blue-500" />
              <StatusBar label="Completed"
                value={summary?.completedAppointments}
                total={summary?.totalAppointments}
                color="bg-green-500" />
              <StatusBar label="Cancelled"
                value={summary?.cancelledAppointments}
                total={summary?.totalAppointments}
                color="bg-red-400" />
            </div>
          )}
        </div>

        {/* Bill breakdown */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4">
            🧾 Billing Status
          </h3>
          {loading ? <Skeleton rows={3} /> : (
            <div className="space-y-3">
              <StatusBar label="Unpaid"
                value={summary?.unpaidBills}
                total={summary?.totalBills}
                color="bg-red-500" />
              <StatusBar label="Partial"
                value={summary?.partialBills}
                total={summary?.totalBills}
                color="bg-yellow-500" />
              <StatusBar label="Paid"
                value={summary?.paidBills}
                total={summary?.totalBills}
                color="bg-green-500" />
            </div>
          )}
        </div>

        {/* Quick actions */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4">
            ⚡ Quick Actions
          </h3>
          <div className="space-y-2">
            {[
              { label: "👥 Add Patient",      path: "/patients",      color: "bg-blue-600" },
              { label: "📅 Schedule Appt",    path: "/appointments",  color: "bg-green-600" },
              { label: "🩺 New Consultation", path: "/consultations", color: "bg-yellow-600" },
              { label: "💊 New Prescription", path: "/prescriptions", color: "bg-pink-600" },
              { label: "🧾 Create Bill",      path: "/billing",       color: "bg-red-600" },
              { label: "👩‍⚕️ Register Nurse",  path: "/nurses",        color: "bg-purple-600" },
            ].map(({ label, path, color }) => (
              <button key={path} onClick={() => navigate(path)}
                className={`${color} text-white w-full text-left px-3
                  py-2 rounded-lg text-sm hover:opacity-90 transition`}>
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <section className="bg-white rounded-xl shadow-sm border
        border-gray-100 p-5">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-semibold text-gray-700">🕐 Recent Activity</h3>
          <span className="text-xs text-gray-400 bg-gray-100 px-2
            py-1 rounded-full">Last 20 actions</span>
        </div>

        {loading ? <Skeleton rows={5} /> :
          !summary?.recentActivities?.length ? (
            <div className="text-center py-8">
              <p className="text-3xl mb-2">📭</p>
              <p className="text-gray-400 text-sm">No recent activity yet.</p>
            </div>
          ) : (
            <div className="space-y-2 max-h-80 overflow-y-auto">
              {summary.recentActivities.map(a => (
                <div key={a.id}
                  className="flex items-start gap-3 p-3 rounded-lg
                    hover:bg-gray-50 border border-gray-100">
                  <span className={`text-xs font-semibold px-2 py-0.5
                    rounded mt-0.5 whitespace-nowrap
                    ${ACTION_COLORS[a.action] || "bg-gray-100 text-gray-600"}`}>
                    {a.action}
                  </span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-700 truncate">
                      <strong>{a.entity}</strong>
                      {a.entityId ? ` #${a.entityId}` : ""}
                      {a.details  ? ` — ${a.details}` : ""}
                    </p>
                    <p className="text-xs text-gray-400 mt-0.5">
                      by {a.performedBy}
                    </p>
                  </div>
                  <span className="text-xs text-gray-400 whitespace-nowrap">
                    {timeAgo(a.performedAt)}
                  </span>
                </div>
              ))}
            </div>
          )}
      </section>
    </Layout>
  );
}

function StatusBar({ label, value = 0, total = 0, color }) {
  const pct = total > 0 ? Math.round((value / total) * 100) : 0;
  return (
    <div>
      <div className="flex justify-between text-sm mb-1">
        <span className="text-gray-600">{label}</span>
        <span className="font-semibold text-gray-800">
          {value}
          <span className="text-gray-400 font-normal"> ({pct}%)</span>
        </span>
      </div>
      <div className="w-full bg-gray-100 rounded-full h-2">
        <div className={`${color} h-2 rounded-full transition-all
          duration-500`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

function Skeleton({ rows = 3 }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-6 bg-gray-200 rounded animate-pulse" />
      ))}
    </div>
  );
}