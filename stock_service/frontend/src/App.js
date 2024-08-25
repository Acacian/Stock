import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { ErrorBoundary } from 'react-error-boundary';
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

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.data.type === 'USER_LOGGED_IN') {
        setUser(event.data.user);
      } else if (event.data.type === 'USER_LOGGED_OUT') {
        setUser(null);
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, []);

  return (
    <Router>
      <div className="App">
        <ErrorBoundary FallbackComponent={ErrorFallback} onReset={() => window.location.reload()}>
          <Routes>
            <Route path="/" element={
              <ErrorBoundary FallbackComponent={ErrorFallback}>
                <StockList />
              </ErrorBoundary>
            } />
            <Route path="/stock/:stockId" element={
              <ErrorBoundary FallbackComponent={ErrorFallback}>
                <StockDetail />
              </ErrorBoundary>
            } />
            <Route path="/stock/:stockId/discussion" element={
              <ErrorBoundary FallbackComponent={ErrorFallback}>
                <StockDiscussion user={user} />
              </ErrorBoundary>
            } />
          </Routes>
        </ErrorBoundary>
      </div>
    </Router>
  );
}

export default App;