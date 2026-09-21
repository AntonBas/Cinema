import React from "react";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";

interface EmptyStateProps {
  title: string;
  message: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ title, message }) => (
  <div className={tableStyles.empty}>
    <h3>{title}</h3>
    <p>{message}</p>
  </div>
);
