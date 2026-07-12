import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

const NAV_ITEMS = [
  { path: "/admin/dashboard",  label: "Dashboard",       icon: "🏠", roles: ["SUPER_ADMIN"] },
  { path: "/nurse/dashboard",  label: "Dashboard",       icon: "🏠", roles: ["NURSE"] },
  { path: "/doctor/dashboard", label: "Dashboard",       icon: "🏠", roles: ["ROLE_DOCTOR"] },
  { path: "/patients",         label: "Patients",        icon: "👥", roles: ["SUPER_ADMIN","NURSE","ROLE_DOCTOR"] },
  { path: "/appointments",     label: "Appointments",    icon: "📅", roles: ["SUPER_ADMIN","NURSE","ROLE_DOCTOR"] },
  { path: "/records",          label: "Medical Records", icon: "📋", roles: ["SUPER_ADMIN","NURSE","ROLE_DOCTOR"] },
  { path: "/consultations",    label: "Consultations",   icon: "🩺", roles: ["SUPER_ADMIN","NURSE","ROLE_DOCTOR"] },
  { path: "/prescriptions",    label: "Prescriptions",   icon: "💊", roles: ["SUPER_ADMIN","NURSE","ROLE_DOCTOR"] },
  { path: "/billing",          label: "Billing",         icon: "🧾", roles: ["SUPER_ADMIN","NURSE"] },
  { path: "/nurses",           label: "Nurses",          icon: "👩‍⚕️", roles: ["SUPER_ADMIN"] },
  { path: "/doctors",          label: "Doctors",         icon: "👨‍⚕️", roles: ["SUPER_ADMIN"] },
];

export default function Layout({ children }) {
  const location = useLocation();
  const navigate  = useNavigate();
  const user      = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const role      = user?.role || "";
  const [collapsed, setCollapsed] = useState(false);

  const visibleItems = NAV_ITEMS.filter(item =>
    item.roles.includes(role)
  );

  const handleLogout = () => {
    localStorage.removeItem("meditrackUser");
    navigate("/login");
  };

  const isActive = (path) => location.pathname === path;

  const roleLabel = {
    SUPER_ADMIN: "Super Admin",
    NURSE:       "Nurse",
    ROLE_DOCTOR: "Doctor",
  }[role] || role;

  const roleColor = {
    SUPER_ADMIN: "bg-purple-100 text-purple-700",
    NURSE:       "bg-blue-100 text-blue-700",
    ROLE_DOCTOR: "bg-green-100 text-green-700",
  }[role] || "bg-gray-100 text-gray-700";

  return (
    <div className="flex min-h-screen bg-gray-50">

      {/* Sidebar */}
      <aside className={`${collapsed ? "w-16" : "w-64"} bg-white border-r
        border-gray-200 flex flex-col transition-all duration-200
        shadow-sm flex-shrink-0`}>

        {/* Logo */}
        <div className="flex items-center justify-between p-4
          border-b border-gray-100">
          {!collapsed && (
            <div>
              <h1 className="text-lg font-bold text-blue-600">MediTrack</h1>
              <p className="text-xs text-gray-400">Clinic Records System</p>
            </div>
          )}
          <button onClick={() => setCollapsed(!collapsed)}
            className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500
              ml-auto">
            {collapsed ? "→" : "←"}
          </button>
        </div>

        {/* User info */}
        {!collapsed && (
          <div className="px-4 py-3 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 bg-blue-600 rounded-full flex
                items-center justify-center text-white font-semibold text-sm
                flex-shrink-0">
                {user?.fullName?.charAt(0)?.toUpperCase() || "U"}
              </div>
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-800 truncate">
                  {user?.fullName || "User"}
                </p>
                <span className={`text-xs font-medium px-2 py-0.5
                  rounded-full ${roleColor}`}>
                  {roleLabel}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Navigation */}
        <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
          {visibleItems.map(item => (
            <Link key={item.path} to={item.path}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg
                text-sm transition-colors
                ${isActive(item.path)
                  ? "bg-blue-50 text-blue-700 font-semibold"
                  : "text-gray-600 hover:bg-gray-50 hover:text-gray-800"
                }`}>
              <span className="text-base flex-shrink-0">{item.icon}</span>
              {!collapsed && (
                <span className="truncate">{item.label}</span>
              )}
              {!collapsed && isActive(item.path) && (
                <span className="ml-auto w-1.5 h-1.5 bg-blue-600
                  rounded-full" />
              )}
            </Link>
          ))}
        </nav>

        {/* Logout */}
        <div className="p-3 border-t border-gray-100">
          <button onClick={handleLogout}
            className={`flex items-center gap-3 px-3 py-2.5 rounded-lg
              text-sm text-red-500 hover:bg-red-50 w-full transition-colors`}>
            <span className="text-base flex-shrink-0">🚪</span>
            {!collapsed && <span>Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 min-w-0 overflow-auto">
        {/* Top bar */}
        <div className="bg-white border-b border-gray-200 px-6 py-3
          flex items-center justify-between sticky top-0 z-10">
          <div>
            <p className="text-xs text-gray-400 uppercase tracking-wide">
              {roleLabel}
            </p>
            <p className="text-sm font-medium text-gray-700">
              {new Date().toLocaleDateString("en-PH", {
                weekday: "long", year: "numeric",
                month: "long", day: "numeric"
              })}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">
              {user?.fullName}
            </span>
            <div className="w-8 h-8 bg-blue-600 rounded-full flex
              items-center justify-center text-white font-semibold text-sm">
              {user?.fullName?.charAt(0)?.toUpperCase() || "U"}
            </div>
          </div>
        </div>

        {/* Page content */}
        <div className="p-6">
          {children}
        </div>
      </main>
    </div>
  );
}