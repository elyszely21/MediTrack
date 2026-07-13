import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../widgets/navbar/Navbar';

const Dashboard = () => {
  const navigate = useNavigate();
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;

  useEffect(() => {
    if (!storedUser) {
      navigate('/login');
    }
  }, [navigate, storedUser]);

  if (!user) {
    return null;
  }

  return (
    <div className="dashboard-page">
      <Navbar userName={user.fullName} />
      <main className="dashboard-content">
        <section className="dashboard-hero">
          <div>
            <p className="eyebrow">Care coordination</p>
            <h1>Welcome, {user.fullName}</h1>
            <p>Your medicine inventory and medication reminders are ready to review.</p>
          </div>
          <div className="hero-badge">Secure access enabled</div>
        </section>

        <section className="dashboard-grid">
          <article className="dashboard-card">
            <h3>Medicine Inventory</h3>
            <p>Track current stock and restock needs in one place.</p>
          </article>
          <article className="dashboard-card">
            <h3>Medication Schedule</h3>
            <p>View upcoming doses and treatment plans.</p>
          </article>
          <article className="dashboard-card">
            <h3>Expiration Alerts</h3>
            <p>Receive reminders for medicines nearing expiration.</p>
          </article>
          <article className="dashboard-card">
            <h3>Profile</h3>
            <p>{user.email}</p>
          </article>
        </section>
      </main>
    </div>
  );
};

export default Dashboard;
