import React from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { LoginForm } from '@/components/auth/LoginForm/LoginForm';
import styles from './LoginPage.module.css';

export const LoginPage: React.FC = () => {
  return (
    <Layout>
      <div className={styles.container}>
        <LoginForm />
      </div>
    </Layout>
  );
};