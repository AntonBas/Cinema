import React from 'react';
import styles from './DeleteConfirmModal.module.css';

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
    itemName = '',
    itemType = 'item',
    isDeleting = false,
    confirmText,
    cancelText = 'Cancel'
}) => {
    if (!isOpen) return null;

    const modalTitle = title || `Delete ${itemType}`;
    const modalMessage = message || `Are you sure you want to delete this ${itemType}?`;
    const modalConfirmText = confirmText || `Delete ${itemType}`;

    return (
        <div className={styles.modalOverlay} role="dialog" aria-labelledby="delete-modal-title" aria-modal="true">
            <div className={styles.modal}>
                <div className={styles.icon} aria-hidden="true">🗑️</div>
                <h3 className={styles.title} id="delete-modal-title">{modalTitle}</h3>
                <p className={styles.message}>{modalMessage}</p>
                {itemName && (
                    <p className={styles.itemName}>"{itemName}"</p>
                )}
                <p className={styles.warning}>
                    This action cannot be undone.
                </p>
                <div className={styles.actions}>
                    <button
                        className={styles.cancelButton}
                        onClick={onCancel}
                        disabled={isDeleting}
                        type="button"
                    >
                        {cancelText}
                    </button>
                    <button
                        className={styles.confirmButton}
                        onClick={onConfirm}
                        disabled={isDeleting}
                        type="button"
                        aria-label={modalConfirmText}
                    >
                        {isDeleting ? 'Deleting...' : modalConfirmText}
                    </button>
                </div>
            </div>
        </div>
    );
};