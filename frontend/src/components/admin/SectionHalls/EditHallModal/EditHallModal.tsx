import React, { useState, useEffect } from 'react';
import type { CinemaHallDto, CinemaHallRequest } from '@/types';
import styles from './EditHallModal.module.css';

interface EditHallModalProps {
  hall: CinemaHallDto;
  onClose: () => void;
  onUpdate: (id: number, request: CinemaHallRequest) => Promise<void>;
  loading?: boolean;
}

export const EditHallModal: React.FC<EditHallModalProps> = ({
  hall,
  onClose,
  onUpdate,
  loading = false
}) => {
  const [name, setName] = useState(hall.name);

  useEffect(() => {
    setName(hall.name);
  }, [hall]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || name === hall.name || loading) return;

    try {
      await onUpdate(hall.id!, { name: name.trim() });
    } catch (err) {
      // Error handled by parent
    }
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>Edit Cinema Hall</h2>
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
              className={styles.updateButton}
              disabled={!name.trim() || name === hall.name || loading}
            >
              {loading ? 'Updating...' : 'Update Hall'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};