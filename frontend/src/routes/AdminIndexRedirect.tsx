import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { getAdminMenuItems } from "@/components/admin/AdminLayout/adminMenuItems";

export const AdminIndexRedirect: React.FC = () => {
  const { user } = useAuth();
  const [firstSection] = getAdminMenuItems(user?.userRole ?? "");

  return <Navigate to={firstSection?.path ?? "/"} replace />;
};
