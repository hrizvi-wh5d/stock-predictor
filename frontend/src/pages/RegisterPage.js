import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [form, setForm] = useState({
    username: '', email: '', password: '', fullName: '', preferredMarket: 'NASDAQ'
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register, login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      // Auto-login after registration
      await login(form.username, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <h1>📈 StockPredictor</h1>
          <p>Create your account</p>
        </div>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input type="text" placeholder="Your full name"
              value={form.fullName}
              onChange={e => setForm({...form, fullName: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Username</label>
            <input type="text" placeholder="Choose a username"
              value={form.username}
              onChange={e => setForm({...form, username: e.target.value})}
              required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" placeholder="your@email.com"
              value={form.email}
              onChange={e => setForm({...form, email: e.target.value})}
              required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" placeholder="Choose a password"
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})}
              required />
          </div>
          <div className="form-group">
            <label>Preferred Market</label>
            <select value={form.preferredMarket}
              onChange={e => setForm({...form, preferredMarket: e.target.value})}>
              <option value="NASDAQ">NASDAQ (US Tech Stocks)</option>
              <option value="FTSE">FTSE 100 (UK Stocks)</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
