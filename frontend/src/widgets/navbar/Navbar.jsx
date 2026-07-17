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
  const isStaff = role === 'SUPER_ADMIN' || role === 'NURSE' || role === 'DOCTOR';

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
        {isStaff && <Link to="/patients">Patients</Link>}
        {isStaff && <Link to="/records">Medical Records</Link>}
        {isStaff && <NavLink to="/prescriptions">Prescriptions</NavLink>}
        {isStaff && <Link to="/appointments">Appointments</Link>}
        {isStaff && <NavLink to="/billing">Billing</NavLink>}
        {isStaff && <NavLink to="/consultations">Consultations</NavLink>}
        {role === 'SUPER_ADMIN' && <Link to="/nurses">Nurses</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/doctors">Doctors</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/reports">Reports</Link>}
        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
};

export default Navbar;