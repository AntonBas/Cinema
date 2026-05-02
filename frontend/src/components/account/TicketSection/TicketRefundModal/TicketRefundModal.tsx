import React, { useState } from "react";
import { Modal, Button } from "@/components/ui";
import { useRefund } from "@/hooks/features/refund/useRefund";
import type { TicketResponse } from "@/types/ticket";
import styles from "./TicketRefundModal.module.css";

interface TicketRefundModalProps {
  ticket: TicketResponse | null;
  onClose: () => void;
  onRefundSuccess?: () => void;
}

const REFUND_REASONS = [
  { value: "change_plans", label: "Changed my plans" },
  { value: "double_booking", label: "Accidental double booking" },
  { value: "schedule_conflict", label: "Schedule conflict" },
  { value: "other", label: "Other reason" },
];

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const TicketRefundModal: React.FC<TicketRefundModalProps> = ({
  ticket,
  onClose,
  onRefundSuccess,
}) => {
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [selectedReason, setSelectedReason] = useState<string>("");
  const { processRefund, loading, refundResult } = useRefund();

  if (!ticket) return null;

  const sessionDate = new Date(ticket.sessionTime);
  const hoursUntilSession =
    (sessionDate.getTime() - Date.now()) / (1000 * 60 * 60);
  const canRequestRefund = ticket.status === "ACTIVE" && hoursUntilSession > 2;

  const handleSubmit = async () => {
    if (!selectedReason || !acceptedTerms) return;

    const result = await processRefund({
      ticketId: ticket.id,
      reason: selectedReason,
    });

    if (result) {
      onRefundSuccess?.();
    }
  };

  if (!canRequestRefund) {
    return (
      <Modal
        isOpen={true}
        onClose={onClose}
        title="Refund Not Available"
        size="medium"
      >
        <div className={styles.modalBody}>
          <div className={styles.errorMessage}>
            <span>⚠️</span>
            <div>
              <h4>Refund Not Available</h4>
              <p>
                {ticket.status !== "ACTIVE"
                  ? "Only active tickets can be refunded"
                  : "Refunds are only available more than 2 hours before the session"}
              </p>
            </div>
          </div>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="primary" onClick={onClose}>
            Close
          </Button>
        </div>
      </Modal>
    );
  }

  if (refundResult) {
    return (
      <Modal
        isOpen={true}
        onClose={onClose}
        title="Refund Submitted"
        size="medium"
      >
        <div className={styles.modalBody}>
          <div className={styles.successMessage}>
            <span>✅</span>
            <div>
              <h4>Refund Request Submitted</h4>
              <div className={styles.refundDetails}>
                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>Refund Number</span>
                  <span className={styles.detailValue}>
                    {refundResult.refundNumber}
                  </span>
                </div>
                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>Status</span>
                  <span className={`${styles.detailValue} ${styles.status}`}>
                    {refundResult.status}
                  </span>
                </div>
                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>Amount</span>
                  <span className={`${styles.detailValue} ${styles.amount}`}>
                    {refundResult.totalAmount} UAH
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="primary" onClick={onClose}>
            Close
          </Button>
        </div>
      </Modal>
    );
  }

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      title="Request Ticket Refund"
      size="medium"
    >
      <div className={styles.modalBody}>
        <div className={styles.ticketInfo}>
          <h4>Ticket Information</h4>
          <div className={styles.infoGrid}>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Movie</span>
              <span className={styles.infoValue}>{ticket.movieTitle}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Session</span>
              <span className={styles.infoValue}>
                {formatDateTime(ticket.sessionTime)}
              </span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Hall</span>
              <span className={styles.infoValue}>{ticket.hallName}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Seat</span>
              <span className={styles.infoValue}>
                Row {ticket.row}, Seat {ticket.seatNumber}
              </span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Price</span>
              <span className={styles.infoValue}>{ticket.price} UAH</span>
            </div>
          </div>
        </div>

        <div className={styles.refundEstimate}>
          <svg
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <div>
            <span className={styles.estimateLabel}>Estimated Refund</span>
            <span className={styles.estimateValue}>{ticket.price} UAH</span>
            <span className={styles.estimateNote}>
              Processing fee may apply based on refund policy
            </span>
          </div>
        </div>

        <div className={styles.timeWarning}>
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          <span>
            {Math.floor(hoursUntilSession)} hours until session starts
          </span>
        </div>

        <div className={styles.reasonSelection}>
          <h4>Select Refund Reason</h4>
          <div className={styles.reasonOptions}>
            {REFUND_REASONS.map((reason) => (
              <label key={reason.value} className={styles.reasonOption}>
                <input
                  type="radio"
                  name="refundReason"
                  value={reason.value}
                  checked={selectedReason === reason.value}
                  onChange={(e) => setSelectedReason(e.target.value)}
                  className={styles.reasonInput}
                />
                <span className={styles.reasonLabel}>{reason.label}</span>
              </label>
            ))}
          </div>
        </div>

        <div className={styles.termsSection}>
          <div className={styles.termsHeader}>
            <h4>Refund Policy</h4>
            <a
              href="/refund-policy"
              className={styles.policyLink}
              target="_blank"
              rel="noopener noreferrer"
            >
              Full policy
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                <polyline points="15 3 21 3 21 9" />
                <line x1="10" y1="14" x2="21" y2="3" />
              </svg>
            </a>
          </div>
          <div className={styles.termsContent}>
            <ul className={styles.termsList}>
              <li>100% refund — 48+ hours before session</li>
              <li>85% refund — 24-48 hours before session</li>
              <li>50% refund — 2-24 hours before session</li>
              <li>No refund — less than 2 hours before session</li>
              <li>Refunds processed within 5-7 business days</li>
            </ul>
          </div>
          <label className={styles.termsAgreement}>
            <input
              type="checkbox"
              checked={acceptedTerms}
              onChange={(e) => setAcceptedTerms(e.target.checked)}
              className={styles.termsCheckbox}
            />
            <span className={styles.termsText}>
              I agree to the refund policy terms
            </span>
          </label>
        </div>
      </div>

      <div className={styles.modalFooter}>
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button
          variant="primary"
          onClick={handleSubmit}
          loading={loading}
          disabled={!selectedReason || !acceptedTerms || loading}
        >
          Submit Refund Request
        </Button>
      </div>
    </Modal>
  );
};
