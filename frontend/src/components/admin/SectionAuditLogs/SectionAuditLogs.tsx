import React, { useEffect, useMemo, useState } from "react";
import { useAuditLogs } from "@/hooks/features/audit/useAuditLogs";
import {
  parseOptionalEnumParam,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { AuditLogsFilters } from "./AuditLogsFilters/AuditLogsFilters";
import { AuditLogsTable } from "./AuditLogsTable/AuditLogsTable";
import { EntityHistoryModal } from "./EntityHistoryModal/EntityHistoryModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionAuditLogs.module.css";

const ENTITY_TYPES = [
  "User",
  "Bonus",
  "Promotion",
  "TicketType",
  "Movie",
  "Session",
];

const ACTIONS = [
  "CREATED",
  "UPDATED",
  "DELETED",
  "TOGGLE",
  "CLAIMED",
  "REFUNDED",
];

export const SectionAuditLogs: React.FC = () => {
  const { page, getParam, setFilters, clearParams, setPage } = useUrlParams();
  const typeParam = getParam("type");
  const actionParam = getParam("action");
  const changedBy = getParam("changedBy");

  const filters = useMemo(
    () => ({
      entityType: parseOptionalEnumParam(typeParam, ENTITY_TYPES),
      action: parseOptionalEnumParam(actionParam, ACTIONS),
      changedBy,
    }),
    [typeParam, actionParam, changedBy],
  );

  const { auditLogs, pagination, loading, refresh } = useAuditLogs({
    filters,
    page,
  });

  const [selectedEntity, setSelectedEntity] = useState<{
    entityType: string;
    entityId: number;
  } | null>(null);

  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const handleEntityTypeChange = (value: string) => {
    setFilters({ type: value.toLowerCase() });
  };

  const handleActionChange = (value: string) => {
    setFilters({ action: value.toLowerCase() });
  };

  const handleChangedByChange = (value: string) => {
    setFilters({ changedBy: value }, { replace: true });
  };

  const hasActiveFilters = !!(
    filters.entityType ||
    filters.action ||
    filters.changedBy
  );

  useEffect(() => {
    refresh();
  }, [refresh]);

  if (showDelayedLoading && !auditLogs.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading audit logs..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Audit Logs"
        subtitle="Track all changes and user activities in the system"
      />

      <AuditLogsFilters
        entityType={filters.entityType || ""}
        action={filters.action || ""}
        changedBy={filters.changedBy || ""}
        onEntityTypeChange={handleEntityTypeChange}
        onActionChange={handleActionChange}
        onChangedByChange={handleChangedByChange}
        onClear={clearParams}
        entityTypes={ENTITY_TYPES}
        actions={ACTIONS}
      />

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} audit logs
          {hasActiveFilters && " (filtered)"}
        </div>
      )}

      <AuditLogsTable
        logs={auditLogs}
        onViewHistory={(entityType, entityId) =>
          setSelectedEntity({ entityType, entityId })
        }
      />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={pagination.number}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={setPage}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

      {selectedEntity && (
        <EntityHistoryModal
          entityType={selectedEntity.entityType}
          entityId={selectedEntity.entityId}
          onClose={() => setSelectedEntity(null)}
        />
      )}
    </div>
  );
};
