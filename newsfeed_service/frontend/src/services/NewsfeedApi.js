import { API_URL } from '../config';

export const fetchNewsfeed = async (userId) => {
  const response = await fetch(`${API_URL}/newsfeed/${userId}`, {
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch newsfeed');
  }
  return response.json();
};