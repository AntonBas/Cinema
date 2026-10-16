import React, { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";
import { PublicRoute } from "./PublicRoute";
import { AdminRoute } from "./AdminRoute";
import { AdminIndexRedirect } from "./AdminIndexRedirect";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";

import { LoginPage } from "@/features/auth/pages/LoginPage/LoginPage";
import { RegisterPage } from "@/features/auth/pages/RegisterPage/RegisterPage";
import { ForgotPasswordPage } from "@/features/auth/pages/ForgotPasswordPage/ForgotPasswordPage";
import { ResetPasswordPage } from "@/features/auth/pages/ResetPasswordPage/ResetPasswordPage";
import { EmailVerificationPage } from "@/features/auth/pages/EmailVerificationPage/EmailVerificationPage";
import { CheckEmailPage } from "@/features/auth/pages/CheckEmailPage/CheckEmailPage";
import { ConfirmEmailChangePage } from "@/features/auth/pages/ConfirmEmailChangePage/ConfirmEmailChangePage";
import { OAuth2Redirect } from "@/features/auth/components/OAuth2Redirect/OAuth2Redirect";

import { HomePage } from "@/pages/home/HomePage";
import { ProfilePage } from "@/features/user/pages/ProfilePage/ProfilePage";
import { SecurityPage } from "@/features/user/pages/SecurityPage/SecurityPage";
import { BonusPage } from "@/features/bonus/pages/BonusPage/BonusPage";
import { TicketsPage } from "@/features/ticket/pages/TicketsPage/TicketsPage";
import { RefundPolicyPage } from "@/features/refund/pages/RefundPolicyPage/RefundPolicyPage";
import { PrivacyPolicyPage } from "@/pages/PrivacyPolicyPage/PrivacyPolicyPage";
import { NotFoundPage } from "@/pages/NotFoundPage/NotFoundPage";

import { MoviesLayout } from "@/features/movie/pages/MoviesLayout/MoviesLayout";
import { CurrentMoviesPage } from "@/features/movie/pages/CurrentMoviesPage/CurrentMoviesPage";
import { UpcomingMoviesPage } from "@/features/movie/pages/UpcomingMoviesPage/UpcomingMoviesPage";
import { MovieDetailPage } from "@/features/movie/pages/MovieDetailPage/MovieDetailPage";

import { SessionsPage } from "@/features/cinema/pages/SessionsPage";
import { BookingPage } from "@/features/booking/pages/BookingPage/BookingPage";
import { BookingSummaryPage } from "@/features/booking/pages/BookingSummaryPage/BookingSummaryPage";
import { PaymentPage } from "@/features/booking/pages/PaymentPage/PaymentPage";
import { SuccessPage } from "@/features/booking/pages/SuccessPage/SuccessPage";

const AdminLayout = lazy(() =>
  import("@/components/admin/AdminLayout/AdminLayout").then((m) => ({
    default: m.AdminLayout,
  })),
);
const SectionMovies = lazy(() =>
  import("@/features/movie/admin/SectionMovies/SectionMovies").then((m) => ({
    default: m.SectionMovies,
  })),
);
const SectionHalls = lazy(() =>
  import("@/features/cinema/admin/SectionHalls/SectionHalls").then((m) => ({
    default: m.SectionHalls,
  })),
);
const SectionSchedule = lazy(() =>
  import("@/features/cinema/admin/SectionSchedule/SectionSchedule").then(
    (m) => ({
      default: m.SectionSchedule,
    }),
  ),
);
const SectionUsers = lazy(() =>
  import("@/features/user/admin/SectionUsers/SectionUsers").then((m) => ({
    default: m.SectionUsers,
  })),
);
const SectionBonus = lazy(() =>
  import("@/features/bonus/admin/SectionBonus/SectionBonus").then((m) => ({
    default: m.SectionBonus,
  })),
);
const SectionPromotion = lazy(() =>
  import("@/features/promotion/admin/SectionPromotion/SectionPromotion").then(
    (m) => ({
      default: m.SectionPromotion,
    }),
  ),
);
const SectionTicketType = lazy(() =>
  import("@/features/ticket/admin/SectionTicketType/SectionTicketType").then(
    (m) => ({ default: m.SectionTicketType }),
  ),
);
const SectionBookings = lazy(() =>
  import("@/features/booking/admin/SectionBookings/SectionBookings").then(
    (m) => ({
      default: m.SectionBookings,
    }),
  ),
);
const SectionRefunds = lazy(() =>
  import("@/features/refund/admin/SectionRefunds/SectionRefunds").then((m) => ({
    default: m.SectionRefunds,
  })),
);
const SectionAuditLogs = lazy(() =>
  import("@/features/audit/admin/SectionAuditLogs/SectionAuditLogs").then(
    (m) => ({
      default: m.SectionAuditLogs,
    }),
  ),
);

const CashierScanPage = lazy(() =>
  import("@/features/ticket/pages/CashierScanPage").then((m) => ({
    default: m.CashierScanPage,
  })),
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

      <Route
        path="/check-email"
        element={
          <PublicRoute>
            <CheckEmailPage />
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
        <Route index element={<Navigate to="current" replace />} />
      </Route>
      <Route path="/movies/:slug" element={<MovieDetailPage />} />

      <Route path="/schedule" element={<SessionsPage />} />

      <Route path="/refund-policy" element={<RefundPolicyPage />} />
      <Route path="/privacy" element={<PrivacyPolicyPage />} />

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
          <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CASHIER"]}>
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
        <Route index element={<AdminIndexRedirect />} />
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
          path="bookings"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CASHIER"]}>
              <SectionBookings />
            </AdminRoute>
          }
        />
        <Route
          path="refunds"
          element={
            <AdminRoute allowedRoles={["ROLE_ADMIN", "ROLE_CASHIER"]}>
              <SectionRefunds />
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

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
