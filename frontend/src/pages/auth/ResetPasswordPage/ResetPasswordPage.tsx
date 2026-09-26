import React from "react";
import { Navigate } from "react-router-dom";
import { ResetPasswordForm } from "@/components/auth/ResetPasswordForm/ResetPasswordForm";
import { useAuth } from "@/context/AuthContext";
import { AuthPageLayout } from "@/components/auth/AuthPageLayout/AuthPageLayout";

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
