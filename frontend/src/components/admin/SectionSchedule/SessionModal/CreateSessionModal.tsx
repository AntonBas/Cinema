import React from 'react';
import { BaseSessionModal } from './BaseSessionModal';
import type { SessionCreateRequest, SessionUpdateRequest } from '@/types/session';
import type { CinemaHallResponse } from '@/types/cinemaHall';

interface CreateSessionModalProps {
    isOpen: boolean;
    onSave: (data: SessionCreateRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
    halls: CinemaHallResponse[];
    hallsLoading: boolean;
}

export const CreateSessionModal: React.FC<CreateSessionModalProps> = ({
    isOpen,
    onSave,
    onClose,
    loading,
    halls,
    hallsLoading
}) => {
    const handleSave = (data: SessionCreateRequest | SessionUpdateRequest) => {
        return onSave(data as SessionCreateRequest);
    };

    return (
        <BaseSessionModal
            isOpen={isOpen}
            onClose={onClose}
            onSave={handleSave}
            loading={loading}
            title="Create New Session"
            submitText="Create Session"
            isEditing={false}
            halls={halls}
            hallsLoading={hallsLoading}
        />
    );
};