import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/auth/ResetPasswordPage';
import { HomePage } from '@/pages/home/HomePage';
import { EmailVerificationPage } from '@/pages/auth/EmailVerificationPage';
import { AdminPage } from '@/pages/admin/AdminPage';
import { DashboardPage } from '@/pages/account/DashboardPage';
import { SecurityPage } from '@/pages/account/SecurityPage';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, isLoading } = useAuth();
  const showLoading = useDelayedLoading(isLoading, 300);

  if (showLoading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #0c0c0c, #1a1a1a)'
      }}>
        <LoadingSpinner text="Checking auth..." />
      </div>
    );
  }

  return token ? <>{children}</> : <Navigate to="/login" replace />;
};

const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, isLoading } = useAuth();
  const showLoading = useDelayedLoading(isLoading, 300);

  if (showLoading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #0c0c0c, #1a1a1a)'
      }}>
        <LoadingSpinner text="Checking session..." />
      </div>
    );
  }

  return !token ? <>{children}</> : <Navigate to="/" replace />;
};

const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, user, isLoading } = useAuth();
  const showLoading = useDelayedLoading(isLoading, 300);

  if (showLoading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #0c0c0c, #1a1a1a)'
      }}>
        <LoadingSpinner text="Verifying admin..." />
      </div>
    );
  }

  return token && user?.userRole === 'ROLE_ADMIN' ? <>{children}</> : <Navigate to="/" replace />;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } />
        <Route path="/register" element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        } />
        <Route path="/forgot-password" element={
          <PublicRoute>
            <ForgotPasswordPage />
          </PublicRoute>
        } />
        <Route path="/reset-password/:token" element={
          <PublicRoute>
            <ResetPasswordPage />
          </PublicRoute>
        } />

        <Route path="/verify-email/:token" element={<EmailVerificationPage />} />

        <Route path="/account" element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        } />

        <Route path="/account/security" element={
          <ProtectedRoute>
            <SecurityPage />
          </ProtectedRoute>
        } />

        <Route path="/admin/*" element={
          <AdminRoute>
            <AdminPage />
          </AdminRoute>
        } />

        <Route path="/" element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;