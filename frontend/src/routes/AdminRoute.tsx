import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth';
import { LoadingSpinner } from '@/components/ui';

interface AdminRouteProps {
    children: React.ReactNode;
}

const centerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    background: 'linear-gradient(135deg, #0c0c0c, #1a1a1a)'
};

export const AdminRoute: React.FC<AdminRouteProps> = ({ children }) => {
    const { token, user, isLoading } = useAuth();

    if (isLoading) {
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