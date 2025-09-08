import React from 'react';
import { Header } from '../../components/layout/Header';
import { ResetPasswordForm } from '../../components/auth/ResetPasswordForm';
import { useAuth } from '../../context/AuthContext';

export const ResetPasswordPage: React.FC = () => {
  const { token, isLoading } = useAuth();
  
  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <div>
      <Header />
      <div>
        <ResetPasswordForm />
      </div>
    </div>
  );
};