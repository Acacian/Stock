const API_URL = '/api/newsfeed';

export const fetchNewsfeed = async (userId) => {
  try {
    const response = await fetch(`${API_URL}/${userId}`);
    if (!response.ok) {
      throw new Error('Failed to fetch newsfeed');
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching newsfeed:', error);
    throw error;
  }
};