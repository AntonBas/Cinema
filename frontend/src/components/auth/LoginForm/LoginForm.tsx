import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Input, Button } from '@/components/ui';
import { Chrome } from 'lucide-react';
import styles from './LoginForm.module.css';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const { loading, error, login, loginWithGoogle } = useAuthActions();

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    await login({ email, password });
  };

  const handleGoogleLogin = (e: React.MouseEvent) => {
    e.preventDefault();
    loginWithGoogle();
  };

  return (
    <section className={styles.login}>
      <div className={styles.loginContainer}>
        <h1 className={styles.loginTitle}>Login into your account</h1>

        <div className={styles.loginTop}>
          <span>Don't have an account?</span>
          <Link to="/register">Register</Link>
        </div>

        <form className={styles.loginForm} onSubmit={handleSubmit}>
          {error && (
            <div className={styles.notification} data-type="error">
              {error.message}
            </div>
          )}

          <Input
            type="email"
            value={email}
            onChange={setEmail}
            placeholder="Email address"
            disabled={loading}
            required
          />

          <Input
            type="password"
            value={password}
            onChange={setPassword}
            placeholder="Password"
            disabled={loading}
            required
          />

          <Button 
            type="submit" 
            variant="primary" 
            size="large" 
            loading={loading} 
            disabled={loading} 
            style={{ width: '100%', marginTop: '1rem' }}
          >
            {loading ? 'Logging in...' : 'Login'}
          </Button>
        </form>

        <div className={styles.divider}>
          <span className={styles.dividerText}>or</span>
        </div>

        <Button
          type="button"
          variant="outline"
          size="large"
          onClick={handleGoogleLogin}
          disabled={loading}
          style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          <Chrome size={20} />
          Continue with Google
        </Button>

        <div className={styles.loginBottom}>
          <Link to="/forgot-password">Forgot your password?</Link>
        </div>
      </div>
    </section>
  );
};