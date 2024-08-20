import React, { createContext, useState, useContext, useEffect } from 'react';
import { loginUser, logoutUser } from '../services/UserApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const login = async (email, password) => {
    try {
      const userData = await loginUser(email, password);
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      window.parent.postMessage({ type: 'USER_LOGGED_IN', user: userData }, '*');
      return true;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  };

  const logout = async () => {
    try {
      await logoutUser(user.token);
      setUser(null);
      localStorage.removeItem('user');
      window.parent.postMessage({ type: 'USER_LOGGED_OUT' }, '*');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);