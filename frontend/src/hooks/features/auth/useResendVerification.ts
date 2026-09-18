import { useCallback, useEffect, useState } from "react";
import { authApi } from "@/api/authApi";
import { isApiErrorException } from "@/utils/apiErrorHandler";

const FALLBACK_COOLDOWN_SECONDS = 60;

export const useResendVerification = (initialCooldown = 0) => {
  const [cooldown, setCooldown] = useState(initialCooldown);
  const [sending, setSending] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error">(
    "success",
  );

  useEffect(() => {
    const id = setInterval(() => {
      setCooldown((seconds) => (seconds > 0 ? seconds - 1 : 0));
    }, 1000);
    return () => clearInterval(id);
  }, []);

  const resend = useCallback(
    async (email: string) => {
      if (cooldown > 0 || sending || !email) return;
      setSending(true);
      setMessage("");
      try {
        const response = await authApi.resendVerification(email);
        setMessageType("success");
        setMessage("Verification email sent. Check your inbox.");
        setCooldown(response.data.cooldownSeconds);
      } catch (err) {
        setMessageType("error");
        if (isApiErrorException(err) && err.isTooManyRequests()) {
          setMessage(err.message);
          try {
            const status = await authApi.getResendVerificationStatus(email);
            setCooldown(status.data.cooldownSeconds || FALLBACK_COOLDOWN_SECONDS);
          } catch {
            setCooldown(FALLBACK_COOLDOWN_SECONDS);
          }
        } else {
          setMessage(
            err instanceof Error
              ? err.message
              : "Failed to resend verification email",
          );
        }
      } finally {
        setSending(false);
      }
    },
    [cooldown, sending],
  );

  const syncStatus = useCallback(async (email: string) => {
    if (!email) return;
    await authApi
      .getResendVerificationStatus(email)
      .then((response) => setCooldown(response.data.cooldownSeconds))
      .catch(() => undefined);
  }, []);

  return { resend, syncStatus, cooldown, sending, message, messageType };
};
