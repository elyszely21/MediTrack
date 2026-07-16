import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../../widgets/navbar/Navbar";
import axios from "../../shared/api/axios";

export default function DoctorDashboard() {
  const user     = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    todayAppointments: 0,
    pendingAppointments: 0,
    upcomingAppointments: 0,
    totalPatients: 0,
    totalAppointments: 0,
  });

  useEffect(() => {
    const todayStr = new Date().toISOString().split("T")[0];

    setLoading(true);
    axios
      .get("/appointments")
      .then((res) => {
        const list = Array.isArray(res.data)
          ? res.data
          : (res.data?.content || []);

        const todayAppointments = list.filter((a) => a.appointmentDate === todayStr);
        const pendingAppointments = list.filter((a) =>
          ["REQUESTED", "PENDING_APPROVAL"].includes(a.status)
        );
        const upcomingAppointments = list.filter((a) => {
          const d = a.appointmentDate;
          if (!d) return false;
          return String(d) >= todayStr && !["COMPLETED", "CANCELLED", "REJECTED"].includes(a.status);
        });

        setStats({
          todayAppointments: todayAppointments.length,
          pendingAppointments: pendingAppointments.length,
          upcomingAppointments: upcomingAppointments.length,
          totalPatients: 0,
          totalAppointments: list.length,
        });
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);



  const NAV_CARDS = [
    { label: "Patients",       subtitle: "View patient records",          path: "/patients",      icon: "👥", color: "border-blue-400" },
    { label: "Consultations",  subtitle: "Record patient consultations",   path: "/consultations", icon: "🩺", color: "border-green-400" },
    { label: "Prescriptions",  subtitle: "Manage prescriptions",          path: "/prescriptions", icon: "💊", color: "border-pink-400" },
    { label: "Appointments",   subtitle: "View appointment schedule",      path: "/appointments",  icon: "📅", color: "border-yellow-400" },
    { label: "Medical Records","subtitle": "Access clinic visit records",  path: "/records",       icon: "📋", color: "border-purple-400" },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-5xl mx-auto px-6 py-8">

        {/* Header */}
        <div className="mb-8">
          <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">
            Doctor Dashboard
          </p>
          <h1 className="text-3xl font-bold text-gray-800">
            Welcome, Dr. {user.fullName || "Doctor"}
          </h1>
          <p className="text-gray-500 mt-1">
            {new Date().toLocaleDateString("en-PH", {
              weekday: "long", year: "numeric",
              month: "long", day: "numeric"
            })}
          </p>
        </div>

        {/* Stat cards */}
        <div className="grid grid-cols-3 gap-4 mb-8">
          {[
            { label: "Total Patients",      key: "totalPatients",     icon: "👥", color: "bg-blue-500" },
            { label: "Today's Appointments",key: "todayAppointments", icon: "📅", color: "bg-green-500" },
            { label: "All Appointments",    key: "totalAppointments", icon: "🗓️", color: "bg-purple-500" },
          ].map(({ label, key, icon, color }) => (
            <div key={key}
              className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
              <div className="flex justify-between items-start mb-2">
                <span className="text-2xl">{icon}</span>
                <span className={`${color} text-white text-xs
                  px-2 py-0.5 rounded-full`}>live</span>
              </div>
              <p className="text-3xl font-bold text-gray-800">
                {loading ? (
                  <span className="inline-block w-8 h-7
                    bg-gray-200 animate-pulse rounded" />
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
            <button key={path}
              onClick={() => navigate(path)}
              className={`bg-white border-l-4 ${color} rounded-xl p-4
                shadow-sm text-left hover:shadow-md transition-shadow
                flex justify-between items-center`}>
              <div className="flex items-center gap-3">
                <span className="text-2xl">{icon}</span>
                <div>
                  <p className="font-semibold text-gray-800">{label}</p>
                  <p className="text-sm text-gray-500">{subtitle}</p>
                </div>
              </div>
              <span className="text-gray-400">›</span>
            </button>
          ))}
        </div>
      </main>
    </div>
  );
}