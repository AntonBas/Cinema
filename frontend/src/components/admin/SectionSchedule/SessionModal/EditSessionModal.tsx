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
    console.log('🎬 EditSessionModal render', {
        isOpen,
        sessionId: session?.id,
        sessionPrice: session?.basePrice,
        sessionStartTime: session?.startTime
    });

    const [modalSession, setModalSession] = useState(session);

    useEffect(() => {
        console.log('🎬 EditSessionModal useEffect - isOpen changed', {
            isOpen,
            sessionId: session?.id,
            sessionPrice: session?.basePrice
        });

        if (isOpen && session) {
            console.log('🎬 EditSessionModal - setting modalSession', session);
            setModalSession(session);
        }
    }, [isOpen, session]);

    const handleSave = async (data: SessionCreateRequest | SessionUpdateRequest) => {
        console.log('🎬 EditSessionModal handleSave', {
            sessionId: session.id,
            data,
            modalSessionPrice: modalSession.basePrice
        });

        await onSave(session.id, data as SessionUpdateRequest);
        console.log('🎬 EditSessionModal handleSave - after onSave, calling onClose');
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
                basePrice: modalSession.basePrice.toString(),
                movieId: modalSession.movieId.toString(),
                hallId: modalSession.hallId.toString(),
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