import React from 'react';
import { RegisterForm } from '@/components/auth/RegisterForm/RegisterForm';
import { useAuth } from '@/hooks/features/auth';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './RegisterPage.module.css';

export const RegisterPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
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