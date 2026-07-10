import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Input from '../components/Input';
import Button from '../components/Button';
import api from '../api/axios';

const emptyForm = { diagnosis: '', treatment: '', prescription: '', notes: '', visitDate: '' };

const MedicalRecords = () => {
  const [searchParams] = useSearchParams();
  const [patients, setPatients] = useState([]);
  const [patientId, setPatientId] = useState(searchParams.get('patientId') || '');
  const [records, setRecords] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    api.get('/patients').then((res) => setPatients(res.data)).catch(() => {});
  }, []);

  const loadRecords = async (id) => {
    if (!id) { setRecords([]); return; }
    try {
      const res = await api.get(`/records/patient/${id}`);
      setRecords(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load records');
    }
  };

  useEffect(() => { loadRecords(patientId); }, [patientId]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!patientId) { setError('Select a patient first'); return; }
    try {
      await api.post('/records', { ...form, patientId: Number(patientId) });
      setForm(emptyForm);
      setShowForm(false);
      loadRecords(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create record');
    }
  };

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <h1>Medical Records</h1>

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
            <Button label={showForm ? 'Cancel' : 'Add Record'} onClick={() => setShowForm(!showForm)} />

            {showForm && (
              <form onSubmit={handleSubmit} style={{ margin: '16px 0' }}>
                <Input label="Visit Date" type="date" name="visitDate" value={form.visitDate} onChange={handleChange} />
                <Input label="Diagnosis" name="diagnosis" value={form.diagnosis} onChange={handleChange} />
                <Input label="Treatment" name="treatment" value={form.treatment} onChange={handleChange} />
                <Input label="Prescription" name="prescription" value={form.prescription} onChange={handleChange} />
                <Input label="Notes" name="notes" value={form.notes} onChange={handleChange} />
                <Button label="Save Record" type="submit" />
              </form>
            )}

            <table className="data-table">
              <thead>
                <tr><th>Date</th><th>Diagnosis</th><th>Treatment</th><th>Prescription</th><th>Notes</th></tr>
              </thead>
              <tbody>
                {records.map((r) => (
                  <tr key={r.id}>
                    <td>{r.visitDate}</td><td>{r.diagnosis}</td><td>{r.treatment}</td><td>{r.prescription}</td><td>{r.notes}</td>
                  </tr>
                ))}
                {records.length === 0 && <tr><td colSpan="5">No records yet.</td></tr>}
              </tbody>
            </table>
          </>
        )}
      </main>
    </div>
  );
};

export default MedicalRecords;