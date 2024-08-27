import React, { useState } from 'react';
import { registerUser } from '../services/UserApi';
import { useNavigate } from 'react-router-dom';

const Register = () => {
  const [userData, setUserData] = useState({
    name: '',
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setUserData({ ...userData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    console.log('Sending registration data:', userData);
    try {
      await registerUser(userData);
      alert('회원가입이 성공적으로 완료되었습니다. 이메일을 확인해 주세요.');
      navigate('/login');
    } catch (error) {
      console.error('Registration error:', error);
      if (error.response) {
        // 서버에서 응답을 받은 경우
        setError(error.response.data.message || '회원가입에 실패했습니다. 다시 시도해 주세요.');
      } else if (error.request) {
        // 요청이 전송되었지만 응답을 받지 못한 경우
        setError('서버에 연결할 수 없습니다. 네트워크 연결을 확인해 주세요.');
      } else {
        // 요청 설정 중 오류가 발생한 경우
        setError('예기치 않은 오류가 발생했습니다. 다시 시도해 주세요.');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Register</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <input
        type="text"
        name="name"
        value={userData.name}
        onChange={handleChange}
        placeholder="Name"
        required
      />
      <input
        type="email"
        name="email"
        value={userData.email}
        onChange={handleChange}
        placeholder="Email"
        required
      />
      <input
        type="password"
        name="password"
        value={userData.password}
        onChange={handleChange}
        placeholder="Password"
        required
      />
      <button type="submit">Register</button>
    </form>
  );
};

export default Register;