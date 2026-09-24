import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useSession } from "@/hooks/features/session/useSession";
import { useCinemaHall } from "@/hooks/features/cinemaHall/useCinemaHall";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import {
  parseNumberParam,
  parseOptionalEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { SessionFilters } from "./SessionFilters/SessionFilters";
import { SessionTable } from "./SessionTable/SessionTable";
import { SessionFormModal } from "./SessionFormModal/SessionFormModal";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { ConfirmModal } from "@/components/ui/ConfirmModal/ConfirmModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import {
  SessionStatusDisplay,
  type SessionAdminResponse,
  type SessionRequest,
  type CinemaSessionStatus,
} from "@/types/session";
import type { CinemaHallListResponse } from "@/types/cinemaHall";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionSchedule.module.css";

interface FiltersState {
  dateFrom?: string;
  dateTo?: string;
  hallId?: number;
  movieTitle?: string;
  status?: CinemaSessionStatus;
}

const SESSION_STATUSES = Object.keys(
  SessionStatusDisplay,
) as CinemaSessionStatus[];

const FILTER_URL_KEYS: Record<keyof FiltersState, string> = {
  dateFrom: "from",
  dateTo: "to",
  hallId: "hall",
  movieTitle: "movie",
  status: "status",
};

export const SectionSchedule: React.FC = () => {
  const { halls, getAll } = useCinemaHall();
  const { page, sort, getParam, setFilters, setPage, setSort } = useUrlParams();
  const dateFrom = getParam(FILTER_URL_KEYS.dateFrom);
  const dateTo = getParam(FILTER_URL_KEYS.dateTo);
  const hallParam = getParam(FILTER_URL_KEYS.hallId);
  const movieTitle = getParam(FILTER_URL_KEYS.movieTitle);
  const statusParam = getParam(FILTER_URL_KEYS.status);

  const filters = useMemo<FiltersState>(
    () => ({
      dateFrom,
      dateTo,
      hallId: parseNumberParam(hallParam),
      movieTitle,
      status: parseOptionalEnumParam(statusParam, SESSION_STATUSES),
    }),
    [dateFrom, dateTo, hallParam, movieTitle, statusParam],
  );

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingSession, setEditingSession] =
    useState<SessionAdminResponse | null>(null);
  const [deletingSession, setDeletingSession] =
    useState<SessionAdminResponse | null>(null);
  const [cancellingSession, setCancellingSession] =
    useState<SessionAdminResponse | null>(null);
  const [reactivatingSession, setReactivatingSession] =
    useState<SessionAdminResponse | null>(null);

  const {
    adminSessions,
    pagination,
    loading,
    getAdminSessions,
    create,
    update,
    remove,
    cancel,
    reactivate,
  } = useSession();

  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  useEffect(() => {
    getAll();
  }, [getAll]);

  const reloadSessions = useCallback(
    () =>
      getAdminSessions({
        page,
        size: DEFAULT_PAGE_SIZE_COMPACT,
        sort,
        ...filters,
      }),
    [page, sort, filters, getAdminSessions],
  );

  useEffect(() => {
    reloadSessions();
  }, [reloadSessions]);

  const handleFilterChange = useCallback(
    <K extends keyof FiltersState>(key: K, value: FiltersState[K]) => {
      const urlValue =
        key === "status"
          ? toUrlEnumValue(value as CinemaSessionStatus | undefined)
          : value;
      setFilters({ [FILTER_URL_KEYS[key]]: urlValue });
    },
    [setFilters],
  );

  const handleClearFilters = useCallback(() => {
    setFilters({ sort }, { reset: true });
  }, [sort, setFilters]);

  const handleCreateSession = useCallback(
    async (data: SessionRequest) => {
      await create(data);
      setIsCreateModalOpen(false);
      reloadSessions();
    },
    [create, reloadSessions],
  );

  const handleUpdateSession = useCallback(
    async (id: number, data: SessionRequest) => {
      await update(id, data);
      setEditingSession(null);
      reloadSessions();
    },
    [update, reloadSessions],
  );

  const handleDeleteSession = useCallback(async () => {
    if (!deletingSession) return;
    await remove(deletingSession.id);
    setDeletingSession(null);
    reloadSessions();
  }, [deletingSession, remove, reloadSessions]);

  const handleCancelSession = useCallback(async () => {
    if (!cancellingSession) return;
    await cancel(cancellingSession.id);
    setCancellingSession(null);
    reloadSessions();
  }, [cancellingSession, cancel, reloadSessions]);

  const handleReactivateSession = useCallback(async () => {
    if (!reactivatingSession) return;
    await reactivate(reactivatingSession.id);
    setReactivatingSession(null);
    reloadSessions();
  }, [reactivatingSession, reactivate, reloadSessions]);

  const activeFilterCount = useMemo(() => {
    return Object.values(filters).filter((v) => v !== undefined && v !== "")
      .length;
  }, [filters]);

  const hasActiveFilters = activeFilterCount > 0;

  const hallsForSelect: CinemaHallListResponse[] = halls || [];

  if (showDelayedLoading && !adminSessions.length) {
    return (
      <div className={styles.loadingContainer}>
        <LoadingSpinner text="Loading sessions..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Schedule"
        subtitle="Manage movie sessions, showtimes, and schedules"
        actions={
          <Button
            variant="primary"
            onClick={() => setIsCreateModalOpen(true)}
            disabled={loading}
          >
            Add Session
          </Button>
        }
      />

      <SessionFilters
        filters={filters}
        onDateFromChange={(v) => handleFilterChange("dateFrom", v)}
        onDateToChange={(v) => handleFilterChange("dateTo", v)}
        onHallChange={(v) => handleFilterChange("hallId", v)}
        onMovieTitleChange={(v) => handleFilterChange("movieTitle", v)}
        onStatusChange={(v) => handleFilterChange("status", v)}
        sort={sort}
        onSortChange={setSort}
        onClearFilters={handleClearFilters}
        halls={hallsForSelect}
      />

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} sessions
          {hasActiveFilters && " (filtered)"}
        </div>
      )}

      <div className={styles.tableSection}>
        <SessionTable
          sessions={adminSessions}
          onEdit={setEditingSession}
          onDelete={setDeletingSession}
          onCancel={setCancellingSession}
          onReactivate={setReactivatingSession}
        />
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationSection}>
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

      <SessionFormModal
        isOpen={isCreateModalOpen}
        onSave={handleCreateSession}
        onClose={() => setIsCreateModalOpen(false)}
        loading={loading}
        halls={hallsForSelect}
      />

      {editingSession && (
        <SessionFormModal
          isOpen={!!editingSession}
          session={editingSession}
          onSave={(data) => handleUpdateSession(editingSession.id, data)}
          onClose={() => setEditingSession(null)}
          loading={loading}
          halls={hallsForSelect}
        />
      )}

      <DeleteConfirmModal
        isOpen={!!deletingSession}
        onConfirm={handleDeleteSession}
        onCancel={() => setDeletingSession(null)}
        itemName={deletingSession?.movieTitle}
        itemType="session"
        isDeleting={loading}
      />

      <ConfirmModal
        isOpen={!!cancellingSession}
        onConfirm={handleCancelSession}
        onCancel={() => setCancellingSession(null)}
        title="Cancel Session"
        message={`Are you sure you want to cancel the session "${cancellingSession?.movieTitle}"? Pending bookings will be cancelled and every sold ticket refunded in full — reactivating the session later does not undo this.`}
        confirmText="Cancel Session"
        variant="error"
        isLoading={loading}
      />

      <ConfirmModal
        isOpen={!!reactivatingSession}
        onConfirm={handleReactivateSession}
        onCancel={() => setReactivatingSession(null)}
        title="Reactivate Session"
        message={`Are you sure you want to reactivate the session "${reactivatingSession?.movieTitle}"?`}
        confirmText="Reactivate"
        variant="success"
        isLoading={loading}
      />
    </div>
  );
};
