import React from 'react';
import { ResetPasswordForm } from '@/components/auth/ResetPasswordForm/ResetPasswordForm';
import { useAuth } from '@/hooks/features/auth/useAuth';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './ResetPasswordPage.module.css';

export const ResetPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (user && !loading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div className={styles.container}>
        <ResetPasswordForm />
      </div>
    </Layout>
  );
};