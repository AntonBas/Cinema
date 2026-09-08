import React, { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";
import { PublicRoute } from "./PublicRoute";
import { AdminRoute } from "./AdminRoute";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";

import { LoginPage } from "@/pages/auth/LoginPage/LoginPage";
import { RegisterPage } from "@/pages/auth/RegisterPage/RegisterPage";
import { ForgotPasswordPage } from "@/pages/auth/ForgotPasswordPage/ForgotPasswordPage";
import { ResetPasswordPage } from "@/pages/auth/ResetPasswordPage/ResetPasswordPage";
import { EmailVerificationPage } from "@/pages/auth/EmailVerificationPage/EmailVerificationPage";
import { ConfirmEmailChangePage } from "@/pages/auth/ConfirmEmailChangePage/ConfirmEmailChangePage";
import { OAuth2Redirect } from "@/components/auth/OAuth2Redirect/OAuth2Redirect";

import { HomePage } from "@/pages/home/HomePage";
import { ProfilePage } from "@/pages/account/ProfilePage/ProfilePage";
import { SecurityPage } from "@/pages/account/SecurityPage/SecurityPage";
import { BonusPage } from "@/pages/account/BonusPage/BonusPage";
import { TicketsPage } from "@/pages/account/TicketsPage/TicketsPage";
import { RefundPolicyPage } from "@/pages/RefundPolicyPage/RefundPolicyPage";

import { MoviesLayout } from "@/pages/movies/MoviesLayout/MoviesLayout";
import { CurrentMoviesPage } from "@/pages/movies/CurrentMoviesPage/CurrentMoviesPage";
import { UpcomingMoviesPage } from "@/pages/movies/UpcomingMoviesPage/UpcomingMoviesPage";
import { MovieDetailPage } from "@/pages/movies/MovieDetailPage/MovieDetailPage";

import SessionsPage from "@/pages/sessions/SessionsPage";
import { BookingPage } from "@/pages/booking/BookingPage/BookingPage";
import { BookingSummaryPage } from "@/pages/booking/BookingSummaryPage/BookingSummaryPage";
import { PaymentPage } from "@/pages/booking/PaymentPage/PaymentPage";
import SuccessPage from "@/pages/booking/SuccessPage/SuccessPage";

const AdminLayout = lazy(() =>
  import("@/components/admin/AdminLayout/AdminLayout").then((m) => ({ default: m.AdminLayout })),
);
const SectionMovies = lazy(() =>
  import("@/components/admin/SectionMovies/SectionMovies").then((m) => ({ default: m.SectionMovies })),
);
const SectionHalls = lazy(() =>
  import("@/components/admin/SectionHalls/SectionHalls").then((m) => ({ default: m.SectionHalls })),
);
const SectionSchedule = lazy(() =>
  import("@/components/admin/SectionSchedule/SectionSchedule").then((m) => ({ default: m.SectionSchedule })),
);
const SectionUsers = lazy(() =>
  import("@/components/admin/SectionUsers/SectionUsers").then((m) => ({ default: m.SectionUsers })),
);
const SectionBonus = lazy(() => import("@/components/admin/SectionBonus/SectionBonus"));
const SectionPromotion = lazy(() => import("@/components/admin/SectionPromotion/SectionPromotion"));
const SectionTicketType = lazy(() => import("@/components/admin/SectionTicketType/SectionTicketType"));
const SectionAuditLogs = lazy(() =>
  import("@/components/admin/SectionAuditLogs/SectionAuditLogs").then((m) => ({ default: m.SectionAuditLogs })),
);

const CashierScanPage = lazy(() =>
  import("@/pages/cashier/CashierScanPage").then((m) => ({ default: m.CashierScanPage })),
);

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />

      <Route
        path="/register"
        element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        }
      />

      <Route
        path="/forgot-password"
        element={
          <PublicRoute>
            <ForgotPasswordPage />
          </PublicRoute>
        }
      />

      <Route
        path="/reset-password/:token"
        element={
          <PublicRoute>
            <ResetPasswordPage />
          </PublicRoute>
        }
      />

      <Route path="/verify-email/:token" element={<EmailVerificationPage />} />
      <Route
        path="/confirm-email-change/:token"
        element={<ConfirmEmailChangePage />}
      />
      <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />

      <Route path="/" element={<HomePage />} />

      <Route path="/movies" element={<MoviesLayout />}>
        <Route path="current" element={<CurrentMoviesPage />} />
        <Route path="upcoming" element={<UpcomingMoviesPage />} />
        <Route index element={<CurrentMoviesPage />} />
      </Route>
      <Route path="/movies/:slug" element={<MovieDetailPage />} />

      <Route path="/schedule" element={<SessionsPage />} />

      <Route path="/refund-policy" element={<RefundPolicyPage />} />

      <Route
        path="/account"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/account/security"
        element={
          <ProtectedRoute>
            <SecurityPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/account/bonuses"
        element={
          <ProtectedRoute>
            <BonusPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/account/tickets"
        element={
          <ProtectedRoute>
            <TicketsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/booking/:sessionId"
        element={
          <ProtectedRoute>
            <BookingPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/booking/summary/:bookingId"
        element={
          <ProtectedRoute>
            <BookingSummaryPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/booking/payment/:bookingId"
        element={
          <ProtectedRoute>
            <PaymentPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/booking/success"
        element={
          <ProtectedRoute>
            <SuccessPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/cashier/scan/:uniqueCode"
        element={
          <AdminRoute>
            <Suspense fallback={<LoadingSpinner />}>
              <CashierScanPage />
            </Suspense>
          </AdminRoute>
        }
      />

      <Route
        path="/admin/*"
        element={
          <AdminRoute>
            <Suspense fallback={<LoadingSpinner />}>
              <AdminLayout />
            </Suspense>
          </AdminRoute>
        }
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route
          path="movies"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"]}>
              <SectionMovies />
            </AdminRoute>
          }
        />
        <Route
          path="halls"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"]}>
              <SectionHalls />
            </AdminRoute>
          }
        />
        <Route
          path="schedule"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"]}>
              <SectionSchedule />
            </AdminRoute>
          }
        />
        <Route
          path="users"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CASHIER"]}>
              <SectionUsers />
            </AdminRoute>
          }
        />
        <Route
          path="bonus"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN"]}>
              <SectionBonus />
            </AdminRoute>
          }
        />
        <Route
          path="promotion"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"]}>
              <SectionPromotion />
            </AdminRoute>
          }
        />
        <Route
          path="ticket-type"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN"]}>
              <SectionTicketType />
            </AdminRoute>
          }
        />
        <Route
          path="audit-logs"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN"]}>
              <SectionAuditLogs />
            </AdminRoute>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
