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
    <div style={{
      backgroundColor: '#12161f',
      color: '#fff',
      minHeight: '100vh',
      fontFamily: 'Arial, sans-serif',
      margin: 0,
      padding: 0
    }}>
      <Header />
      <div style={{ paddingTop: '80px' }}>
        <LoginForm />
      </div>
    </div>
  );
};
