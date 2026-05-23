import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthActions } from "@/hooks/features/auth/useAuthActions";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./OAuth2Redirect.module.css";

export const OAuth2Redirect: React.FC = () => {
  const navigate = useNavigate();
  const { oauth2Success } = useAuthActions();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");
    const userId = params.get("userId");
    const email = params.get("email");

    if (token && userId && email) {
      oauth2Success(token, Number(userId), email).catch(() => {
        navigate("/login?error=oauth2_failed");
      });
    } else {
      navigate("/login?error=invalid_oauth2_response");
    }
  }, [navigate, oauth2Success]);

  return (
    <div className={styles.container}>
      <LoadingSpinner text="Completing login..." />
    </div>
  );
};
