import React, { useState, useEffect } from 'react';
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

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!name.trim()) {
            setError('Genre name is required');
            return;
        }

        if (name.trim().length > 50) {
            setError('Genre name cannot exceed 50 characters');
            return;
        }

        onSubmit(name.trim());
    };

    const handleClose = () => {
        if (!loading) {
            onClose();
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title={isEditing ? 'Edit Genre' : 'Add New Genre'}
            size="small"
        >
            <form onSubmit={handleSubmit} className={styles.form}>
                <Input
                    type="text"
                    placeholder="Genre name"
                    value={name}
                    onChange={(value) => {
                        setName(value);
                        setError('');
                    }}
                    error={error}
                    required
                    maxLength={50}
                    disabled={loading}
                    className={styles.input}
                />
                <div className={styles.hint}>Maximum 50 characters</div>

                <div className={styles.actions}>
                    <Button
                        type="button"
                        variant="cancel"
                        onClick={handleClose}
                        disabled={loading}
                        className={styles.cancelButton}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        loading={loading}
                        disabled={loading}
                        className={styles.submitButton}
                    >
                        {isEditing ? 'Update' : 'Create'} Genre
                    </Button>
                </div>
            </form>
        </Modal>
    );
};