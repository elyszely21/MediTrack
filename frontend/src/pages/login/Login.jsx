import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../shared/api/axios';
import Input from '../../shared/ui/Input';
import Button from '../../shared/ui/Button';

const Login = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!form.email || !form.password) {
      setError('Please enter both email and password.');
      return;
    }

    try {
      const response = await api.post('/auth/login', form);
      const data = response.data;
      localStorage.setItem('meditrackUser', JSON.stringify(data));
      setMessage('Login successful. Redirecting...');
      if (data.role === 'SUPER_ADMIN') {
        navigate('/admin/dashboard');
      } else if (data.role === 'DOCTOR') {
        navigate('/doctor/dashboard');
      } else if (data.role === 'PATIENT') {
        navigate('/patient/dashboard');
      } else {
        navigate('/nurse/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to log in right now.');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Welcome Back</h2>
        <p>Sign in to continue tracking your medication inventory.</p>
        <form onSubmit={handleSubmit} className="auth-form">
          <Input label="Email" type="email" name="email" value={form.email} onChange={handleChange} placeholder="you@example.com" />
          <Input label="Password" type="password" name="password" value={form.password} onChange={handleChange} placeholder="Enter password" />
          <Button label="Login" type="submit" />
        </form>
        {message && <p className="success-text">{message}</p>}
        {error && <p className="error-text">{error}</p>}
        <p className="auth-link">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;