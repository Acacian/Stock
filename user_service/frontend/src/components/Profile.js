import React, { useState, useEffect } from 'react';
import { getUserProfile, updateUserProfile } from '../services/UserApi';

const Profile = ({ user }) => {
  const [profile, setProfile] = useState(null);
  const [newImage, setNewImage] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await getUserProfile(user.id);
        setProfile(data);
      } catch (error) {
        console.error('Failed to fetch user profile:', error);
      }
    };
    fetchProfile();
  }, [user.id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleImageUpload = (e) => {
    setNewImage(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append('name', profile.name);
    formData.append('introduction', profile.introduction);
    if (newImage) {
      formData.append('profileImage', newImage);
    }
    try {
      const updatedProfile = await updateUserProfile(user.id, formData);
      setProfile(updatedProfile);
      alert('Profile updated successfully');
      
      window.parent.postMessage({
        type: 'PROFILE_UPDATED',
        userId: user.id,
        newProfile: updatedProfile
      }, '*');
    } catch (error) {
      console.error('Profile update failed:', error);
      alert('Failed to update profile. Please try again.');
    }
  };

  if (!profile) return <div>Loading...</div>;

  return (
    <div className="user-profile">
      <h2>User Profile</h2>
      <form onSubmit={handleSubmit}>
        <img src={profile.profileImageUrl} alt={profile.name} />
        <input type="file" onChange={handleImageUpload} />
        <input
          type="text"
          name="name"
          value={profile.name}
          onChange={handleChange}
          placeholder="Name"
        />
        <input
          type="email"
          value={profile.email}
          readOnly
        />
        <textarea
          name="introduction"
          value={profile.introduction}
          onChange={handleChange}
          placeholder="Introduction"
        />
        <button type="submit">Update Profile</button>
      </form>
    </div>
  );
};

export default Profile;