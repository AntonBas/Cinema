import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Input, Button } from '@/components/ui';
import { FcGoogle } from 'react-icons/fc';
import styles from './LoginForm.module.css';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login, loginWithGoogle, isAuthenticating } = useAuthActions();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);
    setIsSubmitting(true);

    try {
      await login({ email, password });
    } catch (err: any) {
      setLocalError(err.message || 'Login failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleGoogleLogin = (e: React.MouseEvent) => {
    e.preventDefault();
    loginWithGoogle();
  };

  const isLoading = isSubmitting || isAuthenticating;

  return (
    <section className={styles.login}>
      <div className={styles.loginContainer}>
        <h1 className={styles.loginTitle}>Login into your account</h1>

        <div className={styles.loginTop}>
          <span className={styles.loginTopText}>Don't have an account?</span>
          <Link to="/register">Register</Link>
        </div>

        <form className={styles.loginForm} onSubmit={handleSubmit}>
          {localError && (
            <div className={styles.notification} data-type="error">
              {localError}
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

        <div className={styles.divider}>
          <span className={styles.dividerText}>or</span>
        </div>

        <Button
          type="button"
          variant="outline"
          size="large"
          onClick={handleGoogleLogin}
          disabled={isLoading}
          style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          <FcGoogle size={20} />
          Continue with Google
        </Button>

        <div className={styles.loginBottom}>
          <Link to="/forgot-password">Forgot your password?</Link>
        </div>
      </div>
    </section>
  );
};