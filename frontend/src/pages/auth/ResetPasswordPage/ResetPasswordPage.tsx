import React from 'react';
import { ResetPasswordForm } from '@/components/auth/ResetPasswordForm/ResetPasswordForm';
import { useAuth } from '@/hooks/features/auth';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './ResetPasswordPage.module.css';

export const ResetPasswordPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
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