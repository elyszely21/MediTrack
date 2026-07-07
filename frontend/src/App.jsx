import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import AdminDashboard from './pages/AdminDashboard';
import NurseDashboard from './pages/NurseDashboard';
import Patients from './pages/Patients';
import MedicalRecords from './pages/MedicalRecords';
import Appointments from './pages/Appointments';
import Reports from './pages/Reports';
import Nurses from './pages/Nurses';
import RegisterNurse from './pages/RegisterNurse';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/nurse/dashboard" element={<NurseDashboard />} />
          <Route path="/patients" element={<Patients />} />
          <Route path="/records" element={<MedicalRecords />} />
          <Route path="/appointments" element={<Appointments />} />
          <Route path="/reports" element={<Reports />} />
          <Route path="/nurses" element={<Nurses />} />
          <Route path="/admin/register-nurse" element={<RegisterNurse />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
