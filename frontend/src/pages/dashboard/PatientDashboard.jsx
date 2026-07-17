import { useEffect, useState, useCallback } from 'react';

import api from '../../shared/api/axios';

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

  const [showBooking, setShowBooking] = useState(false);
  const [bookingForm, setBookingForm] = useState({
    appointmentDate: '',
    appointmentTime: '',
    doctorId: '',
    remarks: ''
  });
  const [doctors, setDoctors] = useState([]);
  const [bookingSubmitting, setBookingSubmitting] = useState(false);
  const [bookingMessage, setBookingMessage] = useState('');
  const [bookingError, setBookingError] = useState('');

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

  const fetchDoctors = async () => {
    try {
      const res = await api.get('/doctors/schedule');
      setDoctors(res.data || []);
    } catch {
      // silent
    }
  };

  const handleBookAppointment = async (e) => {
    e.preventDefault();
    setBookingSubmitting(true);
    setBookingMessage('');
    setBookingError('');
    try {
      const durationMinutes = 30;
      const [hours, minutes] = bookingForm.appointmentTime.split(':').map(Number);
      const [year, month, day] = bookingForm.appointmentDate.split('-').map(Number);
      const startDate = new Date(year, month - 1, day, hours, minutes);
      const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);
      const endTime = endDate.toTimeString().slice(0, 5);

      await api.post('/appointments', {
        patientId: profile?.id,
        doctorId: bookingForm.doctorId,
        appointmentType: 'CONSULTATION',
        durationMinutes: durationMinutes,
        appointmentDate: bookingForm.appointmentDate,
        appointmentTime: bookingForm.appointmentTime + ':00',
        endTime: endTime + ':00',
        remarks: bookingForm.remarks || 'Patient self-booking'
      });

      setBookingMessage('Appointment booked successfully.');
      setBookingForm({ appointmentDate: '', appointmentTime: '', doctorId: '', remarks: '' });
      setShowBooking(false);
      setTimeout(() => {
        setBookingMessage('');
        loadAll();
      }, 1500);
    } catch (err) {
      setBookingError(err.response?.data?.message || 'Could not book appointment.');
    } finally {
      setBookingSubmitting(false);
    }
  };

  useEffect(() => {
    if (showBooking) {
      fetchDoctors();
    }
  }, [showBooking]);

  const upcomingCount = appointments.filter((a) =>
    ['PENDING', 'APPROVED'].includes(a.status)
  ).length;
  const activePrescriptionCount = prescriptions.filter(
    (p) => p.status === 'ACTIVE'
  ).length;

  return (
    <div className="dashboard-page">
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
          <p className="loading-text">Loading your records…</p>
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

            <div className="tab-panel">
              <div className="tab-bar">
                {TABS.map((tab) => (
                  <button
                    key={tab.key}
                    onClick={() => setActiveTab(tab.key)}
                    className={activeTab === tab.key ? 'active' : ''}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              <div style={{ padding: 16 }}>
                {activeTab === 'appointments' && (
                  <>
                    <div style={{ marginBottom: 16, textAlign: 'right' }}>
                      <button
                        onClick={() => setShowBooking(true)}
                        className="action-button"
                        style={{ background: '#2563eb', color: 'white' }}
                      >
                        + Book Appointment
                      </button>
                    </div>

                    {appointments.length === 0 ? (
                      <div className="empty-state">
                        <div className="empty-state-icon">📅</div>
                        <p>You have no appointments yet.</p>
                      </div>
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
                    )}
                  </>
                )}

                {activeTab === 'prescriptions' && (
                  prescriptions.length === 0 ? (
                    <div className="empty-state">
                      <div className="empty-state-icon">💊</div>
                      <p>You have no prescriptions yet.</p>
                    </div>
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
                    <div className="empty-state">
                      <div className="empty-state-icon">📋</div>
                      <p>You have no medical records yet.</p>
                    </div>
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

      {showBooking && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center p-4 border-b">
              <h2 className="text-lg font-semibold">Book Appointment</h2>
              <button onClick={() => setShowBooking(false)} className="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
            </div>
            <form onSubmit={handleBookAppointment} className="p-4 space-y-4">
              {bookingError && (
                <div role="alert" className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                  {bookingError}
                </div>
              )}
              {bookingMessage && (
                <p role="status" className="text-sm text-green-700">{bookingMessage}</p>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Doctor *</label>
                <select
                  required
                  value={bookingForm.doctorId}
                  onChange={e => setBookingForm({ ...bookingForm, doctorId: e.target.value })}
                  className="border border-gray-300 rounded-lg p-2 w-full text-sm"
                >
                  <option value="">Select doctor</option>
                  {doctors.map(d => (
                    <option key={d.id} value={d.id}>
                      {d.fullName} — {d.specialization}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Date *</label>
                  <input
                    type="date"
                    required
                    value={bookingForm.appointmentDate}
                    onChange={e => setBookingForm({ ...bookingForm, appointmentDate: e.target.value })}
                    className="border border-gray-300 rounded-lg p-2 w-full text-sm"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Time *</label>
                  <input
                    type="time"
                    required
                    value={bookingForm.appointmentTime}
                    onChange={e => setBookingForm({ ...bookingForm, appointmentTime: e.target.value })}
                    className="border border-gray-300 rounded-lg p-2 w-full text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Remarks</label>
                <textarea
                  rows={2}
                  value={bookingForm.remarks}
                  onChange={e => setBookingForm({ ...bookingForm, remarks: e.target.value })}
                  className="border border-gray-300 rounded-lg p-2 w-full text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowBooking(false)} className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                  Cancel
                </button>
                <button type="submit" disabled={bookingSubmitting} className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm disabled:opacity-60">
                  {bookingSubmitting ? 'Booking...' : 'Book Appointment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;