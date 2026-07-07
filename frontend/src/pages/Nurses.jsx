import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';

const Nurses = () => {
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;
  const isAdmin = user?.role === 'SUPER_ADMIN';

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1>Nurses</h1>
          {isAdmin && (
            <Link to="/admin/register-nurse" style={{ padding: '10px 20px', backgroundColor: '#007bff', color: 'white', borderRadius: '4px', textDecoration: 'none' }}>
              Register New Nurse
            </Link>
          )}
        </div>
        <p>Manage nurse user accounts here.</p>
      </main>
    </div>
  );
};

export default Nurses;
