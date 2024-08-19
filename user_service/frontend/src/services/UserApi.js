const API_URL = process.env.REACT_APP_API_URL || '/api/auth';

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'An error occurred');
  }
  return response.json();
};

export const registerUser = (userData) => 
  fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  }).then(handleResponse);

export const loginUser = (email, password) => 
  fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  }).then(handleResponse);

export const logoutUser = (token) => 
  fetch(`${API_URL}/logout`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });

export const updatePassword = (userId, oldPassword, newPassword) => 
  fetch(`${API_URL}/password`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, oldPassword, newPassword })
  }).then(handleResponse);

export const getUserProfile = (userId) => 
  fetch(`${API_URL}/users/${userId}`).then(handleResponse);

export const updateUserProfile = (userId, profileData) => 
  fetch(`${API_URL}/users/${userId}`, {
    method: 'PUT',
    body: profileData
  }).then(handleResponse);