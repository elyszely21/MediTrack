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

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route element={<ProtectedRoute />}>
          {/* Reachable by every logged-in role */}
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/nurse/dashboard" element={<NurseDashboard />} />
          <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
          <Route path="/patient/dashboard" element={<PatientDashboard />} />

          {/* Staff-only: PATIENT accounts get bounced back to their dashboard */}
          <Route element={<StaffRoute />}>
            <Route path="/patients" element={<Patients />} />
            <Route path="/records" element={<MedicalRecords />} />
            <Route path="/appointments" element={<Appointments />} />
            <Route path="/billing" element={<Billing />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/nurses" element={<Nurses />} />
            <Route path="/doctors" element={<Doctors />} />
            <Route path="/admin/register-nurse" element={<RegisterNurse />} />
            <Route path="/prescriptions" element={<Prescriptions />} />
            <Route path="/consultations" element={<Consultations />} />
          </Route>
        </Route>
      </Routes>
    </Router>
  );
}

export default App;