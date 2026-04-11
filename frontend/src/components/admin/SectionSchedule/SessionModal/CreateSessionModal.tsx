import React from 'react';
import { BaseSessionModal } from './BaseSessionModal';
import type { SessionRequest } from '@/types/session';
import type { CinemaHallListResponse } from '@/types/cinemaHall';

interface CreateSessionModalProps {
    isOpen: boolean;
    onSave: (data: SessionRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
    halls: CinemaHallListResponse[];
}

export const CreateSessionModal: React.FC<CreateSessionModalProps> = ({
    isOpen,
    onSave,
    onClose,
    loading,
    halls,
}) => {
    return (
        <BaseSessionModal
            isOpen={isOpen}
            onClose={onClose}
            onSave={onSave}
            loading={loading}
            title="Create New Session"
            submitText="Create Session"
            halls={halls}
        />
    );
};