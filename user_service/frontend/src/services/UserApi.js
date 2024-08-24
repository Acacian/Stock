const API_URL = process.env.REACT_APP_AUTH_URL || '/api/auth';

const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const handleResponse = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      // Auto logout if 401 response returned from api
      localStorage.removeItem('token');
      window.location.reload(true);
      throw new Error('Unauthorized access');
    }

    const error = await response.text();
    throw new Error(error || response.statusText);
  }
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.indexOf("application/json") !== -1) {
    return response.json();
  }
  return response.text();
};

export const registerUser = (userData) => {
  return fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
    },
    body: JSON.stringify(userData),
  }).then(handleResponse);
};

export const loginUser = (email, password) => {
  return fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  }).then(handleResponse);
};

export const logoutUser = () => {
  return fetch(`${API_URL}/logout`, {
    method: 'POST',
    headers: getAuthHeader(),
  }).then(handleResponse);
};

export const getUserProfile = (userId) => {
  return fetch(`${API_URL}/users/${userId}`, {
    headers: getAuthHeader(),
  }).then(handleResponse);
};

export const updateUserProfile = (userId, profileData) => {
  return fetch(`${API_URL}/users/${userId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
    },
    body: JSON.stringify(profileData),
  }).then(handleResponse);
};

export const updatePassword = (userId, oldPassword, newPassword) => {
  return fetch(`${API_URL}/password`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
    },
    body: JSON.stringify({ userId, oldPassword, newPassword }),
  }).then(handleResponse);
};