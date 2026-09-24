import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { MailCheck } from "lucide-react";
import { Button } from "@/components/ui/Button/Button";
import { Input } from "@/components/ui/Input/Input";
import { useResendVerification } from "@/hooks/features/auth/useResendVerification";
import { AuthPageLayout } from "@/components/auth/AuthPageLayout/AuthPageLayout";
import { AuthCard } from "@/components/auth/AuthCard/AuthCard";
import styles from "./CheckEmailPage.module.css";

interface CheckEmailLocationState {
  email?: string;
  fromLogin?: boolean;
}

export const CheckEmailPage: React.FC = () => {
  const location = useLocation();
  const state = (location.state ?? {}) as CheckEmailLocationState;
  const [email, setEmail] = useState(state.email ?? "");
  const { resend, syncStatus, cooldown, sending, message, messageType } =
    useResendVerification();

  useEffect(() => {
    if (state.email) {
      syncStatus(state.email);
    }
  }, [state.email, syncStatus]);

  const intro = state.fromLogin
    ? "Your email address isn't confirmed yet. Open the link we sent you, or request a new one."
    : state.email
      ? "We've sent a confirmation link to"
      : "Enter your email to get a new confirmation link.";

  return (
    <AuthPageLayout>
      <AuthCard title="Check Your Email" className={styles.card}>
        <MailCheck size={64} className={styles.icon} />
        <p className={styles.message}>{intro}</p>
        {state.email && !state.fromLogin && (
          <p className={styles.email}>{state.email}</p>
        )}

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

        <div className={styles.actions}>
          <Button
            variant="primary"
            loading={sending}
            disabled={cooldown > 0 || !email}
            onClick={() => resend(email)}
            fullWidth
          >
            {cooldown > 0
              ? `Resend in ${cooldown}s`
              : "Resend confirmation email"}
          </Button>
        </div>

        <p className={styles.hint}>Can't find it? Check your spam folder.</p>
        <div className={styles.bottom}>
          <Link to="/login">Back to Login</Link>
        </div>
      </AuthCard>
    </AuthPageLayout>
  );
};
