const API_URL = process.env.REACT_APP_API_GATEWAY_URL || 'https://localhost:8081';

export const checkAuthStatus = async () => {
  const response = await fetch(`${API_URL}/api/auth/check`, {
    method: 'GET',
    credentials: 'include',  // This is important for sending cookies
  });

  if (!response.ok) {
    throw new Error('Auth check failed');
  }

  return response.json();
};

export const login = async (credentials) => {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credentials),
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Login failed');
  }

  return response.json();
};

export const logout = async () => {
  const response = await fetch(`${API_URL}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Logout failed');
  }

  return response.json();
};