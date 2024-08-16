import React, { useState, useEffect } from 'react';

const NewsfeedComponent = ({ userId }) => {
  const [newsfeed, setNewsfeed] = useState([]);

  useEffect(() => {
    const fetchNewsfeed = async () => {
      try {
        const response = await fetch(`/api/newsfeed/${userId}`);
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        setNewsfeed(data);
      } catch (error) {
        console.error('Error fetching newsfeed:', error);
      }
    };

    fetchNewsfeed();
  }, [userId]);

  return (
    <div className="newsfeed">
      <h2>Newsfeed</h2>
      {newsfeed.map((item, index) => (
        <div key={index} className="newsfeed-item">
          {item}
        </div>
      ))}
    </div>
  );
};

export default NewsfeedComponent;