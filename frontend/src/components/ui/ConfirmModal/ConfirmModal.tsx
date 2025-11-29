import React from 'react';
import { Modal } from '../Modal';
import { Button } from '../Button';
import styles from './ConfirmModal.module.css';

interface ConfirmModalProps {
    isOpen: boolean;
    onConfirm: () => void;
    onCancel: () => void;
    title: string;
    message: string;
    confirmText: string;
    cancelText?: string;
    variant?: 'primary' | 'success' | 'error';
    isLoading?: boolean;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
    isOpen,
    onConfirm,
    onCancel,
    title,
    message,
    confirmText,
    cancelText = 'Cancel',
    variant = 'primary',
    isLoading = false
}) => {
    const getButtonVariant = () => {
        switch (variant) {
            case 'success': return 'success';
            case 'error': return 'error';
            default: return 'primary';
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onCancel}
            title={title}
            size="small"
        >
            <div className={styles.content}>
                <p className={styles.message}>{message}</p>
                <div className={styles.actions}>
                    <Button
                        variant="cancel"
                        onClick={onCancel}
                        disabled={isLoading}
                    >
                        {cancelText}
                    </Button>
                    <Button
                        variant={getButtonVariant()}
                        onClick={onConfirm}
                        loading={isLoading}
                    >
                        {confirmText}
                    </Button>
                </div>
            </div>
        </Modal>
    );
};