import React from "react";
import { AuthPageLayout } from "@/features/auth/components/AuthPageLayout/AuthPageLayout";
import { LoginForm } from "@/features/auth/components/LoginForm/LoginForm";

export const LoginPage: React.FC = () => {
  return (
    <AuthPageLayout>
      <LoginForm />
    </AuthPageLayout>
  );
};
