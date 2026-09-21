import React, { useState, useCallback } from 'react';
import type { CinemaHallRequest } from '@/types/cinemaHall';
import { BaseHallModal } from './BaseHallModal';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

export const CreateHallModal: React.FC<CreateHallModalProps> = ({
    onClose,
    onCreate,
    loading = false
}) => {
    const [formData, setFormData] = useState<CinemaHallRequest>({ name: '' });

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.name.trim().length < 2 || loading) return;
        await onCreate(formData);
    }, [formData, loading, onCreate]);

    const updateField = useCallback(<K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    }, []);

    return (
        <BaseHallModal
            isOpen={true}
            title="Create New Hall"
            formData={formData}
            onClose={onClose}
            onSubmit={handleSubmit}
            onFieldChange={updateField}
            submitButtonText="Create Hall"
            loading={loading}
        />
    );
};
