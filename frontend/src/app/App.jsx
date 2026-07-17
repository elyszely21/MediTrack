import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/login/Login';
import Register from '../pages/register/Register';
import AdminDashboard from '../pages/dashboard/AdminDashboard';
import NurseDashboard from '../pages/dashboard/NurseDashboard';
import DoctorDashboard from '../pages/dashboard/DoctorDashboard';
import PatientDashboard from '../pages/dashboard/PatientDashboard';
import Patients from '../pages/patients/Patients';
import MedicalRecords from '../pages/medical-records/MedicalRecords';
import Appointments from '../pages/appointments/Appointments';
import Reports from '../pages/reports/Reports';
import Nurses from '../pages/nurses/Nurses';
import Doctors from '../pages/doctors/Doctors';
import RegisterNurse from '../pages/register-nurse/RegisterNurse';
import ProtectedRoute from '../widgets/route-guards/ProtectedRoute';
import StaffRoute from '../widgets/route-guards/StaffRoute';
import './App.css';
import Prescriptions from '../pages/prescriptions/Prescriptions';
import Billing from '../pages/billing/Billing';
import Consultations from "../pages/consultations/Consultations";
import Unauthorized from "../pages/unauthorized/Unauthorized";
import Profile from "../pages/profile/Profile";
import PatientBilling from "../pages/billing/PatientBilling";

const roleAllows = (requiredRoles, role) => {
  if (!requiredRoles || requiredRoles.length === 0) return true;
  return requiredRoles.includes(role);
};


const RoleGate = ({ allowed, children }) => {
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;
  const role = user?.role;

  if (!roleAllows(allowed, role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route element={<ProtectedRoute />}>
          {/* Reachable by every logged-in role */}
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/nurse/dashboard" element={<NurseDashboard />} />
          <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
          <Route path="/patient/dashboard" element={
            <RoleGate allowed={["PATIENT"]}>
              <PatientDashboard />
            </RoleGate>
          } />
          {/* Patient-only profile (portal) */}
          <Route path="/patient/profile" element={
            <RoleGate allowed={["PATIENT"]}>
              <Profile />
            </RoleGate>
          } />
          <Route path="/patient/billing" element={
            <RoleGate allowed={["PATIENT"]}>
              <PatientBilling />
            </RoleGate>
          } />






          {/* Staff-only: PATIENT accounts get bounced back to their dashboard */}
          <Route element={<StaffRoute />}>

            <Route
              path="/patients"
              element={
                <RoleGate allowed={["SUPER_ADMIN","DOCTOR","NURSE"]}>
                  <Patients />
                </RoleGate>
              }
            />

            {/* Staff appointments only (PATIENT portal routes are handled under /patient/*) */}
            <Route
              path="/appointments"
              element={
                <RoleGate allowed={["SUPER_ADMIN","NURSE","DOCTOR"]}>
                  <Appointments />
                </RoleGate>
              }
            />



            {/* Clinical modules hidden by role */}
            <Route
              path="/records"
              element={
                <RoleGate allowed={["DOCTOR"]}>
                  <MedicalRecords />
                </RoleGate>
              }
            />
            <Route
              path="/consultations"
              element={
                <RoleGate allowed={["DOCTOR"]}>
                  <Consultations />
                </RoleGate>
              }
            />
            <Route
              path="/prescriptions"
              element={
                <RoleGate allowed={["DOCTOR"]}>
                  <Prescriptions />
                </RoleGate>
              }
            />

            {/* Billing & Reports/admin pages */}
            <Route
              path="/billing"
              element={
                <RoleGate allowed={["SUPER_ADMIN"]}>
                  <Billing />
                </RoleGate>
              }
            />
            <Route
              path="/reports"
              element={
                <RoleGate allowed={["SUPER_ADMIN"]}>
                  <Reports />
                </RoleGate>
              }
            />


            {/* User management */}
            <Route
              path="/nurses"
              element={
                <RoleGate allowed={["SUPER_ADMIN"]}>
                  <Nurses />
                </RoleGate>
              }
            />
            <Route
              path="/doctors"
              element={
                <RoleGate allowed={["SUPER_ADMIN"]}>
                  <Doctors />
                </RoleGate>
              }
            />
            <Route
              path="/admin/register-nurse"
              element={
                <RoleGate allowed={["SUPER_ADMIN"]}>
                  <RegisterNurse />
                </RoleGate>
              }
            />
          </Route>

          {/* Patient self-service routes would be added under patient dashboard pages */}
        </Route>
      </Routes>
    </Router>
  );
}


export default App;