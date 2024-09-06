import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { ErrorBoundary } from 'react-error-boundary';
import { AuthProvider, useAuth } from './context/AuthContext';
import StockList from './components/StockList';
import StockDetail from './components/StockDetail';
import StockDiscussion from './components/StockDiscussion';
import './App.css';

function ErrorFallback({error, resetErrorBoundary}) {
  return (
    <div role="alert" className="error-fallback">
      <h2>오류가 발생했습니다</h2>
      <p>죄송합니다. 문제가 발생했습니다:</p>
      <pre>{error.message}</pre>
      <button onClick={resetErrorBoundary}>다시 시도</button>
    </div>
  );
}

function AppContent() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <div className="App">
        <ErrorBoundary FallbackComponent={ErrorFallback} onReset={() => window.location.reload()}>
          <Routes>
            <Route path="/" element={
              user ? <ErrorBoundary FallbackComponent={ErrorFallback}><StockList /></ErrorBoundary>
              : <Navigate to="https://localhost:3001/login" />
            } />
            <Route path="/stock/:stockId" element={
              user ? <ErrorBoundary FallbackComponent={ErrorFallback}><StockDetail /></ErrorBoundary>
              : <Navigate to="https://localhost:3001/login" />
            } />
            <Route path="/stock/:stockId/discussion" element={
              user ? <ErrorBoundary FallbackComponent={ErrorFallback}><StockDiscussion user={user} /></ErrorBoundary>
              : <Navigate to="https://localhost:3001/login" />
            } />
          </Routes>
        </ErrorBoundary>
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