import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './LoginForm.css';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login, token, user } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      console.log('LoginForm: Starting login...');
      await login(email, password);
      console.log('LoginForm: Login successful, token:', token, 'user:', user);

      navigate('/');

    } catch (err: any) {
      console.error('LoginForm: Login error:', err);
      setError(err.response?.data?.message || err.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <section className="login">
      <div className="login-container">
        <h1 className="login-title">Login into your account</h1>

        <div className="login-top">
          <span className="login-top-text">Don't have an account?</span>
          <a href="/register">Register</a>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}

          <div className="login-middle-email">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="email"
              required
              disabled={isLoading}
            />
          </div>

          <div className="login-middle-password">
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="password"
              required
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            className="login-bottom-button"
            disabled={isLoading}
          >
            {isLoading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="login-bottom">
          <a href="/forgot-password">Forgot your password?</a>
        </div>
      </div>
    </section>
  );
};