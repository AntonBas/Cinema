import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';

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
        <Modal isOpen={true} onClose={onClose} title="Create New Hall">
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
                        disabled={!name.trim() || loading}
                    >
                        {loading ? 'Creating...' : 'Create Hall'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};