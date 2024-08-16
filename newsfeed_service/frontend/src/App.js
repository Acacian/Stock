import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [newsfeed, setNewsfeed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchNewsfeed();
  }, []);

  const fetchNewsfeed = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/newsfeed');
      if (!response.ok) {
        throw new Error('Failed to fetch newsfeed');
      }
      const data = await response.json();
      setNewsfeed(data);
      setLoading(false);
    } catch (error) {
      setError('Error fetching newsfeed: ' + error.message);
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="App">
      <h1>Newsfeed</h1>
      <div className="newsfeed">
        {newsfeed.map((item, index) => (
          <div key={index} className="newsfeed-item">
            <p>{item.content}</p>
            <small>Posted at: {new Date(item.timestamp).toLocaleString()}</small>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;