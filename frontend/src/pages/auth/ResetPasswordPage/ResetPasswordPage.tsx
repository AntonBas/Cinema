import React from 'react';
import { Navigate } from 'react-router-dom';
import { ResetPasswordForm } from '@/components/auth/ResetPasswordForm/ResetPasswordForm';
import { useAuth } from '@/context/AuthContext';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './ResetPasswordPage.module.css';

export const ResetPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <ResetPasswordForm />
      </div>
    </Layout>
  );
};