import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Profile from './components/Profile';
import Navbar from './components/Navbar';
import Settings from './components/Settings';
import './App.css';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    window.addEventListener('message', (event) => {
      if (event.origin !== 'http://localhost:3001') {
        if (event.data.type === 'USER_UPDATED') {
          setUser(event.data.user);
        }
      }
    });

    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    window.parent.postMessage({ type: 'USER_LOGGED_IN', user: userData }, '*');
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    window.parent.postMessage({ type: 'USER_LOGGED_OUT' }, '*');
  };

  return (
    <Router>
      <div className="App">
        <Navbar user={user} onLogout={handleLogout} />
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/profile" replace /> : <Login onLogin={handleLogin} />} />
          <Route path="/register" element={user ? <Navigate to="/profile" replace /> : <Register />} />
          <Route path="/profile" element={user ? <Profile user={user} /> : <Navigate to="/login" replace />} />
          <Route path="/settings" element={user ? <Settings user={user} /> : <Navigate to="/login" replace />} />
          <Route path="/" element={<Navigate to={user ? "/profile" : "/login"} replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;