const API_URL = process.env.REACT_APP_NEWSFEED_API_URL || '/api/newsfeed';

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'An error occurred');
  }
  return response.json();
};

export const fetchNewsfeed = (userId) =>
  fetch(`${API_URL}/${userId}`).then(handleResponse);