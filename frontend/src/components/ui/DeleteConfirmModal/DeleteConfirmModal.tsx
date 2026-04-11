import React from 'react';
import { Modal } from '../Modal/Modal';
import { Button } from '../Button/Button';
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
    const modalTitle = title || `Delete ${itemType}`;
    const modalMessage = message || `Are you sure you want to delete this ${itemType}?`;
    const modalConfirmText = confirmText || `Delete ${itemType}`;

    return (
        <Modal isOpen={isOpen} onClose={onCancel} title={modalTitle} size="small">
            <div className={styles.content}>
                <div className={styles.icon} aria-hidden="true">🗑️</div>
                <p className={styles.message}>{modalMessage}</p>
                {itemName && <p className={styles.itemName}>"{itemName}"</p>}
                <p className={styles.warning}>This action cannot be undone.</p>
                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onCancel} disabled={isDeleting}>
                        {cancelText}
                    </Button>
                    <Button variant="error" onClick={onConfirm} loading={isDeleting} disabled={isDeleting}>
                        {modalConfirmText}
                    </Button>
                </div>
            </div>
        </Modal>
    );
};