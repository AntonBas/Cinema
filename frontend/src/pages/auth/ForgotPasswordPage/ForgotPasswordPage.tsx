import React from 'react';
import { Navigate } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout/Layout';
import { ForgotPasswordForm } from '@/components/auth/ForgotPasswordForm/ForgotPasswordForm';
import { useAuth } from '@/context/AuthContext';
import styles from './ForgotPasswordPage.module.css';

export const ForgotPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <ForgotPasswordForm />
      </div>
    </Layout>
  );
};