import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import Input from '../components/Input';
import Button from '../components/Button';

const Register = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '', password: '', confirmPassword: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

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
      const response = await api.post('/auth/register', form);
      const data = response.data;
      localStorage.setItem('meditrackUser', JSON.stringify(data));
      setMessage('Registration successful. Redirecting...');
      if (data.role === 'SUPER_ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/nurse/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Create Account</h2>
        <p>Join MediTrack to access healthcare services and manage your health records.</p>
        <form onSubmit={handleSubmit} className="auth-form">
          <Input label="Full Name" name="fullName" value={form.fullName} onChange={handleChange} placeholder="Juan Dela Cruz" />
          <Input label="Email" type="email" name="email" value={form.email} onChange={handleChange} placeholder="you@example.com" />
          <Input label="Phone Number" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="09XXXXXXXXX" />
          <Input label="Password" type="password" name="password" value={form.password} onChange={handleChange} placeholder="Create password" />
          <Input label="Confirm Password" type="password" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} placeholder="Confirm password" />
          <Button label="Register" type="submit" />
        </form>
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <p className="auth-link">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
