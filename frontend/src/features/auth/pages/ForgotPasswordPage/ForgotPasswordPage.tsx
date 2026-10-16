import React from "react";
import { Navigate } from "react-router-dom";
import { AuthPageLayout } from "@/features/auth/components/AuthPageLayout/AuthPageLayout";
import { ForgotPasswordForm } from "@/features/auth/components/ForgotPasswordForm/ForgotPasswordForm";
import { useAuth } from "@/context/AuthContext";

export const ForgotPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <AuthPageLayout>
      <ForgotPasswordForm />
    </AuthPageLayout>
  );
};
