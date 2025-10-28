import React, { useState, useEffect } from 'react';
import type { CinemaHallDto, CinemaHallRequest } from '@/types';
import styles from './EditHallModal.module.css';

interface EditHallModalProps {
  hall: CinemaHallDto;
  onClose: () => void;
  onUpdate: (id: number, request: CinemaHallRequest) => void;
}

export const EditHallModal: React.FC<EditHallModalProps> = ({
  hall,
  onClose,
  onUpdate
}) => {
  const [name, setName] = useState(hall.name);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setName(hall.name);
  }, [hall]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || name === hall.name) return;

    setLoading(true);
    try {
      await onUpdate(hall.id!, { name: name.trim() });
    } finally {
      setLoading(false);
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