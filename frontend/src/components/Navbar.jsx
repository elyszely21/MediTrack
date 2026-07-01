import { Link, useNavigate } from 'react-router-dom';

const Navbar = ({ userName }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('meditrackUser');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="brand">MediTrack</div>
      <div className="nav-links">
        <Link to="/dashboard">Dashboard</Link>
        {userName ? (
          <button className="logout-button" onClick={handleLogout}>Logout</button>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
