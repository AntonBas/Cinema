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
        <ForgotPasswordForm />
      </div>
    </div>
  );
};