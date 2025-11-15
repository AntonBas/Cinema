import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types';
import { Modal, Input, Button } from '@/components/ui';

interface EditHallModalProps {
  hall: CinemaHallResponse;
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
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Edit Cinema Hall">
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1.5rem' }}>
          <label htmlFor="hallName" style={{
            display: 'block',
            color: '#ffffff',
            fontWeight: 600,
            marginBottom: '0.5rem'
          }}>
            Hall Name
          </label>
          <Input
            type="text"
            value={name}
            onChange={setName}
            placeholder="Enter hall name"
            required={true}
            maxLength={25}
            disabled={loading}
          />
        </div>

        <div style={{
          display: 'flex',
          gap: '1rem',
          justifyContent: 'flex-end',
          marginTop: '2rem',
          paddingTop: '1.5rem',
          borderTop: '1px solid #3a4051'
        }}>
          <Button
            variant="cancel"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={!name.trim() || name === hall.name || loading}
          >
            {loading ? 'Updating...' : 'Update Hall'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};