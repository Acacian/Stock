import React, { useState, useEffect, useCallback } from 'react';
import { getUserProfile, updateUserProfile, uploadProfileImage } from '../services/UserApi';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updateSuccess, setUpdateSuccess] = useState(false);
  const [imageFile, setImageFile] = useState(null);

  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getUserProfile();
      setProfile(data);
      setError('');
    } catch (error) {
      setError('Failed to fetch user profile. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    setImageFile(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setUpdateSuccess(false);
  
    try {
      let updatedProfile = await updateUserProfile({
        name: profile.name,
        introduction: profile.introduction
      });

      if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);
        const imageResponse = await uploadProfileImage(formData);
        updatedProfile = { ...updatedProfile, profileImage: imageResponse.fileUrl };
      }

      setProfile(updatedProfile);
      setUpdateSuccess(true);
      setImageFile(null);
    } catch (error) {
      setError('Failed to update profile. Please try again.');
    }
  };

  if (loading) {
    return <div>Loading profile...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
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
        <div className="form-group">
          <label htmlFor="profileImage">Profile Image:</label>
          <input
            type="file"
            id="profileImage"
            name="profileImage"
            onChange={handleImageChange}
            accept="image/*"
          />
        </div>
        {profile.profileImage ? (
          <div className="form-group">
            <img src={profile.profileImage} alt="Profile" className="profile-image" />
          </div>
        ) : (
          <div className="form-group">
            <img src="/default-profile.png" alt="Default Profile" className="profile-image" />
          </div>
        )}
        <button type="submit">Update Profile</button>
      </form>
      {updateSuccess && <p style={{ color: 'green' }}>Profile updated successfully!</p>}
    </div>
  );
};

export default Profile;