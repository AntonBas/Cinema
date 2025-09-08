import React from 'react';
import { Header } from '../../components/layout/Header';
import { ForgotPasswordForm } from '../../components/auth/ForgotPasswordForm';
import { useAuth } from '../../context/AuthContext';

export const ForgotPasswordPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <div>
      <Header />
      <div>
        <ForgotPasswordForm />
      </div>
    </div>
  );
};