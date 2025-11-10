import React, { useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { LoginForm } from '@/components/auth/LoginForm/LoginForm';
import styles from './LoginPage.module.css';

export const LoginPage: React.FC = () => {
  useEffect(() => {
    document.body.classList.add(styles.loginPageBody);
    return () => {
      document.body.classList.remove(styles.loginPageBody);
    };
  }, []);

  return (
    <Layout>
      <div className={styles.authMainContent}>
        <LoginForm />
      </div>
    </Layout>
  );
};