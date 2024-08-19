const API_URL = '/api/auth';

export const registerUser = async (userData) => {
  const response = await fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });
  return response.json();
};

export const loginUser = async (email, password) => {
  const response = await fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  return response.json();
};

export const logoutUser = async (token) => {
  await fetch(`${API_URL}/logout`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
};

export const updatePassword = async (userId, oldPassword, newPassword) => {
  const response = await fetch(`${API_URL}/password`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, oldPassword, newPassword })
  });
  return response.json();
};

export const getUserProfile = async (userId) => {
  const response = await fetch(`${API_URL}/users/${userId}`);
  return response.json();
};

export const updateUserProfile = async (userId, profileData) => {
  const response = await fetch(`${API_URL}/users/${userId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profileData)
  });
  return response.json();
};