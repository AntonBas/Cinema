import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { PublicRoute } from './PublicRoute';
import { AdminRoute } from './AdminRoute';

import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/auth/ResetPasswordPage';
import { EmailVerificationPage } from '@/pages/auth/EmailVerificationPage';
import { HomePage } from '@/pages/home/HomePage';
import { DashboardPage } from '@/pages/account/DashboardPage';
import { SecurityPage } from '@/pages/account/SecurityPage';
import { MoviesLayout, CurrentMoviesPage, UpcomingMoviesPage, MovieDetailPage } from '@/pages/movies';

import { AdminLayout } from '@/components/admin/AdminLayout/AdminLayout';
import { AdminDashboard } from '@/components/admin/AdminDashboard/AdminDashboard';
import { SectionMovies } from '@/components/admin/SectionMovies/SectionMovies';
import { SectionHalls } from '@/components/admin/SectionHalls/SectionHalls';
import { SectionSchedule } from '@/components/admin/SectionSchedule';
import { SectionUsers } from '@/components/admin/SectionUsers/SectionUsers';
import { SectionBonus } from '@/components/admin/SectionBonus/SectionBonus';
import SectionPromotion from '@/components/admin/SectionPromotion';
import { SectionTicketType } from '@/components/admin/SectionTicketType/SectionTicketType';

export const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
            <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
            <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />
            <Route path="/reset-password/:token" element={<PublicRoute><ResetPasswordPage /></PublicRoute>} />
            <Route path="/verify-email/:token" element={<EmailVerificationPage />} />

            <Route path="/account" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/account/security" element={<ProtectedRoute><SecurityPage /></ProtectedRoute>} />

            <Route
                path="/admin/*"
                element={
                    <AdminRoute>
                        <AdminLayout />
                    </AdminRoute>
                }
            >
                <Route index element={<Navigate to="dashboard" replace />} />
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="movies" element={<SectionMovies />} />
                <Route path="halls" element={<SectionHalls />} />
                <Route path="schedule" element={<SectionSchedule />} />
                <Route path="users" element={<SectionUsers />} />
                <Route path="bonus" element={<SectionBonus />} />
                <Route path="promotion" element={<SectionPromotion />} />
                <Route path="ticket-type" element={<SectionTicketType />} />
            </Route>

            <Route path="/movies" element={<MoviesLayout />}>
                <Route path="current" element={<CurrentMoviesPage />} />
                <Route path="upcoming" element={<UpcomingMoviesPage />} />
                <Route index element={<CurrentMoviesPage />} />
            </Route>

            <Route path="/movies/:slug" element={<MovieDetailPage />} />

            <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};