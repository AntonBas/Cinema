import React, { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useAuthActions } from "@/hooks/features/auth/useAuthActions";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { Chrome } from "lucide-react";
import { AuthCard } from "@/components/auth/AuthCard/AuthCard";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import styles from "./LoginForm.module.css";

const REDIRECT_ERROR_MESSAGES: Record<string, string> = {
  oauth2_failed: "Google sign-in failed. Please try again.",
};

const REDIRECT_INFO_MESSAGES: Record<string, string> = {
  "email-changed": "Your email was changed. Please log in with your new email.",
};

export const LoginForm: React.FC = () => {
  const [searchParams] = useSearchParams();
  const redirectError =
    REDIRECT_ERROR_MESSAGES[searchParams.get("error") ?? ""];
  const redirectInfo = REDIRECT_INFO_MESSAGES[searchParams.get("reason") ?? ""];

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { loading, error, login, loginWithGoogle } = useAuthActions();

  const needsEmailConfirmation =
    isApiErrorException(error) && error.isForbidden();

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await login({ email, password });
    } catch {
      return;
    }
  };

  const handleGoogleLogin = (e: React.MouseEvent) => {
    e.preventDefault();
    loginWithGoogle();
  };

  return (
    <AuthCard title="Log In to Your Account">
      <div className={styles.loginTop}>
        <span>Don't have an account?</span>
        <Link to="/register">Register</Link>
      </div>

      <form className={styles.loginForm} onSubmit={handleSubmit}>
        {!error && redirectError && (
          <div className={styles.notification} data-type="error">
            {redirectError}
          </div>
        )}
        {!error && !redirectError && redirectInfo && (
          <div className={styles.notification} data-type="info">
            {redirectInfo}
          </div>
        )}
        {error && (
          <div className={styles.notification} data-type="error">
            {error.message}
            {needsEmailConfirmation && (
              <Link
                to="/check-email"
                state={{ email, fromLogin: true }}
                className={styles.notificationLink}
              >
                Resend confirmation email
              </Link>
            )}
          </div>
        )}

        <Input
          type="email"
          value={email}
          onChange={setEmail}
          placeholder="Email address"
          disabled={loading}
          required
        />

        <Input
          type="password"
          value={password}
          onChange={setPassword}
          placeholder="Password"
          disabled={loading}
          required
        />

        <Button
          type="submit"
          variant="primary"
          size="large"
          loading={loading}
          disabled={loading}
          fullWidth
          style={{ marginTop: "1rem" }}
        >
          {loading ? "Logging in..." : "Login"}
        </Button>
      </form>

      <div className={styles.divider}>
        <span className={styles.dividerText}>or</span>
      </div>

      <Button
        type="button"
        variant="outline"
        size="large"
        onClick={handleGoogleLogin}
        disabled={loading}
        fullWidth
      >
        <Chrome size={20} />
        Continue with Google
      </Button>

      <div className={styles.loginBottom}>
        <Link to="/forgot-password">Forgot your password?</Link>
      </div>
    </AuthCard>
  );
};
