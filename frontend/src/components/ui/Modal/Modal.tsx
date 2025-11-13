import React from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.css';
import clsx from 'clsx';

export type ModalSize = 'small' | 'medium' | 'large';

export interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    children: React.ReactNode;
    title?: string;
    size?: ModalSize;
    className?: string;
}

export const Modal: React.FC<ModalProps> = ({
    isOpen,
    onClose,
    children,
    title,
    size = 'medium',
    className = ''
}) => {
    if (!isOpen) return null;

    const modalClass = clsx(
        styles.modal,
        styles[size],
        className
    );

    const handleOverlayClick = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    const modalContent = (
        <div className={styles.overlay} onClick={handleOverlayClick}>
            <div className={modalClass}>
                {title && (
                    <div className={styles.header}>
                        <h2 className={styles.title}>{title}</h2>
                        <button className={styles.closeButton} onClick={onClose}>
                            ×
                        </button>
                    </div>
                )}
                <div className={styles.content}>
                    {children}
                </div>
            </div>
        </div>
    );

    return createPortal(modalContent, document.body);
};