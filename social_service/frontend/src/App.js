import React, { useEffect, useState } from 'react';
import MainPage from './components/MainPage';
import './App.css';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.origin === 'https://localhost:3001') {
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
      <h1>Social Platform</h1>
      {user ? <MainPage user={user} /> : <p>Please log in to use the platform.</p>}
    </div>
  );
}

export default App;
