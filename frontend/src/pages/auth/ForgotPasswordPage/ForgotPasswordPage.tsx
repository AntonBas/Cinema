import React from 'react';
import { Layout } from '@/components/layout/Layout';
import { ForgotPasswordForm } from '@/components/auth/ForgotPasswordForm/ForgotPasswordForm';
import { useAuth } from '@/hooks/features/auth';
import styles from './ForgotPasswordPage.module.css';

export const ForgotPasswordPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
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