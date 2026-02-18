import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { PublicRoute } from './PublicRoute';
import { AdminRoute } from './AdminRoute';

import { LoginPage } from '@/pages/auth/LoginPage/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage/RegisterPage';
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/auth/ResetPasswordPage/ResetPasswordPage';
import { EmailVerificationPage } from '@/pages/auth/EmailVerificationPage/EmailVerificationPage';
import { ConfirmEmailChangePage } from '@/pages/auth/ConfirmEmailChangePage/ConfirmEmailChangePage';

import { HomePage } from '@/pages/home/HomePage';
import { ProfilePage } from '@/pages/account/ProfilePage/ProfilePage';
import { SecurityPage } from '@/pages/account/SecurityPage/SecurityPage';
import { BonusPage } from '@/pages/account/BonusPage/BonusPage';
import { TicketsPage } from '@/pages/account/TicketsPage/TicketsPage';

import { MoviesLayout } from '@/pages/movies/MoviesLayout/MoviesLayout';
import { CurrentMoviesPage } from '@/pages/movies/CurrentMoviesPage/CurrentMoviesPage';
import { UpcomingMoviesPage } from '@/pages/movies/UpcomingMoviesPage/UpcomingMoviesPage';
import { MovieDetailPage } from '@/pages/movies/MovieDetailPage/MovieDetailPage';

import SessionsPage from '@/pages/sessions/SessionsPage';
import { BookingPage } from '@/pages/booking/BookingPage/BookingPage';
import { BookingSummaryPage } from '@/pages/booking/BookingSummaryPage/BookingSummaryPage';
import { PaymentPage } from '@/pages/booking/PaymentPage/PaymentPage';
import SuccessPage from '@/pages/booking/SuccessPage/SuccessPage';

import { AdminLayout } from '@/components/admin/AdminLayout/AdminLayout';
import { AdminDashboard } from '@/components/admin/AdminDashboard/AdminDashboard';
import { SectionMovies } from '@/components/admin/SectionMovies/SectionMovies';
import { SectionHalls } from '@/components/admin/SectionHalls/SectionHalls';
import { SectionSchedule } from '@/components/admin/SectionSchedule/SectionSchedule';
import { SectionUsers } from '@/components/admin/SectionUsers/SectionUsers';
import SectionBonus from '@/components/admin/SectionBonus/SectionBonus';
import SectionPromotion from '@/components/admin/SectionPromotion/SectionPromotion';
import SectionTicketType from '@/components/admin/SectionTicketType/SectionTicketType';

export const AppRoutes: React.FC = () => {
    return (
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

            <Route path="/confirm-email-change/:token" element={<ConfirmEmailChangePage />} />

            <Route path="/account" element={
                <ProtectedRoute>
                    <ProfilePage />
                </ProtectedRoute>
            } />

            <Route path="/account/security" element={
                <ProtectedRoute>
                    <SecurityPage />
                </ProtectedRoute>
            } />

            <Route path="/account/bonuses" element={
                <ProtectedRoute>
                    <BonusPage />
                </ProtectedRoute>
            } />

            <Route path="/account/tickets" element={
                <ProtectedRoute>
                    <TicketsPage />
                </ProtectedRoute>
            } />

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

            <Route path="/schedule" element={
                <ProtectedRoute>
                    <SessionsPage />
                </ProtectedRoute>
            } />

            <Route path="/booking/:sessionId" element={
                <ProtectedRoute>
                    <BookingPage />
                </ProtectedRoute>
            } />

            <Route path="/booking/summary/:bookingId" element={
                <ProtectedRoute>
                    <BookingSummaryPage />
                </ProtectedRoute>
            } />

            <Route path="/booking/payment/:bookingId" element={
                <ProtectedRoute>
                    <PaymentPage />
                </ProtectedRoute>
            } />

            <Route path="/booking/success" element={
                <ProtectedRoute>
                    <SuccessPage />
                </ProtectedRoute>
            } />

            <Route path="/" element={
                <ProtectedRoute>
                    <HomePage />
                </ProtectedRoute>
            } />

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};