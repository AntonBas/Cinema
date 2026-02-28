import React from 'react';
import { RegisterForm } from '@/components/auth/RegisterForm/RegisterForm';
import { useAuth } from '@/contexts/AuthContext';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './RegisterPage.module.css';

export const RegisterPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (user && !loading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <RegisterForm />
      </div>
    </Layout>
  );
};