import React, { useEffect } from 'react';
import { Layout } from '../../components/layout/Layout';
import { LoginForm } from '../../components/auth/LoginForm';
import { useAuth } from '../../context/AuthContext';
import './LoginPage.css';

export const LoginPage: React.FC = () => {
  const { token, isLoading } = useAuth();

  useEffect(() => {
    document.body.classList.add('login-page-body');
    return () => {
      document.body.classList.remove('login-page-body');
    };
  }, []);

  if (token && !isLoading) {
    window.location.href = '/';
    return null;
  }

  return (
    <Layout>
      <div className="auth-main-content">
        <LoginForm />
      </div>
    </Layout>
  );
};
