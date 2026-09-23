import React from 'react';
import { AuthPageLayout } from '@/components/auth/AuthPageLayout/AuthPageLayout';
import { LoginForm } from '@/components/auth/LoginForm/LoginForm';

export const LoginPage: React.FC = () => {
  return (
    <AuthPageLayout>
      <LoginForm />
    </AuthPageLayout>
  );
};