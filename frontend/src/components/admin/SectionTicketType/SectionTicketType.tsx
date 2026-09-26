import { useState, useEffect, useCallback } from "react";
import { useTicketType } from "@/hooks/features/ticketType/useTicketType";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { TicketTypeTable } from "./TicketTypeTable/TicketTypeTable";
import { TicketTypeFilters } from "./TicketTypeFilters/TicketTypeFilters";
import { TicketTypeFormModal } from "./TicketTypeFormModal/TicketTypeFormModal";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./SectionTicketType.module.css";
import {
  TicketTypeCategoryDisplay,
  type TicketTypeResponse,
  type TicketTypeCategory,
} from "@/types/ticketType";

type StatusFilter = "all" | "active" | "inactive";

const STATUS_FILTERS: StatusFilter[] = ["all", "active", "inactive"];
const CATEGORY_FILTERS = [
  "all",
  ...Object.keys(TicketTypeCategoryDisplay),
] as Array<TicketTypeCategory | "all">;

export const SectionTicketType = () => {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingTicketType, setEditingTicketType] =
    useState<TicketTypeResponse | null>(null);

  const { page, query, getParam, setFilters, setPage, setSearch } =
    useUrlParams();
  const statusFilter = parseEnumParam(
    getParam("status"),
    STATUS_FILTERS,
    "all",
  );
  const categoryFilter = parseEnumParam(
    getParam("category"),
    CATEGORY_FILTERS,
    "all",
  );

  const {
    ticketTypes: ticketTypesData,
    pagination,
    loading,
    getAll,
    remove,
    toggleActive,
  } = useTicketType();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadTicketTypes = useCallback(() => {
    getAll({
      page,
      size: DEFAULT_PAGE_SIZE_COMPACT,
      query,
      active: statusFilter === "all" ? undefined : statusFilter === "active",
      category: categoryFilter === "all" ? undefined : categoryFilter,
    }).catch(() => {});
  }, [page, query, statusFilter, categoryFilter, getAll]);

  useEffect(() => {
    loadTicketTypes();
  }, [loadTicketTypes]);

  const handleStatusChange = useCallback(
    (status: StatusFilter) => {
      setFilters({ status: toUrlEnumValue(status, "all") });
    },
    [setFilters],
  );

  const handleCategoryChange = useCallback(
    (category: TicketTypeCategory | "all") => {
      setFilters({ category: toUrlEnumValue(category, "all") });
    },
    [setFilters],
  );

  const handleCreateSuccess = useCallback(() => {
    setShowCreateModal(false);
    loadTicketTypes();
  }, [loadTicketTypes]);

  const handleEditSuccess = useCallback(() => {
    setEditingTicketType(null);
    loadTicketTypes();
  }, [loadTicketTypes]);

  const handleDelete = useCallback(
    async (id: number) => {
      await remove(id);
      if (ticketTypesData.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        loadTicketTypes();
      }
    },
    [ticketTypesData.length, page, setPage, loadTicketTypes, remove],
  );

  const handleToggleActive = useCallback(
    async (id: number) => {
      await toggleActive(id);
      loadTicketTypes();
    },
    [loadTicketTypes, toggleActive],
  );

  if (showDelayedLoading && !ticketTypesData.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading ticket types..." />
      </div>
    );
  }

  return (
    <div className={styles.section}>
      <PageHeader
        title="Ticket Types"
        subtitle="Configure ticket pricing and categories"
        actions={
          <Button variant="primary" onClick={() => setShowCreateModal(true)}>
            Create Ticket Type
          </Button>
        }
      />

      <div className={styles.content}>
        <TicketTypeFilters
          searchValue={query}
          onSearchChange={setSearch}
          statusFilter={statusFilter}
          onStatusChange={handleStatusChange}
          categoryFilter={categoryFilter}
          onCategoryChange={handleCategoryChange}
        />

        {pagination && pagination.totalElements > 0 && (
          <div className={styles.resultsInfo}>
            Showing {pagination.number * pagination.size + 1}-
            {Math.min(
              (pagination.number + 1) * pagination.size,
              pagination.totalElements,
            )}{" "}
            of {pagination.totalElements} ticket types
            {query && ` for "${query}"`}
          </div>
        )}

        {ticketTypesData.length === 0 ? (
          <EmptyState title="No Ticket Types Found" />
        ) : (
          <>
            <TicketTypeTable
              ticketTypes={ticketTypesData}
              onEdit={setEditingTicketType}
              onDelete={handleDelete}
              onToggleActive={handleToggleActive}
              loading={loading}
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
          </>
        )}
      </div>

      {showCreateModal && (
        <TicketTypeFormModal
          isOpen={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          onSuccess={handleCreateSuccess}
        />
      )}

      {editingTicketType && (
        <TicketTypeFormModal
          isOpen={!!editingTicketType}
          onClose={() => setEditingTicketType(null)}
          onSuccess={handleEditSuccess}
          ticketType={editingTicketType}
        />
      )}
    </div>
  );
};
