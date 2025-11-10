import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

interface AdminRouteProps {
    children: React.ReactNode;
}

const centerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
};

export const AdminRoute: React.FC<AdminRouteProps> = ({ children }) => {
    const { token, user, isLoading } = useAuth();
    const showLoading = useDelayedLoading(isLoading, 300);

    if (isLoading || showLoading) {
        return (
            <div style={centerStyle}>
                <LoadingSpinner text="Verifying admin access..." />
            </div>
        );
    }

    if (!token) return <Navigate to="/login" replace />;
    if (user?.userRole !== 'ROLE_ADMIN') return <Navigate to="/" replace />;

    return <>{children}</>;
};
