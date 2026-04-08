import React, { useEffect, useState } from 'react';
import { BaseSessionModal } from './BaseSessionModal';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest } from '@/types/session';
import type { CinemaHallResponse } from '@/types/cinemaHall';

interface EditSessionModalProps {
    isOpen: boolean;
    session: SessionAdminResponse;
    onSave: (id: number, data: SessionUpdateRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
    halls: CinemaHallResponse[];
    hallsLoading: boolean;
}

export const EditSessionModal: React.FC<EditSessionModalProps> = ({
    isOpen,
    session,
    onSave,
    onClose,
    loading,
    halls,
    hallsLoading
}) => {
    const [modalSession, setModalSession] = useState(session);

    useEffect(() => {
        if (isOpen && session) {
            setModalSession(session);
        }
    }, [isOpen, session]);

    const handleSave = async (data: SessionCreateRequest | SessionUpdateRequest) => {
        await onSave(session.id, data as SessionUpdateRequest);
        onClose();
    };

    return (
        <BaseSessionModal
            isOpen={isOpen}
            onClose={onClose}
            onSave={handleSave}
            loading={loading}
            initialData={{
                startTime: modalSession.startTime,
                basePrice: modalSession.basePrice,
                movieId: modalSession.movieId,
                hallId: modalSession.hallId,
                movieTitle: modalSession.movieTitle,
                movieDuration: modalSession.movieDuration
            }}
            title="Edit Session"
            submitText="Update Session"
            isEditing={true}
            session={modalSession}
            halls={halls}
            hallsLoading={hallsLoading}
        />
    );
};