import React, { useState, useEffect, useCallback } from 'react';
import { getUserProfile, updateUserProfile } from '../services/UserApi';
import { useAuth } from '../context/AuthContext';

const Profile = () => {
  const { user, loading: authLoading } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updateSuccess, setUpdateSuccess] = useState(false);

  const fetchProfile = useCallback(async () => {
    if (!user || !user.id) return;
    try {
      setLoading(true);
      const data = await getUserProfile(user.id);
      setProfile(data);
      setError('');
    } catch (error) {
      setError('Failed to fetch user profile. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!authLoading) {
      fetchProfile();
    }
  }, [authLoading, fetchProfile]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setUpdateSuccess(false);
    try {
      const updatedProfile = await updateUserProfile(user.id, profile);
      setProfile(updatedProfile);
      setUpdateSuccess(true);
    } catch (error) {
      setError('Failed to update profile. Please try again.');
    }
  };

  if (authLoading || loading) {
    return <div>Loading profile...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  if (!user) {
    return <div>Please log in to view your profile.</div>;
  }

  if (!profile) {
    return <div>No profile data available.</div>;
  }

  return (
    <div className="profile-container">
      <h2>User Profile</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">Name:</label>
          <input
            type="text"
            id="name"
            name="name"
            value={profile.name || ''}
            onChange={handleChange}
            placeholder="Your Name"
          />
        </div>
        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            name="email"
            value={profile.email || ''}
            readOnly
          />
        </div>
        <div className="form-group">
          <label htmlFor="introduction">Introduction:</label>
          <textarea
            id="introduction"
            name="introduction"
            value={profile.introduction || ''}
            onChange={handleChange}
            placeholder="Tell us about yourself"
          />
        </div>
        {profile.profileImage && (
          <div className="form-group">
            <img src={profile.profileImage} alt="Profile" className="profile-image" />
          </div>
        )}
        <button type="submit">Update Profile</button>
      </form>
      {updateSuccess && <p style={{ color: 'green' }}>Profile updated successfully!</p>}
    </div>
  );
};

export default Profile;