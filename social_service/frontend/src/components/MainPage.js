import React from 'react';
import ChatComponent from './ChatComponent';
import WebRTCComponent from './WebRTCComponent';
import Feed from './Feed';
import SearchComponent from './SearchComponent';

const MainPage = ({ user }) => {
  return (
    <div className="main-page">
      <div className="search-section">
        <SearchComponent />
      </div>
      <div className="chat-section">
        <ChatComponent />
      </div>
      <div className="video-section">
        <WebRTCComponent />
      </div>
      <div className="post-section">
        <Feed userId={user.id} />
      </div>
    </div>
  );
};

export default MainPage;