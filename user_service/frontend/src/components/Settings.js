import React, { useState } from 'react';
import { updatePassword } from '../services/UserApi';

const Settings = () => {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await updatePassword(oldPassword, newPassword);
      setSuccess('Password updated successfully');
      setOldPassword('');
      setNewPassword('');
    } catch (error) {
      setError('Failed to update password. Please check your old password and try again.');
    }
  };

  return (
    <div className="settings">
      <h2>Settings</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
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