import React, { useState } from 'react';
import styles from './CreateHallModal.module.css';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (name: string) => Promise<void>;
    loading?: boolean;
}

export const CreateHallModal: React.FC<CreateHallModalProps> = ({
    onClose,
    onCreate,
    loading = false
}) => {
    const [name, setName] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim() || loading) return;

        try {
            await onCreate(name.trim());
        } catch (err) {
        }
    };

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.header}>
                    <h2>Create New Hall</h2>
                    <button className={styles.closeButton} onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.field}>
                        <label htmlFor="hallName" className={styles.label}>Hall Name</label>
                        <input
                            id="hallName"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Enter hall name"
                            required
                            minLength={2}
                            maxLength={25}
                            className={styles.input}
                            disabled={loading}
                        />
                    </div>

                    <div className={styles.actions}>
                        <button
                            type="button"
                            className={styles.cancelButton}
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className={styles.createButton}
                            disabled={!name.trim() || loading}
                        >
                            {loading ? 'Creating...' : 'Create Hall'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};