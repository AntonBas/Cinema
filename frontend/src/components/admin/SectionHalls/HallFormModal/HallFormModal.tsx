import React, { useState, useCallback } from "react";
import { Modal } from "@/components/ui/Modal/Modal";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import type { CinemaHallResponse, CinemaHallRequest } from "@/types/cinemaHall";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import styles from "./HallFormModal.module.css";

interface HallFormModalProps {
  hall?: CinemaHallResponse;
  onClose: () => void;
  onSave: (request: CinemaHallRequest) => Promise<void>;
  loading?: boolean;
}

const MIN_NAME_LENGTH = 2;

export const HallFormModal: React.FC<HallFormModalProps> = ({
  hall,
  onClose,
  onSave,
  loading = false,
}) => {
  const [name, setName] = useState(hall?.name ?? "");
  const [nameError, setNameError] = useState<string | undefined>(undefined);
  const isEditing = hall !== undefined;
  const isNameValid = name.trim().length >= MIN_NAME_LENGTH;
  const hasChanges = !isEditing || name !== hall.name;

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();
      if (!isNameValid || loading) return;
      try {
        await onSave({ name });
      } catch (err) {
        if (isApiErrorException(err) && err.isValidationError()) {
          setNameError(err.getValidationErrors().name);
        }
      }
    },
    [isNameValid, loading, name, onSave],
  );

  const handleNameChange = useCallback((value: string) => {
    setName(value);
    setNameError(undefined);
  }, []);

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      title={isEditing ? "Rename Cinema Hall" : "Create New Hall"}
      size="small"
    >
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label className={styles.label}>Hall Name *</label>
          <Input
            type="text"
            value={name}
            onChange={handleNameChange}
            placeholder="Enter hall name"
            required={true}
            minLength={MIN_NAME_LENGTH}
            maxLength={25}
            disabled={loading}
            error={nameError}
          />
        </div>

        <div className={styles.actions}>
          <Button variant="cancel" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={!isNameValid || !hasChanges || loading}
            loading={loading}
          >
            {isEditing ? "Save Changes" : "Create Hall"}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
