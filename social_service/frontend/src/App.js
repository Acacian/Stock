import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import MainPage from './components/MainPage';
import './App.css';

function AppContent() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <div className="App">
        <h1>Social Platform</h1>
        <Routes>
          <Route path="/" element={
            user ? <MainPage user={user} />
            : <Navigate to="https://localhost:3001/login" />
          } />
        </Routes>
      </div>
    </Router>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;