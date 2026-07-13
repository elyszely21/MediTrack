import Navbar from '../../widgets/navbar/Navbar';

const NurseDashboard = () => {
  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <section className="dashboard-hero">
          <div>
            <p className="eyebrow">Nurse overview</p>
            <h1>NURSE Dashboard</h1>
            <p>Register patients, create records, and manage appointments.</p>
          </div>
          <div className="hero-badge">Nurse access</div>
        </section>

        <section className="dashboard-grid">
          <article className="dashboard-card">
            <h3>Today&apos;s Patients</h3>
            <p>See patients scheduled for care today.</p>
          </article>
          <article className="dashboard-card">
            <h3>Today&apos;s Appointments</h3>
            <p>Manage and update appointment status.</p>
          </article>
          <article className="dashboard-card">
            <h3>Recent Records</h3>
            <p>Access newly created patient medical records.</p>
          </article>
          <article className="dashboard-card">
            <h3>Patient Registry</h3>
            <p>Register and update patient information.</p>
          </article>
        </section>
      </main>
    </div>
  );
};

export default NurseDashboard;
