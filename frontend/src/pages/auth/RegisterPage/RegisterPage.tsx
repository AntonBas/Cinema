import React from 'react';
import { Navigate } from 'react-router-dom';
import { RegisterForm } from '@/components/auth/RegisterForm/RegisterForm';
import { useAuth } from '@/context/AuthContext';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './RegisterPage.module.css';

export const RegisterPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <RegisterForm />
      </div>
    </Layout>
  );
};