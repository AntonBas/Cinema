import React from "react";
import { Modal } from "../Modal/Modal";
import { Button } from "../Button/Button";
import styles from "./DeleteConfirmModal.module.css";

export interface DeleteConfirmModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  title?: string;
  message?: string;
  itemName?: string;
  itemType?: string;
  isDeleting?: boolean;
  confirmText?: string;
  cancelText?: string;
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
  isOpen,
  onConfirm,
  onCancel,
  title,
  message,
  itemName = "",
  itemType = "item",
  isDeleting = false,
  confirmText,
  cancelText = "Cancel",
}) => {
  const modalTitle = title || `Delete ${itemType}`;
  const modalMessage =
    message || `Are you sure you want to delete this ${itemType}?`;
  const modalConfirmText = confirmText || `Delete ${itemType}`;

  return (
    <Modal isOpen={isOpen} onClose={onCancel} title={modalTitle} size="small">
      <div className={styles.content}>
        <div className={styles.icon} aria-hidden="true">
          <svg
            width="48"
            height="48"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>
        <p className={styles.message}>{modalMessage}</p>
        {itemName && <p className={styles.itemName}>"{itemName}"</p>}
        <p className={styles.warning}>This action cannot be undone.</p>
        <div className={styles.actions}>
          <Button variant="cancel" onClick={onCancel} disabled={isDeleting}>
            {cancelText}
          </Button>
          <Button
            variant="error"
            onClick={onConfirm}
            loading={isDeleting}
            disabled={isDeleting}
          >
            {modalConfirmText}
          </Button>
        </div>
      </div>
    </Modal>
  );
};
