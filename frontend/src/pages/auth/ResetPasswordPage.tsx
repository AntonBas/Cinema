import React from 'react';
import { ResetPasswordForm } from '../../components/auth/ResetPasswordForm';
import { useAuth } from '../../context/AuthContext';
import { Layout } from '../../components/layout/Layout';


export const ResetPasswordPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div>
        <ResetPasswordForm />
      </div>
    </Layout>
  );
};