import React, { createContext, useState, useContext, useEffect } from 'react';
import { loginUser, logoutUser, refreshTokenApi } from '../services/UserApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
          const decodedUser = decodeJWT(token);
          if (decodedUser) {
            setUser(decodedUser);
          } else {
            await refreshUserToken();
          }
        } catch (error) {
          console.error('Error initializing auth:', error);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const refreshUserToken = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        const response = await refreshTokenApi(refreshToken);
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        setUser(decodeJWT(response.accessToken));
      } catch (error) {
        console.error('Failed to refresh token:', error);
        logout();
      }
    }
  };

  useEffect(() => {
    const tokenRefreshInterval = setInterval(() => {
      refreshUserToken();
    }, 14 * 60 * 1000); // 14분마다 토큰 갱신

    return () => clearInterval(tokenRefreshInterval);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await loginUser(email, password);
      if (response.accessToken) {
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        const decodedUser = decodeJWT(response.accessToken);
        setUser(decodedUser);
        return true;
      }
      return false;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  };

  const logout = async () => {
    try {
      await logoutUser();
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const decodeJWT = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding JWT:', error);
      return null;
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);