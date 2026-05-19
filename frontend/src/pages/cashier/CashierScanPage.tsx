import React, { useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { XCircle } from "lucide-react";
import { Layout } from "@/components/layout/Layout/Layout";
import { useTickets } from "@/hooks/features/tickets/useTickets";
import { CashierStatusBadge } from "@/components/cashier/CashierStatusBadge/CashierStatusBadge";
import { CashierTicketInfo } from "@/components/cashier/CashierTicketInfo/CashierTicketInfo";
import { CashierValidateButton } from "@/components/cashier/CashierValidateButton/CashierValidateButton";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./CashierScanPage.module.css";

const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) return error.message;
  if (typeof error === "string") return error;
  return "An unexpected error occurred";
};

export const CashierScanPage: React.FC = () => {
  const { uniqueCode } = useParams<{ uniqueCode: string }>();
  const navigate = useNavigate();
  const {
    cashierTicket,
    validatedTicket,
    loading,
    cashierTicketError,
    cashierValidateError,
    getTicketForCashier,
    validateTicket,
  } = useTickets();

  useEffect(() => {
    if (uniqueCode) {
      getTicketForCashier(uniqueCode);
    }
  }, [uniqueCode]);

  const ticket = validatedTicket || cashierTicket;

  if (loading && !ticket) {
    return (
      <Layout>
        <LoadingSpinner text="Loading ticket..." />
      </Layout>
    );
  }

  if (cashierTicketError && !ticket) {
    return (
      <Layout>
        <div className={styles.container}>
          <div className={styles.errorCard}>
            <XCircle size={48} className={styles.errorIcon} />
            <h2 className={styles.errorTitle}>Ticket Not Found</h2>
            <p className={styles.errorMessage}>
              {getErrorMessage(cashierTicketError)}
            </p>
            <button
              className={styles.backButton}
              onClick={() => navigate("/cashier")}
            >
              Back to Home Page
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  if (!ticket) {
    return (
      <Layout>
        <div className={styles.container}>
          <LoadingSpinner text="Loading ticket..." />
        </div>
      </Layout>
    );
  }

  const handleValidate = () => {
    if (uniqueCode) {
      validateTicket(uniqueCode);
    }
  };

  return (
    <Layout>
      <div className={styles.container}>
        <div className={styles.card}>
          <div className={styles.header}>
            <h1 className={styles.title}>Ticket #{ticket.uniqueCode}</h1>
            <CashierStatusBadge status={ticket.status} />
          </div>

          <CashierTicketInfo ticket={ticket} />

          <div className={styles.actions}>
            <CashierValidateButton
              status={ticket.status}
              loading={loading}
              onValidate={handleValidate}
            />
          </div>

          {cashierValidateError && (
            <div className={styles.error}>
              {getErrorMessage(cashierValidateError)}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};
