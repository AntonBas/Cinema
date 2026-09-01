import React, { useEffect } from "react";
import { Modal } from "@/components/ui";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useEntityAuditHistory } from "@/hooks/features/audit/useEntityAuditHistory";
import { AuditLogsTable } from "../AuditLogsTable/AuditLogsTable";
import { getEntityTypeDisplay } from "@/types/audit";
import styles from "./EntityHistoryModal.module.css";

interface EntityHistoryModalProps {
  entityType: string;
  entityId: number;
  onClose: () => void;
}

export const EntityHistoryModal: React.FC<EntityHistoryModalProps> = ({
  entityType,
  entityId,
  onClose,
}) => {
  const { history, loading, error, getEntityHistory } =
    useEntityAuditHistory();

  useEffect(() => {
    getEntityHistory(entityType, entityId);
  }, [entityType, entityId, getEntityHistory]);

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      title={`${getEntityTypeDisplay(entityType)} #${entityId} History`}
      size="large"
    >
      <div className={styles.body}>
        {loading ? (
          <LoadingSpinner text="Loading history..." />
        ) : error || history.length === 0 ? (
          <p className={styles.empty}>
            No audit history found for this entity.
          </p>
        ) : (
          <AuditLogsTable logs={history} />
        )}
      </div>
    </Modal>
  );
};
