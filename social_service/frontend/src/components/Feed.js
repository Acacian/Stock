import React, { useState, useEffect } from 'react';
import { getPostsWithActivity } from '../services/SocialApi';
import Post from './Post';
import CreatePost from './CreatePost';

const Feed = ({ userId }) => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const fetchedPosts = await getPostsWithActivity(userId);
      setPosts(fetchedPosts);
    } catch (error) {
      console.error('Error fetching posts:', error);
      setError('Failed to load posts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [userId]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

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