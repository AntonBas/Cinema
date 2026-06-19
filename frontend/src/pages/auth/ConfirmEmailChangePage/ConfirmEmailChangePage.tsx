import React, { useState, useEffect } from "react";
import {
  useParams,
  useSearchParams,
  useNavigate,
  Link,
} from "react-router-dom";
import { XCircle, CheckCircle2 } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/services/api";
import { Button } from "@/components/ui/Button/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
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

  const confirmationToken = token || searchParams.get("token");

  useEffect(() => {
    if (!confirmationToken) {
      setStatus("error");
      setErrorMessage("Invalid or missing confirmation token.");
      return;
    }

    const confirmEmailChange = async () => {
      try {
        await api.post(
          `/api/tokens/email/change/confirm?token=${confirmationToken}`,
        );
        setStatus("success");
        if (isAuthenticated) {
          await refreshUser();
        }
      } catch (error: any) {
        setStatus("error");
        setErrorMessage(
          error?.response?.data?.message ||
            "Failed to confirm email change. The token may be invalid or expired.",
        );
      }
    };

    confirmEmailChange();
  }, [confirmationToken, isAuthenticated, refreshUser]);

  if (status === "loading") {
    return (
      <div className={styles.container}>
        <div className={styles.card}>
          <LoadingSpinner text="Confirming your email change..." />
        </div>
      </div>
    );
  }

  if (status === "error") {
    return (
      <section className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Confirmation Failed</h1>
          <XCircle size={64} className={styles.icon} />
          <p className={styles.message}>{errorMessage}</p>
          <div className={styles.actions}>
            <Button
              variant="primary"
              onClick={() => navigate("/login")}
              style={{ width: "100%" }}
            >
              Go to Login
            </Button>
          </div>
          <div className={styles.bottom}>
            <Link to="/">Back to Home</Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Email Changed!</h1>
        <CheckCircle2 size={64} className={styles.icon} />
        <p className={styles.message}>
          Your email address has been successfully updated.
        </p>
        <div className={styles.actions}>
          <Button
            variant="primary"
            onClick={() => navigate(isAuthenticated ? "/account" : "/login")}
            style={{ width: "100%" }}
          >
            {isAuthenticated ? "Go to Account" : "Go to Login"}
          </Button>
        </div>
        <div className={styles.bottom}>
          <Link to="/">Back to Home</Link>
        </div>
      </div>
    </section>
  );
};
