import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { logoutUser } from '../services/UserApi';
import TokenService from '../services/TokenService';

const Navbar = () => {
  const navigate = useNavigate();
  const isLoggedIn = !!TokenService.getAccessToken();

  const handleLogout = async () => {
    try {
      await logoutUser();
      TokenService.removeTokens();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <nav>
      <ul>
        {isLoggedIn ? (
          <>
            <li><Link to="/profile">Profile</Link></li>
            <li><Link to="/settings">Settings</Link></li>
            <li><button onClick={handleLogout}>Logout</button></li>
          </>
        ) : (
          <>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/register">Register</Link></li>
          </>
        )}
      </ul>
    </nav>
  );
};

export default Navbar;