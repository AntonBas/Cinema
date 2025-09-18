import React from 'react';
import { RegisterForm } from '../../components/auth/RegisterForm';
import { useAuth } from '../../context/AuthContext';
import { Layout } from '../../components/layout/Layout';

export const RegisterPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div>
        <RegisterForm />
      </div>
    </Layout>
  );
};
