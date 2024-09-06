import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    console.log('Current user state in Navbar:', user); // 디버깅용
  }, [user]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (loading) {
    return <nav>Loading...</nav>;
  }

  return (
    <nav>
      <ul>
        {user ? (
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