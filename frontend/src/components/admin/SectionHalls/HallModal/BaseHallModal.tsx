import React from 'react';
import { Modal, Input, Button, Select } from '@/components/ui';
import { SeatType } from '@/types/seat';
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
    showDefaultSeatType?: boolean;
}

const seatTypeOptions = [
    { value: SeatType.STANDARD, label: 'Standard' },
    { value: SeatType.VIP, label: 'VIP' },
    { value: SeatType.COUPLE, label: 'Couple' }
];

export const BaseHallModal: React.FC<BaseHallModalProps> = ({
    isOpen,
    title,
    formData,
    onClose,
    onSubmit,
    onFieldChange,
    submitButtonText,
    isSubmitDisabled = false,
    loading = false,
    showDefaultSeatType = true
}) => {
    const totalSeats = (formData.rows || 0) * (formData.seatsPerRow || 0);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="medium">
            <form onSubmit={onSubmit}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall Name *</label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => onFieldChange('name', value)}
                        placeholder="Enter hall name"
                        required={true}
                        maxLength={25}
                        disabled={loading}
                    />
                </div>

                <div className={styles.rowContainer}>
                    <div>
                        <label className={styles.label}>Number of Rows *</label>
                        <Input
                            type="number"
                            value={formData.rows?.toString() || ''}
                            onChange={(value) => onFieldChange('rows', parseInt(value) || 1)}
                            placeholder="Rows"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>

                    <div>
                        <label className={styles.label}>Seats Per Row *</label>
                        <Input
                            type="number"
                            value={formData.seatsPerRow?.toString() || ''}
                            onChange={(value) => onFieldChange('seatsPerRow', parseInt(value) || 1)}
                            placeholder="Seats per row"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>
                </div>

                {showDefaultSeatType && (
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Default Seat Type</label>
                        <Select
                            value={formData.defaultSeatType || SeatType.STANDARD}
                            onChange={(value) => onFieldChange('defaultSeatType', value as SeatType)}
                            options={seatTypeOptions}
                            disabled={loading}
                        />
                    </div>
                )}

                <div className={styles.summary}>
                    <h4 className={styles.summaryTitle}>Layout Summary</h4>
                    <div className={styles.summaryGrid}>
                        <div>Rows: <strong>{formData.rows}</strong></div>
                        <div>Seats/Row: <strong>{formData.seatsPerRow}</strong></div>
                        <div>Total: <strong>{totalSeats}</strong></div>
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={!formData.name.trim() || isSubmitDisabled || loading}
                        loading={loading}
                    >
                        {submitButtonText}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};