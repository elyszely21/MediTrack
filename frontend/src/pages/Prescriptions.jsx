import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import Input from '../components/Input';
import Button from '../components/Button';
import api from '../api/axios';

const emptyForm = {
  medication: '', dosage: '', frequency: '', duration: '',
  instructions: '', prescribedBy: '', prescribedDate: '',
};

const Prescriptions = () => {
  const [patients, setPatients] = useState([]);
  const [patientId, setPatientId] = useState('');
  const [prescriptions, setPrescriptions] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    api.get('/patients').then((res) => setPatients(res.data)).catch(() => {});
  }, []);

  const loadPrescriptions = async (id) => {
    if (!id) { setPrescriptions([]); return; }
    try {
      const res = await api.get(`/prescriptions/patient/${id}`);
      setPrescriptions(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load prescriptions');
    }
  };

  useEffect(() => { loadPrescriptions(patientId); }, [patientId]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!patientId) { setError('Select a patient first'); return; }
    try {
      await api.post('/prescriptions', { ...form, patientId: Number(patientId) });
      setForm(emptyForm);
      setShowForm(false);
      loadPrescriptions(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create prescription');
    }
  };

  const updateStatus = async (p, status) => {
    try {
      await api.put(`/prescriptions/${p.id}`, { ...p, status });
      loadPrescriptions(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update prescription');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this prescription?')) return;
    try {
      await api.delete(`/prescriptions/${id}`);
      loadPrescriptions(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete prescription');
    }
  };

  const handlePrint = (p) => {
    const patient = patients.find((pt) => pt.id === Number(patientId));
    const win = window.open('', '_blank');
    win.document.write(`
      <html><head><title>Prescription</title>
      <style>body{font-family:sans-serif;padding:40px;} h2{margin-bottom:0;} .line{margin:8px 0;}</style>
      </head><body>
      <h2>MediTrack Clinic</h2>
      <p>Prescription</p>
      <hr/>
      <div class="line"><b>Patient:</b> ${patient ? patient.firstName + ' ' + patient.lastName : ''}</div>
      <div class="line"><b>Date:</b> ${p.prescribedDate}</div>
      <div class="line"><b>Medication:</b> ${p.medication}</div>
      <div class="line"><b>Dosage:</b> ${p.dosage || '-'}</div>
      <div class="line"><b>Frequency:</b> ${p.frequency || '-'}</div>
      <div class="line"><b>Duration:</b> ${p.duration || '-'}</div>
      <div class="line"><b>Instructions:</b> ${p.instructions || '-'}</div>
      <br/><br/>
      <div class="line">Prescribed by: ${p.prescribedBy}</div>
      </body></html>
    `);
    win.document.close();
    win.print();
  };

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <h1>Prescriptions</h1>

        <div className="form-group">
          <label htmlFor="patientSelect">Patient</label>
          <select id="patientSelect" className="input-field" value={patientId} onChange={(e) => setPatientId(e.target.value)}>
            <option value="">Select patient</option>
            {patients.map((p) => (
              <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>
            ))}
          </select>
        </div>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        {patientId && (
          <>
            <Button label={showForm ? 'Cancel' : 'New Prescription'} onClick={() => setShowForm(!showForm)} />

            {showForm && (
              <form onSubmit={handleSubmit} style={{ margin: '16px 0' }}>
                <Input label="Medication" name="medication" value={form.medication} onChange={handleChange} />
                <Input label="Dosage" name="dosage" value={form.dosage} onChange={handleChange} placeholder="e.g. 500mg" />
                <Input label="Frequency" name="frequency" value={form.frequency} onChange={handleChange} placeholder="e.g. 3x daily" />
                <Input label="Duration" name="duration" value={form.duration} onChange={handleChange} placeholder="e.g. 7 days" />
                <Input label="Instructions" name="instructions" value={form.instructions} onChange={handleChange} />
                <Input label="Prescribed By" name="prescribedBy" value={form.prescribedBy} onChange={handleChange} />
                <Input label="Date" type="date" name="prescribedDate" value={form.prescribedDate} onChange={handleChange} />
                <Button label="Save Prescription" type="submit" />
              </form>
            )}

            <table className="data-table">
              <thead>
                <tr><th>Date</th><th>Medication</th><th>Dosage</th><th>Freq</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {prescriptions.map((p) => (
                  <tr key={p.id}>
                    <td>{p.prescribedDate}</td>
                    <td>{p.medication}</td>
                    <td>{p.dosage}</td>
                    <td>{p.frequency}</td>
                    <td>{p.status}</td>
                    <td>
                      {p.status === 'ACTIVE' && (
                        <button className="action-button" onClick={() => updateStatus(p, 'COMPLETED')}>Complete</button>
                      )}{' '}
                      <button className="action-button" onClick={() => handlePrint(p)}>Print</button>{' '}
                      <button className="action-button" onClick={() => handleDelete(p.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
                {prescriptions.length === 0 && <tr><td colSpan="6">No prescriptions yet.</td></tr>}
              </tbody>
            </table>
          </>
        )}
      </main>
    </div>
  );
};

export default Prescriptions;