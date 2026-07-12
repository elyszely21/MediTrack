import Navbar from '../components/Navbar';

const PatientDashboard = () => {
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <h1>Welcome, {user?.fullName}</h1>
        <p>
          Your patient portal is being built — soon you'll be able to view your
          appointments, prescriptions, and medical history here.
        </p>
        <p>
          In the meantime, please contact the clinic directly for appointment
          bookings or questions about your records.
        </p>
      </main>
    </div>
  );
};

export default PatientDashboard;