import { Link, NavLink, useNavigate } from 'react-router-dom';

const dashboardPathFor = (role) => {
  if (role === 'SUPER_ADMIN') return '/admin/dashboard';
  if (role === 'DOCTOR') return '/doctor/dashboard';
  if (role === 'PATIENT') return '/patient/dashboard';
  return '/nurse/dashboard';
};

const Navbar = () => {
  const navigate = useNavigate();
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;
  const role = user?.role;
  const dashboardPath = dashboardPathFor(role);

  const handleLogout = () => {
    localStorage.removeItem('meditrackUser');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="brand">
        <Link to={dashboardPath}>MediTrack</Link>
      </div>
      <div className="nav-links">
        <Link to={dashboardPath}>Dashboard</Link>

        {/* Keep navigation strictly role-visible (no hidden links) */}
        {role === 'SUPER_ADMIN' && <Link to="/patients">Patients</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/billing">Billing</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/reports">Reports</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/nurses">Nurses</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/doctors">Doctors</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/appointments">Appointments</Link>}

        {role === 'ROLE_DOCTOR' && <Link to="/patients">Patients</Link>}
        {role === 'ROLE_DOCTOR' && <Link to="/records">Medical Records</Link>}
        {role === 'ROLE_DOCTOR' && <NavLink to="/prescriptions">Prescriptions</NavLink>}
        {role === 'ROLE_DOCTOR' && <Link to="/appointments">Appointments</Link>}
        {role === 'ROLE_DOCTOR' && <NavLink to="/consultations">Consultations</NavLink>}

        {role === 'NURSE' && <Link to="/patients">Patients</Link>}
        {role === 'NURSE' && <Link to="/appointments">Appointments</Link>}

        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
};

export default Navbar;