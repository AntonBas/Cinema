import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { LoadingSpinner } from '@/components/ui';

interface PublicRouteProps {
    children: React.ReactNode;
}

const centerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
};

export const PublicRoute: React.FC<PublicRouteProps> = ({ children }) => {
    const { token, isLoading } = useAuth();
    const showLoading = useDelayedLoading(isLoading, 300);

    if (showLoading) {
        return (
            <div style={centerStyle}>
                <LoadingSpinner text="Loading..." />
            </div>
        );
    }

    return !token ? <>{children}</> : <Navigate to="/" replace />;
};