import React, { createContext, useState, useContext, useEffect } from 'react';
import { checkAuthStatus, logout } from '../services/AuthService';
import NewsfeedApi from '../services/NewsfeedApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [newsfeed, setNewsfeed] = useState([]);

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        const userData = await checkAuthStatus();
        setUser(userData);
        if (userData) {
          fetchNewsfeed(userData.id);
        }
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
    setNewsfeed([]);
  };

  const fetchNewsfeed = async (userId) => {
    try {
      const newsfeedData = await NewsfeedApi.fetchNewsfeed(userId);
      setNewsfeed(newsfeedData);
    } catch (error) {
      console.error('Failed to fetch newsfeed:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, logout: logoutUser, loading, newsfeed, fetchNewsfeed }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);