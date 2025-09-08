import React from 'react';
import { Header } from '../../components/layout/Header';
import { LoginForm } from '../../components/auth/LoginForm';
import { useAuth } from '../../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <div>
      <Header />
      <div>
        <LoginForm />
      </div>
    </div>
  );
};
