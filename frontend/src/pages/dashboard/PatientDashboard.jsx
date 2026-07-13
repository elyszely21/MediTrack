import { useEffect, useState, useCallback } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axios';

const APPOINTMENT_STATUS_COLORS = {
  PENDING:   'bg-yellow-100 text-yellow-800',
  APPROVED:  'bg-blue-100 text-blue-800',
  REJECTED:  'bg-red-100 text-red-700',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-100 text-gray-600',
};

const PRESCRIPTION_STATUS_COLORS = {
  ACTIVE:    'bg-green-100 text-green-800',
  COMPLETED: 'bg-blue-100 text-blue-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

const TABS = [
  { key: 'appointments', label: 'Appointments' },
  { key: 'prescriptions', label: 'Prescriptions' },
  { key: 'records', label: 'Medical Records' },
  { key: 'profile', label: 'My Profile' },
];

const StatusBadge = ({ status, colorMap }) => (
  <span
    className={`text-xs font-medium px-2 py-0.5 rounded-full ${
      colorMap[status] || 'bg-gray-100 text-gray-600'
    }`}
  >
    {status || '—'}
  </span>
);

const PatientDashboard = () => {
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;

  const [activeTab, setActiveTab] = useState('appointments');

  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [records, setRecords] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [profileForm, setProfileForm] = useState(null);
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileMessage, setProfileMessage] = useState('');

  const loadAll = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [profileRes, apptRes, presRes, recRes] = await Promise.all([
        api.get('/patient-portal/me'),
        api.get('/patient-portal/appointments'),
        api.get('/patient-portal/prescriptions'),
        api.get('/patient-portal/medical-records'),
      ]);
      setProfile(profileRes.data);
      setProfileForm(profileRes.data);
      setAppointments(apptRes.data);
      setPrescriptions(presRes.data);
      setRecords(recRes.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Could not load your patient portal right now.'
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  const handleProfileChange = (e) => {
    setProfileForm({ ...profileForm, [e.target.name]: e.target.value });
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    setProfileMessage('');
    try {
      const res = await api.put('/patient-portal/me', profileForm);
      setProfile(res.data);
      setProfileForm(res.data);
      setProfileMessage('Profile updated.');
      setTimeout(() => setProfileMessage(''), 3000);
    } catch (err) {
      setProfileMessage(
        err.response?.data?.message || 'Could not save your profile.'
      );
    } finally {
      setSavingProfile(false);
    }
  };

  const upcomingCount = appointments.filter((a) =>
    ['PENDING', 'APPROVED'].includes(a.status)
  ).length;
  const activePrescriptionCount = prescriptions.filter(
    (p) => p.status === 'ACTIVE'
  ).length;

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <section className="dashboard-hero">
          <div>
            <p className="eyebrow">Patient portal</p>
            <h1>Welcome, {user?.fullName}</h1>
            <p>
              {profile?.patientNumber
                ? `Patient No. ${profile.patientNumber}`
                : 'View your own appointments, prescriptions, and medical history.'}
            </p>
          </div>
          <div className="hero-badge">Patient access</div>
        </section>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg px-4 py-3 mb-4">
            {error}
          </div>
        )}

        {loading ? (
          <p>Loading your records…</p>
        ) : (
          <>
            <section className="dashboard-grid" style={{ marginBottom: 24 }}>
              <article className="dashboard-card">
                <h3>Upcoming Appointments</h3>
                <p>{upcomingCount} pending or approved</p>
              </article>
              <article className="dashboard-card">
                <h3>Active Prescriptions</h3>
                <p>{activePrescriptionCount} currently active</p>
              </article>
              <article className="dashboard-card">
                <h3>Medical Records</h3>
                <p>{records.length} visit{records.length === 1 ? '' : 's'} on file</p>
              </article>
              <article className="dashboard-card">
                <h3>Contact on File</h3>
                <p>{profile?.email}</p>
              </article>
            </section>

            <div className="bg-white rounded-xl shadow-xl" style={{ overflow: 'hidden' }}>
              <div className="flex border-b border-gray-200">
                {TABS.map((tab) => (
                  <button
                    key={tab.key}
                    onClick={() => setActiveTab(tab.key)}
                    className={`px-4 py-3 text-sm font-medium ${
                      activeTab === tab.key
                        ? 'text-blue-600 border-b-2 border-blue-600'
                        : 'text-gray-500 hover:text-gray-700'
                    }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              <div style={{ padding: 20 }}>
                {activeTab === 'appointments' && (
                  appointments.length === 0 ? (
                    <p>You have no appointments yet.</p>
                  ) : (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Date</th>
                          <th>Time</th>
                          <th>Status</th>
                          <th>Remarks</th>
                        </tr>
                      </thead>
                      <tbody>
                        {appointments.map((a) => (
                          <tr key={a.id}>
                            <td>{a.appointmentDate}</td>
                            <td>{a.appointmentTime}</td>
                            <td>
                              <StatusBadge
                                status={a.status}
                                colorMap={APPOINTMENT_STATUS_COLORS}
                              />
                            </td>
                            <td>{a.remarks || '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )
                )}

                {activeTab === 'prescriptions' && (
                  prescriptions.length === 0 ? (
                    <p>You have no prescriptions yet.</p>
                  ) : (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Medication</th>
                          <th>Dosage</th>
                          <th>Frequency</th>
                          <th>Duration</th>
                          <th>Prescribed By</th>
                          <th>Date</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {prescriptions.map((p) => (
                          <tr key={p.id}>
                            <td>{p.medication}</td>
                            <td>{p.dosage || '—'}</td>
                            <td>{p.frequency || '—'}</td>
                            <td>{p.duration || '—'}</td>
                            <td>{p.prescribedBy}</td>
                            <td>{p.prescribedDate}</td>
                            <td>
                              <StatusBadge
                                status={p.status}
                                colorMap={PRESCRIPTION_STATUS_COLORS}
                              />
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )
                )}

                {activeTab === 'records' && (
                  records.length === 0 ? (
                    <p>You have no medical records yet.</p>
                  ) : (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Visit Date</th>
                          <th>Diagnosis</th>
                          <th>Treatment</th>
                          <th>Notes</th>
                        </tr>
                      </thead>
                      <tbody>
                        {records.map((r) => (
                          <tr key={r.id}>
                            <td>{r.visitDate}</td>
                            <td>{r.diagnosis || '—'}</td>
                            <td>{r.treatment || '—'}</td>
                            <td>{r.notes || '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )
                )}

                {activeTab === 'profile' && profileForm && (
                  <form
                    onSubmit={handleProfileSave}
                    style={{ display: 'flex', flexDirection: 'column', gap: 14, maxWidth: 480 }}
                  >
                    <div className="form-group">
                      <label>First name</label>
                      <p>{profileForm.firstName}</p>
                    </div>
                    <div className="form-group">
                      <label>Last name</label>
                      <p>{profileForm.lastName}</p>
                    </div>
                    <div className="form-group">
                      <label>Email</label>
                      <p>{profileForm.email}</p>
                    </div>
                    <div className="form-group">
                      <label>Phone number</label>
                      <p>{profileForm.phoneNumber}</p>
                    </div>

                    <div className="form-group">
                      <label htmlFor="birthDate">Birth date</label>
                      <input
                        id="birthDate"
                        type="date"
                        name="birthDate"
                        className="input-field"
                        value={profileForm.birthDate || ''}
                        onChange={handleProfileChange}
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="gender">Gender</label>
                      <input
                        id="gender"
                        type="text"
                        name="gender"
                        className="input-field"
                        value={profileForm.gender || ''}
                        onChange={handleProfileChange}
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="address">Address</label>
                      <input
                        id="address"
                        type="text"
                        name="address"
                        className="input-field"
                        value={profileForm.address || ''}
                        onChange={handleProfileChange}
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="contactNumber">Contact number</label>
                      <input
                        id="contactNumber"
                        type="text"
                        name="contactNumber"
                        className="input-field"
                        value={profileForm.contactNumber || ''}
                        onChange={handleProfileChange}
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="emergencyContact">Emergency contact</label>
                      <input
                        id="emergencyContact"
                        type="text"
                        name="emergencyContact"
                        className="input-field"
                        value={profileForm.emergencyContact || ''}
                        onChange={handleProfileChange}
                      />
                    </div>

                    <button
                      type="submit"
                      className="action-button"
                      disabled={savingProfile}
                      style={{ alignSelf: 'flex-start' }}
                    >
                      {savingProfile ? 'Saving…' : 'Save changes'}
                    </button>
                    {profileMessage && <p className="success-text">{profileMessage}</p>}
                  </form>
                )}
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default PatientDashboard;