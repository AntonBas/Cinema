import React, { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthActions } from "@/hooks/features/auth/useAuthActions";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./OAuth2Redirect.module.css";

export const OAuth2Redirect: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { oauth2Exchange } = useAuthActions();

  useEffect(() => {
    const code = searchParams.get("code");

    if (code) {
      oauth2Exchange(code).catch(() => {
        navigate("/login?error=oauth2_failed");
      });
    } else {
      navigate("/login?error=invalid_oauth2_response");
    }
  }, [navigate, oauth2Exchange, searchParams]);

  return (
    <div className={styles.container}>
      <LoadingSpinner text="Completing login..." />
    </div>
  );
};
