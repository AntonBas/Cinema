import React from "react";
import { Navigate } from "react-router-dom";
import { RegisterForm } from "@/components/auth/RegisterForm/RegisterForm";
import { useAuth } from "@/context/AuthContext";
import { AuthPageLayout } from "@/components/auth/AuthPageLayout/AuthPageLayout";

export const RegisterPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <AuthPageLayout>
      <RegisterForm />
    </AuthPageLayout>
  );
};
