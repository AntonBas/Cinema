import { useEffect, useState, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { usePayment } from "@/hooks/features/payment/usePayment";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import {
  CheckCircle2,
  Home,
  Ticket,
  AlertCircle,
  RefreshCw,
} from "lucide-react";
import {
  FINAL_PAYMENT_STATUSES,
  LATE_PAYMENT_REFUND_STATUSES,
  type PaymentResponse,
} from "@/types/payment";
import { Layout } from "@/components/layout/Layout/Layout";
import styles from "./SuccessPage.module.css";

export const SuccessPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { getById } = usePayment();

  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [pollingCount, setPollingCount] = useState(0);
  const [isVisible, setIsVisible] = useState(false);
  const [loadFailed, setLoadFailed] = useState(false);
  const [loadAttempt, setLoadAttempt] = useState(0);
  const pollingRef = useRef<number | null>(null);

  const paymentId = searchParams.get("paymentId");
  const bookingId = searchParams.get("bookingId");

  useEffect(() => {
    setIsVisible(true);
    if (!paymentId) {
      navigate("/", { replace: true });
      return;
    }

    const stopPolling = () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };

    const fetchPayment = async () => {
      try {
        const result = await getById(Number(paymentId));
        setPayment(result);
        setPollingCount((prev) => prev + 1);
      } catch {
        stopPolling();
        setLoadFailed(true);
      }
    };

    fetchPayment();
    pollingRef.current = window.setInterval(fetchPayment, 5000);

    return stopPolling;
  }, [paymentId, getById, navigate, loadAttempt]);

  const handleReload = () => {
    setLoadFailed(false);
    setPollingCount(0);
    setLoadAttempt((prev) => prev + 1);
  };

  useEffect(() => {
    if (payment && FINAL_PAYMENT_STATUSES.includes(payment.status)) {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    }
    if (pollingCount >= 30) {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    }
  }, [payment, pollingCount]);

  if (!payment && loadFailed) {
    return (
      <Layout>
        <div className={`${styles.container} ${styles.visible}`}>
          <div className={styles.card}>
            <div className={styles.errorContainer}>
              <div
                className={`${styles.iconWrapper} ${styles.errorIconWrapper}`}
              >
                <AlertCircle className={styles.errorIcon} size={64} />
              </div>
              <h1 className={styles.title}>Couldn't Load Payment</h1>
              <p className={styles.message}>
                We couldn't check your payment status right now. Please try
                again or check your tickets later.
              </p>
              <div className={styles.actions}>
                <Button variant="primary" onClick={handleReload}>
                  <RefreshCw size={18} /> Try Again
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => navigate("/account/tickets")}
                >
                  <Ticket size={18} /> My Tickets
                </Button>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  if (!payment) {
    return (
      <Layout>
        <div className={styles.loadingContainer}>
          <LoadingSpinner text="Loading payment information..." />
        </div>
      </Layout>
    );
  }

  const isSuccess = payment.status === "SUCCESS";
  const isFailed = ["FAILED", "CANCELLED", "EXPIRED"].includes(payment.status);
  const isProcessing = ["PENDING", "PROCESSING"].includes(payment.status);
  const isRefunded = LATE_PAYMENT_REFUND_STATUSES.includes(payment.status);

  return (
    <Layout>
      <div className={`${styles.container} ${isVisible ? styles.visible : ""}`}>
        <div className={styles.card}>
          <div
            className={
              isSuccess
                ? styles.successContainer
                : isFailed
                  ? styles.errorContainer
                  : styles.warningContainer
            }
          >
            <div
              className={`${styles.iconWrapper} ${isSuccess ? styles.successIconWrapper : isFailed ? styles.errorIconWrapper : styles.warningIconWrapper}`}
            >
              {isSuccess && (
                <CheckCircle2 className={styles.successIcon} size={64} />
              )}
              {isFailed && (
                <AlertCircle className={styles.errorIcon} size={64} />
              )}
              {isProcessing && (
                <RefreshCw className={styles.warningIcon} size={64} />
              )}
              {isRefunded && (
                <AlertCircle className={styles.warningIcon} size={64} />
              )}
            </div>

            <h1 className={styles.title}>
              {isSuccess && "Payment Successful!"}
              {isFailed && "Payment Failed"}
              {isProcessing && "Payment Processing"}
              {isRefunded && "Payment Refunded"}
            </h1>

            <p className={styles.message}>
              {isSuccess &&
                "Your tickets have been successfully paid and booked."}
              {isFailed &&
                "Payment failed. Please try again or contact support."}
              {isProcessing &&
                "Your payment is being processed. Please wait..."}
              {isRefunded &&
                "Your payment arrived after the booking had expired, so no tickets were issued. The full amount is being returned to your card."}
            </p>

            {isProcessing && pollingRef.current && (
              <div className={styles.pollingInfo}>
                <LoadingSpinner text="" />
                <span>Checking payment status... {pollingCount}/30</span>
              </div>
            )}

            <div className={styles.actions}>
              {isSuccess && (
                <>
                  <Button
                    variant="primary"
                    onClick={() => navigate("/account/tickets")}
                  >
                    <Ticket size={18} /> View My Tickets
                  </Button>
                  <Button variant="secondary" onClick={() => navigate("/")}>
                    <Home size={18} /> Back to Home
                  </Button>
                </>
              )}
              {isFailed && (
                <>
                  {bookingId && (
                    <Button
                      variant="primary"
                      onClick={() => navigate(`/booking/summary/${bookingId}`)}
                    >
                      <RefreshCw size={18} /> Try Again
                    </Button>
                  )}
                  <Button variant="secondary" onClick={() => navigate("/")}>
                    <Home size={18} /> Back to Home
                  </Button>
                </>
              )}
              {(isProcessing || isRefunded) && (
                <Button variant="secondary" onClick={() => navigate("/")}>
                  <Home size={18} /> Back to Home
                </Button>
              )}
            </div>
          </div>

          {isSuccess && (
            <div className={styles.footer}>
              <span className={styles.footerText}>
                Thank you for your purchase! Enjoy the movie!
              </span>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};
