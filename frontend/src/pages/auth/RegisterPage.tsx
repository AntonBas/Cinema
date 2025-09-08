import React from 'react';
import { Header } from '../../components/layout/Header';
import { RegisterForm } from '../../components/auth/RegisterForm';
import { useAuth } from '../../context/AuthContext';

export const RegisterPage: React.FC = () => {
    const { token, isLoading } = useAuth();
    
    if (token && !isLoading) {
        window.location.href = '/';
        return null;
    }

   return (
    <div>
      <Header />
      <div>
        <RegisterForm />
      </div>
    </div>
  );
};
