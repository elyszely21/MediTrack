import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Input from '../components/Input';
import Button from '../components/Button';
import Navbar from '../components/Navbar';

const RegisterNurse = () => {
  const navigate = useNavigate();
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;

  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '', password: '', confirmPassword: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Redirect if not SUPER_ADMIN
  if (!user || user.role !== 'SUPER_ADMIN') {
    navigate('/admin/dashboard');
    return null;
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!form.fullName || !form.email || !form.phoneNumber || !form.password || !form.confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
      setError('Please enter a valid email address.');
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      const response = await api.post('/auth/admin/register-nurse', form);
      setMessage('Nurse registered successfully!');
      setForm({ fullName: '', email: '', phoneNumber: '', password: '', confirmPassword: '' });
      setTimeout(() => {
        navigate('/nurses');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register nurse.');
    }
  };

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <div className="auth-card" style={{ maxWidth: '500px', margin: '40px auto' }}>
          <h2>Register New Nurse</h2>
          <p>Create a new nurse account for clinic staff.</p>
          <form onSubmit={handleSubmit} className="auth-form">
            <Input label="Full Name" name="fullName" value={form.fullName} onChange={handleChange} placeholder="Juan Dela Cruz" />
            <Input label="Email" type="email" name="email" value={form.email} onChange={handleChange} placeholder="nurse@clinic.com" />
            <Input label="Phone Number" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="09XXXXXXXXX" />
            <Input label="Password" type="password" name="password" value={form.password} onChange={handleChange} placeholder="Create password" />
            <Input label="Confirm Password" type="password" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} placeholder="Confirm password" />
            <Button label="Register Nurse" type="submit" />
          </form>
          {message && <p className="success-text">{message}</p>}
          {error && <p className="error-text">{error}</p>}
        </div>
      </main>
    </div>
  );
};

export default RegisterNurse;
