import React, { useState } from 'react';
import styles from './CreateHallModal.module.css';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (name: string) => void;
}

export const CreateHallModal: React.FC<CreateHallModalProps> = ({
    onClose,
    onCreate
}) => {
    const [name, setName] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) return;

        setLoading(true);
        try {
            await onCreate(name.trim());
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.modalHeader}>
                    <h2>Create New Hall</h2>
                    <button className={styles.closeButton} onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.field}>
                        <label htmlFor="hallName">Hall Name</label>
                        <input
                            id="hallName"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Enter hall name"
                            required
                            minLength={2}
                            maxLength={25}
                        />
                    </div>

                    <div className={styles.modalActions}>
                        <button
                            type="button"
                            className={styles.cancelButton}
                            onClick={onClose}
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