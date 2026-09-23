import React from "react";
import { Navigate, useSearchParams } from "react-router-dom";
import { getRedirectFromSearch } from "@/utils/authRedirect";
import { useAuth } from "@/context/AuthContext";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";

interface PublicRouteProps {
  children: React.ReactNode;
}

const centerStyle = {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  height: "100vh",
};

export const PublicRoute: React.FC<PublicRouteProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const [searchParams] = useSearchParams();
  const showLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  if (showLoading) {
    return (
      <div style={centerStyle}>
        <LoadingSpinner text="Loading..." />
      </div>
    );
  }

  if (loading) {
    return null;
  }

  if (isAuthenticated) {
    return <Navigate to={getRedirectFromSearch(searchParams)} replace />;
  }

  return <>{children}</>;
};
