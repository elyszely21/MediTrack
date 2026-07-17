import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../../widgets/navbar/Navbar";
import axios from "../../shared/api/axios";

export default function DoctorDashboard() {
  const user = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    todayAppointments: 0,
    pendingAppointments: 0,
    upcomingAppointments: 0,
    totalPatients: null, // unknown unless we can compute from existing data
    totalAppointments: 0,
    medicalRecords: null,
    consultations: null,
    prescriptions: null,
  });

  useEffect(() => {
    setLoading(true);
    axios
      .get("/appointments/doctor/dashboard")
      .then((res) => {
        const data = res.data || {};
        setStats({
          todayAppointments: data.todayAppointments || 0,
          pendingAppointments: data.pendingAppointments || 0,
          upcomingAppointments: data.upcomingAppointments || 0,
          totalAppointments: data.totalAppointments || 0,
          totalPatients: data.totalAssignedPatients || 0,
          medicalRecords: null,
          consultations: null,
          prescriptions: null,
        });
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const NAV_CARDS = [
    { label: "Patients", subtitle: "View patient records", path: "/patients", icon: "👥", color: "border-blue-400" },
    { label: "Consultations", subtitle: "Record patient consultations", path: "/consultations", icon: "🩺", color: "border-green-400" },
    { label: "Prescriptions", subtitle: "Manage prescriptions", path: "/prescriptions", icon: "💊", color: "border-pink-400" },
    { label: "Appointments", subtitle: "View appointment schedule", path: "/appointments", icon: "📅", color: "border-yellow-400" },
    { label: "Medical Records", subtitle: "Access clinic visit records", path: "/records", icon: "📋", color: "border-purple-400" },
  ];

  const statCards = useMemo(() => {
    const base = [
      { label: "Today's Appointments", key: "todayAppointments", icon: "📅", color: "bg-green-500" },
      { label: "Pending Appointments", key: "pendingAppointments", icon: "⏳", color: "bg-yellow-500" },
      { label: "Upcoming Appointments", key: "upcomingAppointments", icon: "🗓️", color: "bg-purple-500" },
      { label: "Assigned Patients", key: "totalPatients", icon: "👥", color: "bg-blue-500" },
    ];

    return base;
  }, [stats]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-5xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="mb-8">
          <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">Doctor Dashboard</p>
          <h1 className="text-3xl font-bold text-gray-800">Welcome, Dr. {user.fullName || "Doctor"}</h1>
          <p className="text-gray-500 mt-1">
            {new Date().toLocaleDateString("en-PH", {
              weekday: "long",
              year: "numeric",
              month: "long",
              day: "numeric",
            })}
          </p>
        </div>

        {/* Stat cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8" aria-busy={loading}>
          {statCards.map(({ label, key, icon, color }) => (
            <div key={key}
              className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
              <div className="flex justify-between items-start mb-2">
                <span className="text-2xl">{icon}</span>
                <span className={`${color} text-white text-xs px-2 py-0.5 rounded-full`}>live</span>
              </div>
              <p className="text-3xl font-bold text-gray-800">
                {loading ? (
                  <span className="inline-block w-8 h-7 bg-gray-200 animate-pulse rounded" />
                ) : (
                  stats?.[key] ?? "—"
                )}
              </p>
              <p className="text-sm text-gray-500 mt-1">{label}</p>
            </div>
          ))}
        </div>

        {/* Navigation cards */}
        <h2 className="text-lg font-semibold text-gray-700 mb-4">Quick Access</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {NAV_CARDS.map(({ label, subtitle, path, icon, color }) => (
            <button
              key={path}
              onClick={() => navigate(path)}
              aria-label={`Go to ${label}`}
              className={`bg-white border-l-4 ${color} rounded-xl p-4 shadow-sm text-left hover:shadow-md transition-shadow flex justify-between items-center focus:outline-none focus:ring-2 focus:ring-blue-300`}
            >
              <div className="flex items-center gap-3">
                <span className="text-2xl">{icon}</span>
                <div>
                  <p className="font-semibold text-gray-800">{label}</p>
                  <p className="text-sm text-gray-500">{subtitle}</p>
                </div>
              </div>
              <span className="text-gray-400" aria-hidden="true">›</span>
            </button>
          ))}
        </div>
      </main>
    </div>
  );
}

