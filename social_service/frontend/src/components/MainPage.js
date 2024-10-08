import React from 'react';
import ChatComponent from './ChatComponent';
import WebRTCComponent from './WebRTCComponent';
import Feed from './Feed';
import SearchComponent from './SearchComponent';

const MainPage = () => {
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
        <Feed />
      </div>
    </div>
  );
};

export default MainPage;