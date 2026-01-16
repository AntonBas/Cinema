import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { Modal, Input, Button, Select } from '@/components/ui';
import styles from './HallModal.module.css';

interface EditHallModalProps {
    hall: CinemaHallResponse;
    currentLayout?: { rows: number; seatsPerRow: number };
    onClose: () => void;
    onUpdate: (id: number, request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

const seatTypeOptions = [
    { value: SeatType.STANDARD, label: 'Standard' },
    { value: SeatType.VIP, label: 'VIP' },
    { value: SeatType.DISABLED, label: 'Disabled' },
    { value: SeatType.COUPLE, label: 'Couple' }
];

export const EditHallModal: React.FC<EditHallModalProps> = ({
    hall,
    currentLayout,
    onClose,
    onUpdate,
    loading = false
}) => {
    const [formData, setFormData] = useState<CinemaHallRequest>({
        name: hall.name,
        rows: currentLayout?.rows || 10,
        seatsPerRow: currentLayout?.seatsPerRow || 15,
        defaultSeatType: SeatType.STANDARD
    });

    useEffect(() => {
        setFormData({
            name: hall.name,
            rows: currentLayout?.rows || 10,
            seatsPerRow: currentLayout?.seatsPerRow || 15,
            defaultSeatType: SeatType.STANDARD
        });
    }, [hall, currentLayout]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || loading) return;
        await onUpdate(hall.id, formData);
    };

    const updateField = <K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const totalSeats = (formData.rows || 0) * (formData.seatsPerRow || 0);
    const hasChanges = formData.name !== hall.name ||
        formData.rows !== currentLayout?.rows ||
        formData.seatsPerRow !== currentLayout?.seatsPerRow;

    return (
        <Modal isOpen={true} onClose={onClose} title="Edit Cinema Hall" size="medium">
            <form onSubmit={handleSubmit}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall Name *</label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => updateField('name', value)}
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
                            onChange={(value) => updateField('rows', parseInt(value) || 1)}
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
                            onChange={(value) => updateField('seatsPerRow', parseInt(value) || 1)}
                            placeholder="Seats per row"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Default Seat Type</label>
                    <Select
                        value={formData.defaultSeatType || SeatType.STANDARD}
                        onChange={(value) => updateField('defaultSeatType', value as SeatType)}
                        options={seatTypeOptions}
                        disabled={loading}
                    />
                </div>

                <div className={styles.summary}>
                    <h4 className={styles.summaryTitle}>Layout Summary</h4>
                    <div className={styles.summaryGrid}>
                        <div>Rows: <strong>{formData.rows}</strong></div>
                        <div>Seats/Row: <strong>{formData.seatsPerRow}</strong></div>
                        <div>Total: <strong>{totalSeats}</strong></div>
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button
                        variant="cancel"
                        onClick={onClose}
                        disabled={loading}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={!formData.name.trim() || !hasChanges || loading}
                        loading={loading}
                    >
                        Update Hall
                    </Button>
                </div>
            </form>
        </Modal>
    );
};