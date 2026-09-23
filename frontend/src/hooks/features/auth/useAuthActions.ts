import { useCallback, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useApi } from "@/hooks/common/useApi";
import { authApi } from "@/api/authApi";
import type { LoginRequest, RegisterRequest } from "@/types/auth";
import { useAuth } from "@/context/AuthContext";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import {
  consumeOAuth2Redirect,
  getRedirectFromSearch,
  rememberOAuth2Redirect,
} from "@/utils/authRedirect";

export const useAuthActions = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { refreshUser, logout: contextLogout } = useAuth();

  const authApiHook = useApi();
  const authApiRef = useRef(authApiHook);
  authApiRef.current = authApiHook;

  const loading = useDelayedLoading(authApiHook.loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const handleAuthSuccess = useCallback(
    async (redirectTarget: string) => {
      await refreshUser();
      navigate(redirectTarget, { replace: true });
    },
    [refreshUser, navigate],
  );

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const response = await authApiRef.current.execute(
        () => authApi.login(credentials),
        { successMessage: "Login successful", dedupeKey: "login" },
      );
      if (response) {
        await handleAuthSuccess(getRedirectFromSearch(searchParams));
      }
      return response;
    },
    [handleAuthSuccess, searchParams],
  );

  const register = useCallback(async (userData: RegisterRequest) => {
    return authApiRef.current.execute(() => authApi.register(userData), {
      suppressValidationToast: true,
      dedupeKey: "register",
    });
  }, []);

  const checkEmail = useCallback(async (email: string) => {
    return authApiRef.current.execute(() => authApi.checkEmail(email));
  }, []);

  const forgotPassword = useCallback(async (email: string) => {
    return authApiRef.current.execute(() => authApi.forgotPassword(email), {
      successMessage: "Password reset instructions sent to your email",
      dedupeKey: "forgotPassword",
    });
  }, []);

  const resetPassword = useCallback(
    async (token: string, newPassword: string) => {
      return authApiRef.current.execute(
        () => authApi.resetPassword(token, newPassword),
        { suppressValidationToast: true, dedupeKey: "resetPassword" },
      );
    },
    [],
  );

  const oauth2Exchange = useCallback(
    async (code: string) => {
      const response = await authApiRef.current.execute(
        () => authApi.oauth2Exchange(code),
        { successMessage: "Login successful", dedupeKey: "oauth2Exchange" },
      );
      if (response) {
        await handleAuthSuccess(consumeOAuth2Redirect());
      }
      return response;
    },
    [handleAuthSuccess],
  );

  const loginWithGoogle = useCallback(() => {
    rememberOAuth2Redirect(getRedirectFromSearch(searchParams));
    window.location.href = authApi.getGoogleAuthUrl();
  }, [searchParams]);

  return {
    loading,
    error: authApiHook.error,
    login,
    register,
    checkEmail,
    forgotPassword,
    resetPassword,
    oauth2Exchange,
    loginWithGoogle,
    logout: contextLogout,
  };
};
