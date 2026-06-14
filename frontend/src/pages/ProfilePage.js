import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ fullName: '', preferredMarket: 'NASDAQ' });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axios.get('/api/user/profile').then(res => {
      setProfile(res.data);
      setForm({ fullName: res.data.fullName, preferredMarket: res.data.preferredMarket });
    });
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await axios.put('/api/user/profile', form);
      setMessage('✅ Profile updated successfully!');
    } catch (err) {
      setMessage('❌ Failed to update profile.');
    } finally {
      setLoading(false);
      setTimeout(() => setMessage(''), 3000);
    }
  };

  return (
    <div>
      <nav className="navbar">
        <span className="navbar-brand">📈 StockPredictor</span>
        <div className="navbar-actions">
          <Link to="/dashboard" className="btn btn-outline" style={{padding:'6px 12px',fontSize:'13px'}}>← Dashboard</Link>
          <button className="btn btn-outline" onClick={logout} style={{padding:'6px 12px',fontSize:'13px'}}>Logout</button>
        </div>
      </nav>

      <div className="dashboard">
        <div style={{maxWidth: 500, margin: '40px auto'}}>
          <div className="card">
            <h3>Your Profile</h3>
            <div style={{marginBottom: 20, padding: '14px', background: 'rgba(59,130,246,0.05)',
              borderRadius: 8, border: '1px solid rgba(59,130,246,0.15)'}}>
              <p style={{fontSize: 13, color: '#9ca3af'}}>Username: <strong style={{color:'#f9fafb'}}>{profile?.username}</strong></p>
              <p style={{fontSize: 13, color: '#9ca3af', marginTop: 4}}>Email: <strong style={{color:'#f9fafb'}}>{profile?.email}</strong></p>
              <p style={{fontSize: 13, color: '#9ca3af', marginTop: 4}}>Member since: <strong style={{color:'#f9fafb'}}>{profile?.createdAt?.split('T')[0]}</strong></p>
            </div>

            {message && <div className="error-msg" style={{background:'rgba(16,185,129,0.1)',borderColor:'rgba(16,185,129,0.3)',color:'#6ee7b7'}}>{message}</div>}

            <form onSubmit={handleSave}>
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" value={form.fullName}
                  onChange={e => setForm({...form, fullName: e.target.value})}
                  placeholder="Your full name" />
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
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
