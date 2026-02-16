import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { LoadingSpinner } from '@/components/ui';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

const centerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
};

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();
    const showLoading = useDelayedLoading(loading, { delay: 300, minDisplayTime: 500 });

    if (showLoading) {
        return (
            <div style={centerStyle}>
                <LoadingSpinner text="Loading..." />
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
};