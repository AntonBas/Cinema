import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { BaseHallModal } from './BaseHallModal';

interface EditHallModalProps {
    hall: CinemaHallResponse;
    currentLayout?: { rows: number; seatsPerRow: number };
    onClose: () => void;
    onUpdate: (id: number, request: CinemaHallRequest) => Promise<void>;
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

    const hasChanges = formData.name !== hall.name ||
        formData.rows !== currentLayout?.rows ||
        formData.seatsPerRow !== currentLayout?.seatsPerRow;

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
        />
    );
};