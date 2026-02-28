import React from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { ForgotPasswordForm } from '@/components/auth/ForgotPasswordForm/ForgotPasswordForm';
import { useAuth } from '@/contexts/AuthContext';
import styles from './ForgotPasswordPage.module.css';

export const ForgotPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (user && !loading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <ForgotPasswordForm />
      </div>
    </Layout>
  );
};