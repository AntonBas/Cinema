import React, { useState, useEffect } from "react";
import { Modal } from "@/components/ui/Modal/Modal";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useTicket } from "@/hooks/features/ticket/useTicket";
import styles from "./TicketQRModal.module.css";

interface TicketQRModalProps {
  ticketCode: string;
  onClose: () => void;
}

export const TicketQRModal: React.FC<TicketQRModalProps> = ({
  ticketCode,
  onClose,
}) => {
  const { getQRCode, loading } = useTicket();
  const [qrImage, setQrImage] = useState<string>("");
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let objectUrl = "";

    const loadQRCode = async () => {
      try {
        setError(null);
        const blob = await getQRCode(ticketCode);
        if (blob) {
          objectUrl = URL.createObjectURL(blob);
          setQrImage(objectUrl);
        } else {
          setError("Failed to load QR code");
        }
      } catch {
        setError("Failed to load QR code");
      }
    };

    loadQRCode();

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [ticketCode, getQRCode]);

  const handleDownload = () => {
    if (!qrImage) return;

    const link = document.createElement("a");
    link.href = qrImage;
    link.download = `ticket-${ticketCode}-qr.png`;
    link.click();
  };

  const handleCopyCode = async () => {
    await navigator.clipboard.writeText(ticketCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Ticket QR Code" size="medium">
      <div className={styles.modalBody}>
        <div className={styles.ticketCodeSection}>
          <div className={styles.ticketCodeLabel}>Ticket Code:</div>
          <div className={styles.ticketCodeValue}>
            {ticketCode}
            <Button variant="secondary" size="small" onClick={handleCopyCode}>
              {copied ? "Copied!" : "Copy"}
            </Button>
          </div>
        </div>

        <div className={styles.qrContainer}>
          {loading ? (
            <LoadingSpinner text="Generating QR code..." />
          ) : qrImage ? (
            <div className={styles.qrImageWrapper}>
              <img
                src={qrImage}
                alt="Ticket QR Code"
                className={styles.qrImage}
              />
            </div>
          ) : (
            <div className={styles.errorQR}>
              <p>{error || "Failed to load QR code"}</p>
              <Button
                variant="secondary"
                onClick={() => window.location.reload()}
              >
                Retry
              </Button>
            </div>
          )}
        </div>

        <div className={styles.instructions}>
          <h4>How to use:</h4>
          <ul>
            <li>Show this QR code at the cinema entrance</li>
            <li>The cashier will scan it to validate your ticket</li>
            <li>QR code is valid only for ACTIVE tickets</li>
          </ul>
        </div>

        <div className={styles.modalFooter}>
          <Button variant="secondary" onClick={onClose}>
            Close
          </Button>
          <Button
            variant="primary"
            onClick={handleDownload}
            disabled={!qrImage || loading}
          >
            Download QR Code
          </Button>
        </div>
      </div>
    </Modal>
  );
};
