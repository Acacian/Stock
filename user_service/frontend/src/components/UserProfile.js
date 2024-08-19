import React, { useState, useEffect } from 'react';
import { getUserProfile, updateUserProfile } from '../services/UserApi';

const UserProfile = ({ user }) => {
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const fetchedProfile = await getUserProfile(user.id);
      setProfile(fetchedProfile);
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await updateUserProfile(user.id, profile);
      alert('Profile updated successfully');
    } catch (error) {
      console.error('Failed to update profile:', error);
    }
  };

  if (!profile) return <div>Loading...</div>;

  return (
    <div className="user-profile">
      <h2>User Profile</h2>
      <form onSubmit={handleUpdate}>
        <input
          type="text"
          value={profile.name}
          onChange={(e) => setProfile({...profile, name: e.target.value})}
          placeholder="Name"
        />
        <input
          type="email"
          value={profile.email}
          readOnly
        />
        <textarea
          value={profile.introduction}
          onChange={(e) => setProfile({...profile, introduction: e.target.value})}
          placeholder="Introduction"
        />
        <button type="submit">Update Profile</button>
      </form>
    </div>
  );
};

export default UserProfile;