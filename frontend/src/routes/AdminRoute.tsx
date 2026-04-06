import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';

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
    const { user, loading, isAuthenticated } = useAuth();
    const showLoading = useDelayedLoading(loading, { delay: 300, minDisplayTime: 500 });

    if (showLoading) {
        return (
            <div style={centerStyle}>
                <LoadingSpinner text="Verifying admin access..." />
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

    if (user.userRole !== 'ROLE_ADMIN') {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};