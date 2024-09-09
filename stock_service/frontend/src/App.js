import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import TokenService from './services/TokenService';
import StockList from './components/StockList';
import StockDetail from './components/StockDetail';
import StockDiscussion from './components/StockDiscussion';
import './App.css';

// 환경 변수에서 인증 서비스 URL을 가져옵니다.
const AUTH_SERVICE_URL = process.env.REACT_APP_AUTH_SERVICE_URL || 'https://localhost:3001';

function App() {
  const isAuthenticated = () => TokenService.getAccessToken() !== null;

  // 인증되지 않은 사용자를 인증 서비스로 리다이렉션하는 컴포넌트
  const ProtectedRoute = ({ children }) => {
    if (!isAuthenticated()) {
      return <Navigate to={AUTH_SERVICE_URL} replace />;
    }
    return children;
  };

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<StockList />} />
          <Route path="/stock/:stockId" element={<StockDetail />} />
          <Route 
            path="/stock/:stockId/discussion" 
            element={<ProtectedRoute><StockDiscussion /></ProtectedRoute>} 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;