const API_URL = process.env.REACT_APP_API_GATEWAY_URL || 'https://localhost:8081';

export const checkAuthStatus = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    throw new Error('No token found');
  }

  const response = await fetch(`${API_URL}/api/auth/check`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error('Auth check failed');
  }

  return response.json();
};