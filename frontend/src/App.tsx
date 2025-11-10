import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { PublicRoute } from '@/routes/PublicRoute';
import { AdminRoute } from '@/routes/AdminRoute';

import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/auth/ResetPasswordPage';
import { EmailVerificationPage } from '@/pages/auth/EmailVerificationPage';
import { HomePage } from '@/pages/home/HomePage';
import { AdminPage } from '@/pages/admin/AdminPage';
import { DashboardPage } from '@/pages/account/DashboardPage';
import { SecurityPage } from '@/pages/account/SecurityPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
        <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />
        <Route path="/reset-password/:token" element={<PublicRoute><ResetPasswordPage /></PublicRoute>} />
        <Route path="/verify-email/:token" element={<EmailVerificationPage />} />

        <Route path="/account" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/account/security" element={<ProtectedRoute><SecurityPage /></ProtectedRoute>} />

        <Route path="/admin/*" element={<AdminRoute><AdminPage /></AdminRoute>} />

        <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
