import React, { useEffect, useState } from 'react';
import Newsfeed from './components/Newsfeed';
import NotificationComponent from './components/NotificationComponent';
import './App.css';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.origin === 'https://localhost:3001' && event.data.type === 'USER_LOGGED_IN') {
        setUser(event.data.user);
        localStorage.setItem('user', JSON.stringify(event.data.user));
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
      <h2>Newsfeed</h2>
      {user ? (
        <>
          <NotificationComponent currentUserId={user.id} />
          <Newsfeed userId={user.id} />
        </>
      ) : (
        <p>Please log in to view your newsfeed.</p>
      )}
    </div>
  );
}

export default App;