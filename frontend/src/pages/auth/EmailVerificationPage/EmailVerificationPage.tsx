import React, { useState, useEffect } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { XCircle, CheckCircle2 } from "lucide-react";
import { api } from "@/services/api";
import { Button } from "@/components/ui";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./EmailVerificationPage.module.css";

export const EmailVerificationPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [errorMessage, setErrorMessage] = useState("");

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
      } catch (error: any) {
        setStatus("error");
        setErrorMessage(
          error?.response?.data?.message ||
            "Failed to verify email. The token may be invalid or expired.",
        );
      }
    };

    verifyEmail();
  }, [verificationToken, navigate]);

  if (status === "loading") {
    return (
      <div className={styles.verificationContainer}>
        <LoadingSpinner text="Verifying your email..." />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div className={styles.verificationContainer}>
        <div className={`${styles.verificationCard} ${styles.error}`}>
          <XCircle size={64} className={styles.icon} />
          <h2>Verification Failed</h2>
          <p>{errorMessage}</p>
          <Button variant="secondary" onClick={() => navigate("/login")}>
            Go to Login
          </Button>
        </div>
      </div>
    );
  }

  return (
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
  );
};
