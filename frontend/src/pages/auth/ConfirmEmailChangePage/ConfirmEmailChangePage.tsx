import React, { useState, useEffect, useRef } from "react";
import {
  useParams,
  useSearchParams,
  useNavigate,
  Link,
} from "react-router-dom";
import { XCircle, CheckCircle2 } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { authApi } from "@/api/authApi";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { AuthPageLayout } from "@/components/auth/AuthPageLayout/AuthPageLayout";
import { AuthCard } from "@/components/auth/AuthCard/AuthCard";
import styles from "./ConfirmEmailChangePage.module.css";

export const ConfirmEmailChangePage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated, refreshUser } = useAuth();

  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [errorMessage, setErrorMessage] = useState("");
  const [alreadyConfirmed, setAlreadyConfirmed] = useState(false);
  const requestedRef = useRef(false);

  const confirmationToken = token || searchParams.get("token");

  useEffect(() => {
    if (requestedRef.current) return;
    requestedRef.current = true;

    if (!confirmationToken) {
      setStatus("error");
      setErrorMessage("Invalid or missing confirmation token.");
      return;
    }

    const confirmEmailChange = async () => {
      try {
        await authApi.confirmEmailChange(confirmationToken);
        await refreshUser();
        setStatus("success");
      } catch (error) {
        if (isApiErrorException(error) && error.isConflict()) {
          await refreshUser();
          setAlreadyConfirmed(true);
          setStatus("success");
          return;
        }
        setStatus("error");
        const message = error instanceof Error ? error.message : undefined;
        setErrorMessage(
          message ||
            "Failed to confirm email change. The token may be invalid or expired.",
        );
      }
    };

    confirmEmailChange();
  }, [confirmationToken, refreshUser]);

  if (status === "loading") {
    return (
      <AuthPageLayout>
        <LoadingSpinner text="Confirming your email change..." />
      </AuthPageLayout>
    );
  }

  if (status === "error") {
    return (
      <AuthPageLayout>
        <AuthCard title="Confirmation Failed" className={styles.card}>
          <XCircle size={64} className={styles.icon} />
          <p className={styles.message}>{errorMessage}</p>
          <div className={styles.actions}>
            <Button
              variant="primary"
              onClick={() => navigate("/login")}
              fullWidth
            >
              Go to Login
            </Button>
          </div>
          <div className={styles.bottom}>
            <Link to="/">Back to Home</Link>
          </div>
        </AuthCard>
      </AuthPageLayout>
    );
  }

  return (
    <AuthPageLayout>
      <AuthCard
        title={alreadyConfirmed ? "Already Confirmed" : "Email Changed!"}
        className={styles.card}
      >
        <CheckCircle2 size={64} className={styles.icon} />
        <p className={styles.message}>
          {alreadyConfirmed
            ? "This email change has already been confirmed."
            : "Your email address has been successfully updated."}
          {!isAuthenticated && " Sign in with your new email address."}
        </p>
        <div className={styles.actions}>
          <Button
            variant="primary"
            onClick={() =>
              navigate(
                isAuthenticated ? "/account" : "/login?reason=email-changed",
              )
            }
            fullWidth
          >
            {isAuthenticated ? "Go to Account" : "Go to Login"}
          </Button>
        </div>
        <div className={styles.bottom}>
          <Link to="/">Back to Home</Link>
        </div>
      </AuthCard>
    </AuthPageLayout>
  );
};
