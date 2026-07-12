import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import NurseDashboard from './pages/NurseDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import PatientDashboard from './pages/PatientDashboard';
import Patients from './pages/Patients';
import MedicalRecords from './pages/MedicalRecords';
import Appointments from './pages/Appointments';
import Reports from './pages/Reports';
import Nurses from './pages/Nurses';
import Doctors from './pages/Doctors';
import RegisterNurse from './pages/RegisterNurse';
import ProtectedRoute from './components/ProtectedRoute';
import StaffRoute from './components/StaffRoute';
import './App.css';
import Prescriptions from './pages/Prescriptions';
import Billing from './pages/Billing';
import Consultations from "./pages/Consultations";

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