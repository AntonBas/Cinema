import React from 'react';
import { BaseSessionModal } from './BaseSessionModal';
import type { SessionAdminResponse, SessionRequest } from '@/types/session';
import type { CinemaHallListResponse } from '@/types/cinemaHall';

interface EditSessionModalProps {
    isOpen: boolean;
    session: SessionAdminResponse;
    onSave: (id: number, data: SessionRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
    halls: CinemaHallListResponse[];
}

export const EditSessionModal: React.FC<EditSessionModalProps> = ({
    isOpen,
    session,
    onSave,
    onClose,
    loading,
    halls,
}) => {
    const handleSave = async (data: SessionRequest) => {
        await onSave(session.id, data);
    };

    return (
        <BaseSessionModal
            isOpen={isOpen}
            onClose={onClose}
            onSave={handleSave}
            loading={loading}
            initialData={{
                startTime: session.startTime,
                basePrice: session.basePrice,
                movieId: session.movieId,
                hallId: session.hallId,
                movieTitle: session.movieTitle,
            }}
            title="Edit Session"
            submitText="Update Session"
            halls={halls}
        />
    );
};