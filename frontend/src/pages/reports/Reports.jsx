import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

const STAT_CARDS = [
  { key: "totalPatients",       label: "Total Patients",      color: "bg-blue-500",   icon: "👥" },
  { key: "totalDoctors",        label: "Total Doctors",       color: "bg-green-500",  icon: "👨‍⚕️" },
  { key: "totalNurses",         label: "Total Nurses",        color: "bg-purple-500", icon: "👩‍⚕️" },
  { key: "totalAppointments",   label: "Total Appointments",  color: "bg-indigo-500", icon: "📅" },
];

const ACTION_COLORS = {
  CREATED:    "bg-green-100 text-green-700",
  UPDATED:    "bg-blue-100 text-blue-700",
  DELETED:    "bg-red-100 text-red-700",
  PAYMENT:    "bg-yellow-100 text-yellow-700",
  APPROVED:   "bg-blue-100 text-blue-700",
  REJECTED:   "bg-red-100 text-red-700",
  COMPLETED:  "bg-green-100 text-green-700",
  CANCELLED:  "bg-gray-100 text-gray-600",
  ARCHIVED:   "bg-orange-100 text-orange-700",
  UNARCHIVED: "bg-teal-100 text-teal-700",
};

export default function Reports() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [doctorCount, setDoctorCount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");

  const loadReports = () => {
    let cancelled = false;
    setLoading(true);
    setError("");

    Promise.allSettled([
      axios.get("/reports/summary"),
      axios.get("/doctors"),
    ])
      .then(([summaryRes, doctorsRes]) => {
        if (cancelled) return;

        if (summaryRes.status === "fulfilled") {
          setSummary(summaryRes.value.data);
        } else {
          setError("Failed to load report summary.");
        }

        if (doctorsRes.status === "fulfilled") {
          const list = Array.isArray(doctorsRes.value.data)
            ? doctorsRes.value.data
            : (doctorsRes.value.data?.content || []);
          setDoctorCount(list.length);
        }
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  };

  useEffect(() => loadReports(), []);

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

  const noData = !loading && !error && !summary;

  return (
    <Layout>
      {/* Header */}
      <div className="flex justify-between items-start mb-8">
        <div>
          <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">
            Admin
          </p>
          <h1 className="text-3xl font-bold text-gray-800">Reports &amp; Analytics</h1>
          <p className="text-gray-500 mt-1">
            Clinic-wide overview and recent activity
          </p>
        </div>
        <button onClick={() => navigate("/admin/dashboard")}
          className="border border-gray-300 text-gray-600 px-4 py-2
            rounded-lg text-sm hover:bg-gray-100">
          ← Back to Dashboard
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600
          rounded-lg p-4 mb-6 flex justify-between items-center">
          <span>{error}</span>
          <button onClick={loadReports}
            className="text-sm underline">Retry</button>
        </div>
      )}

      {noData && (
        <div className="bg-white border border-gray-200 rounded-xl p-10
          text-center shadow-sm">
          <p className="text-4xl mb-3">📊</p>
          <p className="text-gray-400">No data available.</p>
        </div>
      )}

      {/* Stat cards */}
      <section className="mb-8">
        <h2 className="text-base font-semibold text-gray-600 mb-4 uppercase
          tracking-wide">Clinic Overview</h2>
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
                ) : key === "totalDoctors" ? (
                  (doctorCount ?? 0)
                ) : (
                  summary?.[key] ?? "—"
                )}
              </p>
              <p className="text-sm text-gray-500 mt-1">{label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Breakdown row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">

        {/* Appointment status summary */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4 flex
            items-center gap-2">
            📅 Appointment Status
          </h3>
          {loading ? <Skeleton rows={5} /> : !summary ? (
            <p className="text-gray-400 text-sm">No data available.</p>
          ) : (
            <div className="space-y-3">
              <StatusBar label="Requested / Pending"
                value={summary.scheduledAppointments}
                total={summary.totalAppointments}
                color="bg-yellow-400" />
              <StatusBar label="Approved"
                value={summary.approvedAppointments}
                total={summary.totalAppointments}
                color="bg-blue-500" />
              <StatusBar label="Completed"
                value={summary.completedAppointments}
                total={summary.totalAppointments}
                color="bg-green-500" />
              <StatusBar label="Cancelled"
                value={summary.cancelledAppointments}
                total={summary.totalAppointments}
                color="bg-red-400" />
              <StatusBar label="Today"
                value={summary.todayAppointments}
                total={summary.totalAppointments}
                color="bg-indigo-500" />
            </div>
          )}
        </div>

        {/* Billing status */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4">
            🧾 Billing Status
          </h3>
          {loading ? <Skeleton rows={3} /> : !summary ? (
            <p className="text-gray-400 text-sm">No data available.</p>
          ) : (
            <div className="space-y-3">
              <StatusBar label="Unpaid"
                value={summary.unpaidBills}
                total={summary.totalBills}
                color="bg-red-500" />
              <StatusBar label="Partial"
                value={summary.partialBills}
                total={summary.totalBills}
                color="bg-yellow-500" />
              <StatusBar label="Paid"
                value={summary.paidBills}
                total={summary.totalBills}
                color="bg-green-500" />
            </div>
          )}
        </div>

        {/* Clinical volume */}
        <div className="bg-white rounded-xl shadow-sm border
          border-gray-100 p-5">
          <h3 className="font-semibold text-gray-700 mb-4">
            🩺 Clinical Volume
          </h3>
          {loading ? <Skeleton rows={4} /> : !summary ? (
            <p className="text-gray-400 text-sm">No data available.</p>
          ) : (
            <div className="space-y-3">
              <StatusBar label="Consultations"
                value={summary.totalConsultations}
                total={summary.totalConsultations}
                color="bg-cyan-500" />
              <StatusBar label="Prescriptions"
                value={summary.totalPrescriptions}
                total={summary.totalPrescriptions}
                color="bg-pink-500" />
              <StatusBar label="Medical Records"
                value={summary.totalMedicalRecords}
                total={summary.totalMedicalRecords}
                color="bg-teal-500" />
              <StatusBar label="Bills"
                value={summary.totalBills}
                total={summary.totalBills}
                color="bg-red-500" />
            </div>
          )}
        </div>
      </div>

      {/* Recent activity */}
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
  const v = value ?? 0;
  const t = total ?? 0;
  const pct = t > 0 ? Math.round((v / t) * 100) : 0;
  return (
    <div>
      <div className="flex justify-between text-sm mb-1">
        <span className="text-gray-600">{label}</span>
        <span className="font-semibold text-gray-800">
          {v}
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
