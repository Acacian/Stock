import React, { useEffect, useState } from 'react';
import Feed from './components/Feed';
import './App.css';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.origin === 'http://localhost:3001') {
        if (event.data.type === 'USER_LOGGED_IN') {
          setUser(event.data.user);
        } else if (event.data.type === 'USER_LOGGED_OUT') {
          setUser(null);
        }
      }
    };

    window.addEventListener('message', handleMessage);

    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }

    return () => {
      window.removeEventListener('message', handleMessage);
    };
  }, []);

  return (
    <div className="App">
      <h1>Social Feed</h1>
      {user ? <Feed userId={user.id} /> : <p>Please log in to view your social feed.</p>}
    </div>
  );
}

export default App;