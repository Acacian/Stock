import React, { useEffect, useState } from 'react';
import Newsfeed from './components/Newsfeed';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    // User Service로부터의 메시지 리스닝
    window.addEventListener('message', (event) => {
      if (event.origin !== 'http://localhost:3001') {
        if (event.data.type === 'USER_LOGGED_IN') {
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

  return (
    <div>
      <h1>Newsfeed Service</h1>
      {user ? <Newsfeed user={user} /> : <p>Please log in to view your newsfeed.</p>}
    </div>
  );
}

export default App;
