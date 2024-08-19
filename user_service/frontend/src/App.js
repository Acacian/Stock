import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Profile from './components/Profile';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    // 다른 서비스로부터의 메시지 리스닝
    window.addEventListener('message', (event) => {
      if (event.origin !== 'http://localhost:3001') {
        // 사용자 관련 이벤트 처리
        if (event.data.type === 'USER_UPDATED') {
          setUser(event.data.user);
        }
      }
    });

    // 로컬 스토리지에서 사용자 정보 가져오기
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    // 다른 서비스에 로그인 이벤트 알림
    window.parent.postMessage({ type: 'USER_LOGGED_IN', user: userData }, '*');
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login onLogin={handleLogin} />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile user={user} />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;