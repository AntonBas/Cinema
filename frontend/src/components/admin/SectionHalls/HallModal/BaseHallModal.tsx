import React from 'react';
import { Modal, Input, Button } from '@/components/ui';
import type { CinemaHallRequest } from '@/types/cinemaHall';
import styles from './HallModal.module.css';

interface BaseHallModalProps {
    isOpen: boolean;
    title: string;
    formData: CinemaHallRequest;
    onClose: () => void;
    onSubmit: (e: React.FormEvent) => void;
    onFieldChange: <K extends keyof CinemaHallRequest>(field: K, value: CinemaHallRequest[K]) => void;
    submitButtonText: string;
    isSubmitDisabled?: boolean;
    loading?: boolean;
}

export const BaseHallModal: React.FC<BaseHallModalProps> = ({
    isOpen,
    title,
    formData,
    onClose,
    onSubmit,
    onFieldChange,
    submitButtonText,
    isSubmitDisabled = false,
    loading = false
}) => {
    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="small">
            <form onSubmit={onSubmit}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall Name *</label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => onFieldChange('name', value)}
                        placeholder="Enter hall name"
                        required={true}
                        minLength={2}
                        maxLength={25}
                        disabled={loading}
                    />
                </div>

                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={formData.name.trim().length < 2 || isSubmitDisabled || loading}
                        loading={loading}
                    >
                        {submitButtonText}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};
