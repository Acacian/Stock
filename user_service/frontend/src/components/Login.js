import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const success = await login(credentials.email, credentials.password);
      if (success) {
        navigate('/profile');
      } else {
        setError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해 주세요.');
      }
    } catch (error) {
      console.error('Login error:', error);
      if (error.response) {
        setError(error.response.data || '로그인 중 오류가 발생했습니다.');
      } else if (error.request) {
        setError('서버로부터 응답을 받지 못했습니다. 나중에 다시 시도해 주세요.');
      } else {
        setError('예기치 않은 오류가 발생했습니다. 다시 시도해 주세요.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <input
        type="email"
        name="email"
        value={credentials.email}
        onChange={handleChange}
        placeholder="Email"
        required
      />
      <input
        type="password"
        name="password"
        value={credentials.password}
        onChange={handleChange}
        placeholder="Password"
        required
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};

export default Login;