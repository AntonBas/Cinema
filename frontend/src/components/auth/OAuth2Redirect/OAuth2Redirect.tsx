import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';

export const OAuth2Redirect: React.FC = () => {
    const navigate = useNavigate();
    const { oauth2Success } = useAuthActions();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;
        processed.current = true;

        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        const userId = params.get('userId');
        const email = params.get('email');

        const handleOAuth2 = async () => {
            if (token && userId && email) {
                try {
                    await oauth2Success(token, Number(userId), email);
                } catch (error) {
                    navigate('/login?error=oauth2_failed');
                }
            } else {
                navigate('/login?error=invalid_oauth2_response');
            }
        };

        handleOAuth2();
    }, [navigate, oauth2Success]);

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh'
        }}>
            <LoadingSpinner text="Completing login..." />
        </div>
    );
};