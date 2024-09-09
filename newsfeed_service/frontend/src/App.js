import React, { useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Newsfeed from './components/Newsfeed';
import NotificationComponent from './components/NotificationComponent';
import TokenService from './services/TokenService';
import './App.css';

// 환경 변수에서 인증 서비스 URL을 가져옵니다.
const AUTH_SERVICE_URL = process.env.REACT_APP_AUTH_SERVICE_URL || 'https://localhost:3001';

function App() {
  const isAuthenticated = () => TokenService.getAccessToken() !== null;

  // 인증되지 않은 사용자를 인증 서비스로 리다이렉션하는 함수
  const redirectToAuth = () => {
    window.location.href = AUTH_SERVICE_URL;
  };

  // 보호된 라우트를 위한 컴포넌트
  const ProtectedRoute = ({ children }) => {
    useEffect(() => {
      if (!isAuthenticated()) {
        redirectToAuth();
      }
    }, []);

    return isAuthenticated() ? children : null;
  };

  return (
    <Router>
      <div className="App">
        <h2>Newsfeed</h2>
        <Routes>
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <>
                  <NotificationComponent />
                  <Newsfeed />
                </>
              </ProtectedRoute>
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;