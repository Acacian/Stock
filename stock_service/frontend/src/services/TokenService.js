const TokenService = {
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  },
  getAccessToken: () => {
    return localStorage.getItem('accessToken');
  },
  getRefreshToken: () => {
    return localStorage.getItem('refreshToken');
  },
  removeTokens: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }
};

export default TokenService;