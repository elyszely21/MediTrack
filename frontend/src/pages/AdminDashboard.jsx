import Navbar from '../components/Navbar';

const AdminDashboard = () => {
  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <section className="dashboard-hero">
          <div>
            <p className="eyebrow">Admin overview</p>
            <h1>SUPER_ADMIN Dashboard</h1>
            <p>Manage nurses, patients, appointments, and medical records.</p>
          </div>
          <div className="hero-badge">Admin access</div>
        </section>

        <section className="dashboard-grid">
          <article className="dashboard-card">
            <h3>Total Patients</h3>
            <p>View your complete patient list and records.</p>
          </article>
          <article className="dashboard-card">
            <h3>Total Nurses</h3>
            <p>Manage nurse accounts and clinic staff access.</p>
          </article>
          <article className="dashboard-card">
            <h3>Today&apos;s Appointments</h3>
            <p>Monitor appointment schedules and statuses.</p>
          </article>
          <article className="dashboard-card">
            <h3>Recent Activities</h3>
            <p>Track the most recent clinic actions and updates.</p>
          </article>
        </section>
      </main>
    </div>
  );
};

export default AdminDashboard;
