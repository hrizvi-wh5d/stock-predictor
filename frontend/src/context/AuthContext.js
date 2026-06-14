import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

// Context = a way to share state across components without prop drilling
// Think of it like a global variable that React components can subscribe to
const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On app start, check if a token is stored (user was previously logged in)
  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    if (token && username) {
      // Set the token on all future axios requests automatically
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setUser({ username, token });
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    const response = await axios.post('/api/auth/login', { username, password });
    const { token } = response.data;

    // Store token in localStorage so it survives page refresh
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);

    // Attach token to every future HTTP request header
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setUser({ username, token });
    return response.data;
  };

  const register = async (userData) => {
    const response = await axios.post('/api/auth/register', userData);
    return response.data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook - components call useAuth() to access login state
export const useAuth = () => useContext(AuthContext);
