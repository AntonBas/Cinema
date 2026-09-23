import React, { useState, useEffect } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { XCircle, CheckCircle2 } from "lucide-react";
import axios from "axios";
import { api } from "@/services/api";
import { Button, Input } from "@/components/ui";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useResendVerification } from "@/hooks/features/auth/useResendVerification";
import { Layout } from "@/components/layout/Layout/Layout";
import styles from "./EmailVerificationPage.module.css";

export const EmailVerificationPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [errorMessage, setErrorMessage] = useState("");
  const [email, setEmail] = useState(searchParams.get("email") || "");
  const { resend, cooldown, sending, message, messageType } =
    useResendVerification();

  const verificationToken = token || searchParams.get("token");

  useEffect(() => {
    if (!verificationToken) {
      setStatus("error");
      setErrorMessage("Invalid or missing verification token.");
      return;
    }

    const verifyEmail = async () => {
      try {
        await api.post(`/api/tokens/email/verify?token=${verificationToken}`);
        setStatus("success");
        setTimeout(() => navigate("/login"), 5000);
      } catch (error) {
        setStatus("error");
        const message = axios.isAxiosError(error)
          ? (error.response?.data as { message?: string } | undefined)
              ?.message
          : error instanceof Error
            ? error.message
            : undefined;
        setErrorMessage(
          message ||
            "Failed to verify email. The token may be invalid or expired.",
        );
      }
    };

    verifyEmail();
  }, [verificationToken, navigate]);

  if (status === "loading") {
    return (
      <Layout>
        <div className={styles.verificationContainer}>
          <LoadingSpinner text="Verifying your email..." />
        </div>
      </Layout>
    );
  }

  if (status === "error") {
    return (
      <Layout>
        <div className={styles.verificationContainer}>
          <div className={`${styles.verificationCard} ${styles.error}`}>
            <XCircle size={64} className={styles.icon} />
            <h2>Verification Failed</h2>
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
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className={styles.verificationContainer}>
        <div className={`${styles.verificationCard} ${styles.success}`}>
          <CheckCircle2 size={64} className={styles.icon} />
          <h2>Email Verified Successfully!</h2>
          <p>Your email has been verified.</p>
          <p className={styles.redirectText}>
            Redirecting to login page in 5 seconds...
          </p>
          <Button variant="primary" onClick={() => navigate("/login")}>
            Go to Login Now
          </Button>
        </div>
      </div>
    </Layout>
  );
};
