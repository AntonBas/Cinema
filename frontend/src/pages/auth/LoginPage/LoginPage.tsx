import React, { useEffect } from 'react';
import { Layout } from '@/components/layout/Layout';
import { LoginForm } from '@/components/auth/LoginForm/LoginForm';
import { useAuth } from '@/hooks/features/auth';
import styles from './LoginPage.module.css';

export const LoginPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  useEffect(() => {
    document.body.classList.add(styles.loginPageBody);
    return () => {
      document.body.classList.remove(styles.loginPageBody);
    };
  }, []);

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div className={styles.authMainContent}>
        <LoginForm />
      </div>
    </Layout>
  );
};