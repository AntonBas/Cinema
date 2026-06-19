import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Modal, Input, Button } from '@/components/ui';
import styles from './GenreFormModal.module.css';

interface GenreFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (name: string) => void;
    initialName?: string;
    loading?: boolean;
    isEditing?: boolean;
}

const MAX_NAME_LENGTH = 30;
const WARNING_THRESHOLD = 5;

export const GenreFormModal: React.FC<GenreFormModalProps> = ({
    isOpen,
    onClose,
    onSubmit,
    initialName = '',
    loading = false,
    isEditing = false
}) => {
    const [name, setName] = useState(initialName);
    const [error, setError] = useState('');

    useEffect(() => {
        if (isOpen) {
            setName(initialName);
            setError('');
        }
    }, [isOpen, initialName]);

    const validateName = useCallback((value: string): string | null => {
        const trimmed = value.trim();

        if (!trimmed) {
            return 'Genre name is required';
        }

        if (trimmed.length < 2) {
            return 'Genre name must be at least 2 characters';
        }

        if (trimmed.length > MAX_NAME_LENGTH) {
            return `Genre name cannot exceed ${MAX_NAME_LENGTH} characters`;
        }

        return null;
    }, []);

    const handleSubmit = useCallback((e: React.FormEvent) => {
        e.preventDefault();

        const validationError = validateName(name);
        if (validationError) {
            setError(validationError);
            return;
        }

        onSubmit(name.trim());
    }, [name, onSubmit, validateName]);

    const handleChange = useCallback((value: string) => {
        setName(value);
        if (error) {
            setError('');
        }
    }, [error]);

    const handleClose = useCallback(() => {
        if (!loading) {
            onClose();
            setName('');
            setError('');
        }
    }, [loading, onClose]);

    const remainingChars = useMemo(() =>
        MAX_NAME_LENGTH - name.length,
        [name.length]
    );

    const isValid = useMemo(() => {
        const trimmed = name.trim();
        return trimmed.length >= 2 && trimmed.length <= MAX_NAME_LENGTH;
    }, [name]);

    const isWarning = remainingChars < WARNING_THRESHOLD;

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title={isEditing ? 'Edit Genre' : 'Add New Genre'}
            size="small"
        >
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.inputWrapper}>
                    <Input
                        type="text"
                        placeholder="Enter genre name"
                        value={name}
                        onChange={handleChange}
                        error={error}
                        required
                        maxLength={MAX_NAME_LENGTH}
                        disabled={loading}
                        className={styles.input}
                        autoFocus
                    />
                    <div className={`${styles.charCount} ${isWarning ? styles.warning : ''}`}>
                        {remainingChars} character{remainingChars !== 1 ? 's' : ''} remaining
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button
                        type="button"
                        variant="cancel"
                        onClick={handleClose}
                        disabled={loading}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        loading={loading}
                        disabled={loading || !isValid}
                    >
                        {isEditing ? 'Update' : 'Create'} Genre
                    </Button>
                </div>
            </form>
        </Modal>
    );
};