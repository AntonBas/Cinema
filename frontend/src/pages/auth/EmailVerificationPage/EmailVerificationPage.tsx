import React, { useState, useEffect, useRef } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { XCircle, CheckCircle2 } from "lucide-react";
import { api } from "@/services/api";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import { Button } from "@/components/ui/Button/Button";
import { Input } from "@/components/ui/Input/Input";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useResendVerification } from "@/hooks/features/auth/useResendVerification";
import { AuthPageLayout } from "@/components/auth/AuthPageLayout/AuthPageLayout";
import { AuthCard } from "@/components/auth/AuthCard/AuthCard";
import styles from "./EmailVerificationPage.module.css";

export const EmailVerificationPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [errorMessage, setErrorMessage] = useState("");
  const [alreadyVerified, setAlreadyVerified] = useState(false);
  const requestedRef = useRef(false);
  const [email, setEmail] = useState(searchParams.get("email") || "");
  const { resend, cooldown, sending, message, messageType } =
    useResendVerification();

  const verificationToken = token || searchParams.get("token");

  useEffect(() => {
    if (requestedRef.current) return;
    requestedRef.current = true;

    if (!verificationToken) {
      setStatus("error");
      setErrorMessage("Invalid or missing verification token.");
      return;
    }

    const verifyEmail = async () => {
      try {
        await api.post(`/api/tokens/email/verify?token=${verificationToken}`);
        setStatus("success");
      } catch (error) {
        if (isApiErrorException(error) && error.isConflict()) {
          setAlreadyVerified(true);
          setStatus("success");
          return;
        }
        setStatus("error");
        const message = error instanceof Error ? error.message : undefined;
        setErrorMessage(
          message ||
            "Failed to verify email. The token may be invalid or expired.",
        );
      }
    };

    verifyEmail();
  }, [verificationToken]);

  useEffect(() => {
    if (status !== "success") return;
    const timerId = setTimeout(() => navigate("/login"), 5000);
    return () => clearTimeout(timerId);
  }, [status, navigate]);

  if (status === "loading") {
    return (
      <AuthPageLayout>
        <LoadingSpinner text="Verifying your email..." />
      </AuthPageLayout>
    );
  }

  if (status === "error") {
    return (
      <AuthPageLayout>
        <AuthCard
          title="Verification Failed"
          className={`${styles.verificationCard} ${styles.error}`}
        >
          <XCircle size={64} className={styles.icon} />
          <p>{errorMessage}</p>
          <p className={styles.message}>
            Enter your email to get a new verification link.
          </p>

          <Input
            type="email"
            label="Email"
            value={email}
            onChange={setEmail}
            placeholder="your@email.com"
          />

          {message && (
            <p
              className={
                messageType === "error"
                  ? styles.messageError
                  : styles.messageSuccess
              }
            >
              {message}
            </p>
          )}

          <div className={styles.errorActions}>
            <Button
              variant="primary"
              loading={sending}
              disabled={cooldown > 0 || !email}
              onClick={() => resend(email)}
            >
              {cooldown > 0
                ? `Resend in ${cooldown}s`
                : "Resend verification email"}
            </Button>
            <Button variant="secondary" onClick={() => navigate("/login")}>
              Go to Login
            </Button>
          </div>
        </AuthCard>
      </AuthPageLayout>
    );
  }

  return (
    <AuthPageLayout>
      <AuthCard
        title={
          alreadyVerified
            ? "Email Already Verified"
            : "Email Verified Successfully!"
        }
        className={`${styles.verificationCard} ${styles.success}`}
      >
        <CheckCircle2 size={64} className={styles.icon} />
        <p>
          {alreadyVerified
            ? "This email address has already been confirmed. You can sign in."
            : "Your email has been verified."}
        </p>
        <p className={styles.redirectText}>
          Redirecting to login page in 5 seconds...
        </p>
        <Button variant="primary" onClick={() => navigate("/login")}>
          Go to Login Now
        </Button>
      </AuthCard>
    </AuthPageLayout>
  );
};
