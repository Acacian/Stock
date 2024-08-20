import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Feed from './components/Feed';
import PostDetail from './components/PostDetail';
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
          <Route path="/" element={<Feed user={user} />} />
          <Route path="/post/:postId" element={<PostDetail user={user} />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;