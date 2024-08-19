import React, { useState } from 'react';
import { updatePassword } from '../services/UserApi';

const Settings = ({ user }) => {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    try {
      await updatePassword(user.id, oldPassword, newPassword);
      alert('Password updated successfully');
      setOldPassword('');
      setNewPassword('');
    } catch (error) {
      console.error('Failed to update password:', error);
      alert('Failed to update password. Please check your old password and try again.');
    }
  };

  return (
    <div className="settings">
      <h2>Settings</h2>
      <form onSubmit={handlePasswordChange}>
        <input
          type="password"
          value={oldPassword}
          onChange={(e) => setOldPassword(e.target.value)}
          placeholder="Old Password"
          required
        />
        <input
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          placeholder="New Password"
          required
        />
        <button type="submit">Change Password</button>
      </form>
    </div>
  );
};

export default Settings;