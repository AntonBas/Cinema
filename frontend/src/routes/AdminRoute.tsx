import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";

interface AdminRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

const centerStyle = {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  height: "100vh",
  background: "linear-gradient(135deg, #0c0c0c, #1a1a1a)",
};

export const AdminRoute: React.FC<AdminRouteProps> = ({
  children,
  allowedRoles = ["ROLE_ADMIN", "ROLE_CONTENT_MANAGER", "ROLE_CASHIER"],
}) => {
  const { user, loading, isAuthenticated } = useAuth();
  const showLoading = useDelayedLoading(loading, {
    delay: 300,
    minDisplayTime: 500,
  });

  if (showLoading) {
    return (
      <div style={centerStyle}>
        <LoadingSpinner text="Verifying access..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!user) {
    return (
      <div style={centerStyle}>
        <LoadingSpinner text="Loading user data..." />
      </div>
    );
  }

  const hasAccess = allowedRoles.includes(user.userRole);

  if (!hasAccess) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};
