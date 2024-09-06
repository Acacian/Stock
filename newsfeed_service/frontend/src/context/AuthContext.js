import React, { createContext, useState, useContext, useEffect } from 'react';
import { checkAuthStatus, logout } from '../services/AuthService';

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

  return (
    <AuthContext.Provider value={{ user, logout: logoutUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);