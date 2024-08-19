import React, { useState, useEffect } from 'react';
import { getUserProfile, updateUserProfile } from '../services/UserApi';

const Profile = ({ userId }) => {
  const [profile, setProfile] = useState(null);
  const [newImage, setNewImage] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      const data = await getUserProfile(userId);
      setProfile(data);
    };
    fetchProfile();
  }, [userId]);

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
      const updatedProfile = await updateUserProfile(userId, formData);
      setProfile(updatedProfile);
      alert('Profile updated successfully');
      
      // Notify other services
      window.parent.postMessage({
        type: 'PROFILE_UPDATED',
        userId: userId,
        newProfile: updatedProfile
      }, '*');
    } catch (error) {
      console.error('Profile update failed:', error);
    }
  };

  if (!profile) return <div>Loading...</div>;

  return (
    <form onSubmit={handleSubmit}>
      <img src={profile.profileImageUrl} alt={profile.name} />
      <input type="file" onChange={handleImageUpload} />
      <input
        type="text"
        value={profile.name}
        onChange={(e) => setProfile({...profile, name: e.target.value})}
        placeholder="Name"
      />
      <textarea
        value={profile.introduction}
        onChange={(e) => setProfile({...profile, introduction: e.target.value})}
        placeholder="Introduction"
      />
      <button type="submit">Update Profile</button>
    </form>
  );
};

export default Profile;