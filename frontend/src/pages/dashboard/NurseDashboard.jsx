import { useEffect, useMemo, useState } from "react";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

export default function NurseDashboard() {
  const user = JSON.parse(localStorage.getItem("meditrackUser") || "{}");

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    todayAppointments: 0,
    pendingAppointments: 0,
    upcomingAppointments: 0,
    registeredPatients: null,
    totalPatients: 0,
  });

  useEffect(() => {
    const todayStr = new Date().toISOString().split("T")[0];

    setLoading(true);
    axios
      .get("/appointments")
      .then((res) => {
        const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);

        const todayAppointments = list.filter((a) => a.appointmentDate === todayStr);
        const pendingAppointments = list.filter((a) =>
          ["REQUESTED", "PENDING_APPROVAL"].includes(a.status)
        );
        const upcomingAppointments = list.filter((a) => {
          const d = a.appointmentDate;
          if (!d) return false;
          return String(d) >= todayStr && !["COMPLETED", "CANCELLED", "REJECTED"].includes(a.status);
        });

        // Avoid inventing “registered patients” counts.
        // We can safely derive an assigned-patient count only if patientId exists in the appointment DTO.
        const uniquePatientIds = new Set(
          list.map((a) => a.patientId).filter((id) => id !== undefined && id !== null)
        );

        setStats({
          todayAppointments: todayAppointments.length,
          pendingAppointments: pendingAppointments.length,
          upcomingAppointments: upcomingAppointments.length,
          registeredPatients: uniquePatientIds.size > 0 ? uniquePatientIds.size : null,
          totalPatients: uniquePatientIds.size > 0 ? uniquePatientIds.size : 0,
        });
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const statCards = useMemo(() => {
    const base = [
      { label: "Today's Appointments", key: "todayAppointments", icon: "📅", color: "bg-green-500" },
      { label: "Pending Appointments", key: "pendingAppointments", icon: "⏳", color: "bg-yellow-500" },
      { label: "Upcoming Appointments", key: "upcomingAppointments", icon: "🗓️", color: "bg-purple-500" },
    ];

    // Only show registered patients if we could derive it from appointment DTO reliably.
    if (stats.registeredPatients !== null) {
      base.push({ label: "Registered Patients", key: "registeredPatients", icon: "👥", color: "bg-blue-500" });
    }

    return base;
  }, [stats.registeredPatients]);

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <main className="max-w-5xl mx-auto px-6 py-8">
          <div className="mb-8">
            <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">Nurse Dashboard</p>
            <h1 className="text-3xl font-bold text-gray-800">Welcome, {user.fullName || "Nurse"}</h1>
            <p className="text-gray-500 mt-1">
              {new Date().toLocaleDateString("en-PH", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-8" aria-busy={loading}>
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
                  ) : (stats?.[key] ?? "—")}
                </p>
                <p className="text-sm text-gray-500 mt-1">{label}</p>
              </div>
            ))}
          </div>

          <section className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-700 mb-3">Quick Actions</h2>
            <div className="text-sm text-gray-600">
              Use the sidebar to manage Patients, Appointments, Medical Records, Consultations, and Prescriptions.
            </div>
          </section>
        </main>
      </div>
    </Layout>
  );
}

