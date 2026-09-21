import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import { BaseHallModal } from './BaseHallModal';

interface EditHallModalProps {
    hall: CinemaHallResponse;
    onClose: () => void;
    onUpdate: (id: number, request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

export const EditHallModal: React.FC<EditHallModalProps> = ({
    hall,
    onClose,
    onUpdate,
    loading = false
}) => {
    const [formData, setFormData] = useState<CinemaHallRequest>({ name: hall.name });
    const [nameError, setNameError] = useState<string | undefined>(undefined);

    useEffect(() => {
        setFormData({ name: hall.name });
    }, [hall]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.name.trim().length < 2 || loading) return;
        try {
            await onUpdate(hall.id, formData);
        } catch (err) {
            if (isApiErrorException(err) && err.isValidationError()) {
                setNameError(err.getValidationErrors().name);
            }
        }
    }, [formData, hall.id, loading, onUpdate]);

    const updateField = useCallback(<K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        setNameError(undefined);
    }, []);

    const hasChanges = useMemo(() => formData.name !== hall.name, [formData, hall]);

    return (
        <BaseHallModal
            isOpen={true}
            title="Rename Cinema Hall"
            formData={formData}
            onClose={onClose}
            onSubmit={handleSubmit}
            onFieldChange={updateField}
            submitButtonText="Save Name"
            isSubmitDisabled={!hasChanges}
            loading={loading}
            nameError={nameError}
        />
    );
};
