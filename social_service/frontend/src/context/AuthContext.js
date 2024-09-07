import React, { createContext, useState, useContext, useEffect } from 'react';
import { checkAuthStatus, logout } from '../services/AuthService';
import * as SocialApi from '../services/SocialApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        const userData = await checkAuthStatus();
        setUser(userData);
      } catch (error) {
        console.error('Auth verification failed:', error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    verifyAuth();
    const interval = setInterval(verifyAuth, 5 * 60 * 1000);

    return () => clearInterval(interval);
  }, []);

  const logoutUser = async () => {
    await logout();
    setUser(null);
  };

  const socialActions = {
    createPost: (title, content) => SocialApi.createPost(user.id, title, content),
    addComment: (postId, content) => SocialApi.addComment(user.id, postId, content),
    likePost: (postId) => SocialApi.likePost(user.id, postId),
    follow: (followedId) => SocialApi.follow(user.id, followedId),
    unfollow: (followedId) => SocialApi.unfollow(user.id, followedId),
    getPostsByUserId: () => SocialApi.getPostsByUserId(user.id),
    getPostsWithActivity: () => SocialApi.getPostsWithActivity(user.id),
    getFollowers: () => SocialApi.getFollowers(user.id),
    getFollowing: () => SocialApi.getFollowing(user.id),
    searchPosts: SocialApi.searchPosts,
  };

  return (
    <AuthContext.Provider value={{ user, logout: logoutUser, loading, socialActions }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);