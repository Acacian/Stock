import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Newsfeed from './components/Newsfeed';
import NotificationComponent from './components/NotificationComponent';
import './App.css';

function AppContent() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <div className="App">
        <h2>Newsfeed</h2>
        <Routes>
          <Route path="/" element={
            user ? (
              <>
                <NotificationComponent currentUserId={user.id} />
                <Newsfeed userId={user.id} />
              </>
            ) : <Navigate to="https://localhost:3001/login" />
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