import React, { useState, useEffect } from 'react';
import { getPostsWithActivity } from '../services/SocialApi';
import Post from './Post';
import CreatePost from './CreatePost';

const Feed = ({ userId }) => {
  const [posts, setPosts] = useState([]);

  const fetchPosts = async () => {
    const fetchedPosts = await getPostsWithActivity(userId);
    setPosts(fetchedPosts);
  };

  useEffect(() => {
    fetchPosts();
  }, [userId]);

  return (
    <div className="feed">
      <CreatePost userId={userId} onPostCreated={fetchPosts} />
      {posts.map(post => (
        <Post key={post.id} post={post} currentUserId={userId} />
      ))}
    </div>
  );
};

export default Feed;
