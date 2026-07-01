import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import Input from '../components/Input';
import Button from '../components/Button';

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
      localStorage.setItem('meditrackUser', JSON.stringify(response.data));
      setMessage('Login successful. Redirecting...');
      navigate('/dashboard');
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
