import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth, useAuthMutation } from '@/hooks/features/auth';
import { Input, Button } from '@/components/ui';
import styles from './LoginForm.module.css';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login: authLogin } = useAuth();
  const { login, isLoading, error } = useAuthMutation();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const response = await login({ email, password });
      authLogin(response);
      navigate('/');
    } catch (err) {
    }
  };

  return (
    <section className={styles.login}>
      <div className={styles.loginContainer}>
        <h1 className={styles.loginTitle}>Login into your account</h1>

        <div className={styles.loginTop}>
          <span className={styles.loginTopText}>Don't have an account?</span>
          <Link to="/register">Register</Link>
        </div>

        <form className={styles.loginForm} onSubmit={handleSubmit}>
          {error && (
            <div className={styles.notification} data-type="error">
              {error}
            </div>
          )}

          <Input
            type="email"
            value={email}
            onChange={setEmail}
            placeholder="Email address"
            disabled={isLoading}
          />

          <Input
            type="password"
            value={password}
            onChange={setPassword}
            placeholder="Password"
            disabled={isLoading}
          />

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={isLoading}
            disabled={isLoading}
            style={{ width: '100%', marginTop: '1rem' }}
          >
            {isLoading ? 'Logging in...' : 'Login'}
          </Button>
        </form>

        <div className={styles.loginBottom}>
          <Link to="/forgot-password">Forgot your password?</Link>
        </div>
      </div>
    </section>
  );
};