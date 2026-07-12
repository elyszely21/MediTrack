import { Link, NavLink, useNavigate } from 'react-router-dom';

const dashboardPathFor = (role) => {
  if (role === 'SUPER_ADMIN') return '/admin/dashboard';
  if (role === 'DOCTOR') return '/doctor/dashboard';
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
        <Link to="/patients">Patients</Link>
        <Link to="/records">Medical Records</Link>
        <NavLink to="/prescriptions">Prescriptions</NavLink>
        <Link to="/appointments">Appointments</Link>
        <NavLink to="/billing">Billing</NavLink>
        <NavLink to="/consultations">Consultations</NavLink>
        {role === 'SUPER_ADMIN' && <Link to="/nurses">Nurses</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/doctors">Doctors</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/reports">Reports</Link>}
        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
};

export default Navbar;