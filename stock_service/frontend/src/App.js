import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import StockList from './components/StockList';
import StockDetail from './components/StockDetail';
import StockDiscussion from './components/StockDiscussion';
import './App.css';

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
        <Routes>
          <Route path="/" element={<StockList />} />
          <Route path="/stock/:stockId" element={<StockDetail />} />
          <Route path="/stock/:stockId/discussion" element={<StockDiscussion user={user} />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;