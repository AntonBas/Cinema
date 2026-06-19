import React, { useCallback } from 'react';
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
    coupleRows?: number[];
    onCoupleRowsChange?: (rows: number[]) => void;
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
    showDefaultSeatType = true,
    coupleRows = [],
    onCoupleRowsChange
}) => {
    const totalSeats = (formData.rows || 0) * (formData.seatsPerRow || 0);
    const isEvenSeatsPerRow = (formData.seatsPerRow || 0) % 2 === 0;

    const handleCoupleRowToggle = useCallback((row: number) => {
        if (!onCoupleRowsChange) return;
        const newRows = coupleRows.includes(row)
            ? coupleRows.filter(r => r !== row)
            : [...coupleRows, row].sort((a, b) => a - b);
        onCoupleRowsChange(newRows);
    }, [coupleRows, onCoupleRowsChange]);

    const rowsArray = Array.from({ length: formData.rows || 0 }, (_, i) => i + 1);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="large">
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
                            onChange={(value) => onFieldChange('rows', value ? parseInt(value) : 1)}
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
                            onChange={(value) => onFieldChange('seatsPerRow', value ? parseInt(value) : 1)}
                            placeholder="Seats per row"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>
                </div>

                {!isEvenSeatsPerRow && coupleRows.length > 0 && (
                    <div className={styles.warning}>
                        ⚠️ Seats per row must be even when using COUPLE seats
                    </div>
                )}

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

                {formData.rows && formData.rows > 0 && (
                    <div className={styles.formGroup}>
                        <label className={styles.label}>COUPLE Rows</label>
                        <div className={styles.coupleRowsGrid}>
                            {rowsArray.map(row => (
                                <label key={row} className={styles.coupleRowLabel}>
                                    <input
                                        type="checkbox"
                                        checked={coupleRows.includes(row)}
                                        onChange={() => handleCoupleRowToggle(row)}
                                        disabled={loading}
                                    />
                                    <span>Row {row}</span>
                                </label>
                            ))}
                        </div>
                        <small className={styles.hint}>
                            Select rows that should have COUPLE seats (2 seats per unit)
                        </small>
                    </div>
                )}

                <div className={styles.summary}>
                    <h4 className={styles.summaryTitle}>Layout Summary</h4>
                    <div className={styles.summaryGrid}>
                        <div>Rows: <strong>{formData.rows}</strong></div>
                        <div>Seats/Row: <strong>{formData.seatsPerRow}</strong></div>
                        <div>Total: <strong>{totalSeats}</strong></div>
                        {coupleRows.length > 0 && (
                            <div>COUPLE Rows: <strong>{coupleRows.join(', ')}</strong></div>
                        )}
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={
                            !formData.name ||
                            isSubmitDisabled ||
                            loading ||
                            (coupleRows.length > 0 && (formData.seatsPerRow || 0) % 2 !== 0)
                        }
                        loading={loading}
                    >
                        {submitButtonText}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};