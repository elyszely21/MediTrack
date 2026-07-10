import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import Input from '../components/Input';
import Button from '../components/Button';
import api from '../api/axios';

const emptyForm = { patientId: '', appointmentDate: '', appointmentTime: '', status: 'PENDING', remarks: '' };

const Appointments = () => {
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);

  const loadData = async () => {
    try {
      const [apptRes, patientRes] = await Promise.all([
        api.get('/appointments'),
        api.get('/patients'),
      ]);
      setAppointments(apptRes.data);
      setPatients(patientRes.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load appointments');
    }
  };

  useEffect(() => { loadData(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/appointments', { ...form, patientId: Number(form.patientId) });
      setForm(emptyForm);
      setShowForm(false);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create appointment');
    }
  };

  const updateStatus = async (appt, status) => {
    try {
      await api.put(`/appointments/${appt.id}`, { ...appt, status });
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update appointment');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Cancel and delete this appointment?')) return;
    try {
      await api.delete(`/appointments/${id}`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete appointment');
    }
  };

  const patientName = (id) => {
    const p = patients.find((pt) => pt.id === id);
    return p ? `${p.firstName} ${p.lastName}` : id;
  };

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h1>Appointments</h1>
          <Button label={showForm ? 'Cancel' : 'Schedule Appointment'} onClick={() => setShowForm(!showForm)} />
        </div>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        {showForm && (
          <form onSubmit={handleSubmit} style={{ marginBottom: 24 }}>
            <div className="form-group">
              <label htmlFor="patientId">Patient</label>
              <select id="patientId" name="patientId" value={form.patientId} onChange={handleChange} className="input-field" required>
                <option value="">Select patient</option>
                {patients.map((p) => (
                  <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>
                ))}
              </select>
            </div>
            <Input label="Date" type="date" name="appointmentDate" value={form.appointmentDate} onChange={handleChange} />
            <Input label="Time" type="time" name="appointmentTime" value={form.appointmentTime} onChange={handleChange} />
            <Input label="Remarks" name="remarks" value={form.remarks} onChange={handleChange} />
            <Button label="Save Appointment" type="submit" />
          </form>
        )}

        <table className="data-table">
          <thead>
            <tr><th>Patient</th><th>Date</th><th>Time</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{patientName(a.patientId)}</td>
                <td>{a.appointmentDate}</td>
                <td>{a.appointmentTime}</td>
                <td>{a.status}</td>
                <td>
                  {a.status !== 'COMPLETED' && (
                    <button className="action-button" onClick={() => updateStatus(a, 'COMPLETED')}>Complete</button>
                  )}{' '}
                  <button className="action-button" onClick={() => handleDelete(a.id)}>Delete</button>
                </td>
              </tr>
            ))}
            {appointments.length === 0 && <tr><td colSpan="5">No appointments yet.</td></tr>}
          </tbody>
        </table>
      </main>
    </div>
  );
};

export default Appointments;