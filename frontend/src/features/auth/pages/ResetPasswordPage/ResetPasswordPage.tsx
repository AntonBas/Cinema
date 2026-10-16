import React from "react";
import { Navigate } from "react-router-dom";
import { ResetPasswordForm } from "@/features/auth/components/ResetPasswordForm/ResetPasswordForm";
import { useAuth } from "@/context/AuthContext";
import { AuthPageLayout } from "@/features/auth/components/AuthPageLayout/AuthPageLayout";

export const ResetPasswordPage: React.FC = () => {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  return (
    <AuthPageLayout>
      <ResetPasswordForm />
    </AuthPageLayout>
  );
};
