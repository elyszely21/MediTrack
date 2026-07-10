import { Link, useNavigate } from 'react-router-dom';

const Navbar = () => {
  const navigate = useNavigate();
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;
  const role = user?.role;

  const handleLogout = () => {
    localStorage.removeItem('meditrackUser');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="brand">
        <Link to="/dashboard">MediTrack</Link>
      </div>
      <div className="nav-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/patients">Patients</Link>
        <Link to="/records">Medical Records</Link>
        <Link to="/prescriptions">Prescriptions</Link>
        <Link to="/appointments">Appointments</Link>
        <Link to="/billing">Billing</Link>
        {role === 'SUPER_ADMIN' && <Link to="/nurses">Nurses</Link>}
        {role === 'SUPER_ADMIN' && <Link to="/reports">Reports</Link>}
        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
};

export default Navbar;
