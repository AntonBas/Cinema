import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { BaseHallModal } from './BaseHallModal';

interface EditHallModalProps {
    hall: CinemaHallResponse;
    currentLayout?: { rows: number; seatsPerRow: number; coupleRows?: number[] };
    onClose: () => void;
    onUpdate: (id: number, request: CinemaHallRequest & { coupleRows?: number[] }) => Promise<void>;
    loading?: boolean;
}

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
    const [coupleRows, setCoupleRows] = useState<number[]>(currentLayout?.coupleRows || []);

    useEffect(() => {
        setFormData({
            name: hall.name,
            rows: currentLayout?.rows || 10,
            seatsPerRow: currentLayout?.seatsPerRow || 15,
            defaultSeatType: SeatType.STANDARD
        });
        setCoupleRows(currentLayout?.coupleRows || []);
    }, [hall, currentLayout]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || loading) return;
        await onUpdate(hall.id, { ...formData, coupleRows });
    };

    const updateField = <K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (field === 'rows') {
            setCoupleRows(prev => prev.filter(row => row <= (value as number)));
        }
    };

    const hasChanges = formData.name !== hall.name ||
        formData.rows !== currentLayout?.rows ||
        formData.seatsPerRow !== currentLayout?.seatsPerRow ||
        JSON.stringify(coupleRows) !== JSON.stringify(currentLayout?.coupleRows || []);

    return (
        <BaseHallModal
            isOpen={true}
            title="Edit Cinema Hall"
            formData={formData}
            onClose={onClose}
            onSubmit={handleSubmit}
            onFieldChange={updateField}
            submitButtonText="Update Hall"
            isSubmitDisabled={!hasChanges}
            loading={loading}
            showDefaultSeatType={false}
            coupleRows={coupleRows}
            onCoupleRowsChange={setCoupleRows}
        />
    );
};