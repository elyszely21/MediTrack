import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Input from '../components/Input';
import Button from '../components/Button';
import api from '../api/axios';

const emptyForm = {
  patientNumber: '', firstName: '', lastName: '', birthDate: '',
  gender: '', address: '', contactNumber: '', emergencyContact: '',
};

const Patients = () => {
  const [patients, setPatients] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const loadPatients = async () => {
    try {
      const res = await api.get('/patients');
      setPatients(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load patients');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadPatients(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/patients', form);
      setForm(emptyForm);
      setShowForm(false);
      loadPatients();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create patient');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this patient?')) return;
    try {
      await api.delete(`/patients/${id}`);
      loadPatients();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete patient');
    }
  };

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h1>Patients</h1>
          <Button label={showForm ? 'Cancel' : 'Add Patient'} onClick={() => setShowForm(!showForm)} />
        </div>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        {showForm && (
          <form onSubmit={handleSubmit} style={{ marginBottom: 24 }}>
            <Input label="Patient Number" name="patientNumber" value={form.patientNumber} onChange={handleChange} />
            <Input label="First Name" name="firstName" value={form.firstName} onChange={handleChange} />
            <Input label="Last Name" name="lastName" value={form.lastName} onChange={handleChange} />
            <Input label="Birth Date" type="date" name="birthDate" value={form.birthDate} onChange={handleChange} />
            <Input label="Gender" name="gender" value={form.gender} onChange={handleChange} />
            <Input label="Address" name="address" value={form.address} onChange={handleChange} />
            <Input label="Contact Number" name="contactNumber" value={form.contactNumber} onChange={handleChange} />
            <Input label="Emergency Contact" name="emergencyContact" value={form.emergencyContact} onChange={handleChange} />
            <Button label="Save Patient" type="submit" />
          </form>
        )}

        {loading ? (
          <p>Loading...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Patient #</th><th>Name</th><th>Gender</th><th>Contact</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p) => (
                <tr key={p.id}>
                  <td>{p.patientNumber}</td>
                  <td>{p.firstName} {p.lastName}</td>
                  <td>{p.gender}</td>
                  <td>{p.contactNumber}</td>
                  <td>
                    <Link to={`/records?patientId=${p.id}`}>Records</Link>{' '}
                    <button className="action-button" onClick={() => handleDelete(p.id)}>Delete</button>
                  </td>
                </tr>
              ))}
              {patients.length === 0 && (
                <tr><td colSpan="5">No patients yet.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </main>
    </div>
  );
};

export default Patients;