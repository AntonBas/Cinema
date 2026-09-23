import React, { useState, useEffect, useCallback, useRef } from "react";
import { UserTable } from "./UserTable/UserTable";
import { UserFilters } from "./UserFilters/UserFilters";
import { useAdminUsers } from "@/hooks/features/user/useAdminUsers";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { usePagination } from "@/hooks/common/usePagination";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import type { UserRole, VerificationStatus } from "@/types/user";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionUsers.module.css";

export const SectionUsers: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState<UserRole | "">("");
  const [verificationStatusFilter, setVerificationStatusFilter] = useState<
    VerificationStatus | ""
  >("");
  const [enabledFilter, setEnabledFilter] = useState<string>("");

  const { params, setPage } = usePagination({
    size: DEFAULT_PAGE_SIZE_COMPACT,
  });
  const { users, pagination, loading, getAll } = useAdminUsers();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const currentPage = params.page ?? 0;
  const pageSize = params.size ?? DEFAULT_PAGE_SIZE_COMPACT;

  const loadUsers = useCallback(
    (page: number = currentPage) => {
      getAll({
        query: searchQuery || undefined,
        role: roleFilter || undefined,
        verificationStatus: verificationStatusFilter || undefined,
        enabled: enabledFilter === "" ? undefined : enabledFilter === "true",
        page: page,
        size: pageSize,
      });
    },
    [
      searchQuery,
      roleFilter,
      verificationStatusFilter,
      enabledFilter,
      pageSize,
      getAll,
      currentPage,
    ],
  );

  const didLoadRef = useRef(false);
  useEffect(() => {
    if (didLoadRef.current) return;
    didLoadRef.current = true;
    loadUsers(0);
  }, [loadUsers]);

  const handleSearch = useCallback(
    (query: string) => {
      setSearchQuery(query);
      setPage(0);
      getAll({
        query: query || undefined,
        role: roleFilter || undefined,
        verificationStatus: verificationStatusFilter || undefined,
        enabled: enabledFilter === "" ? undefined : enabledFilter === "true",
        page: 0,
        size: pageSize,
      });
    },
    [
      roleFilter,
      verificationStatusFilter,
      enabledFilter,
      pageSize,
      getAll,
      setPage,
    ],
  );

  const handleRoleFilterChange = useCallback(
    (value: string) => {
      const newRole = value as UserRole | "";
      setRoleFilter(newRole);
      setPage(0);
      getAll({
        query: searchQuery || undefined,
        role: newRole || undefined,
        verificationStatus: verificationStatusFilter || undefined,
        enabled: enabledFilter === "" ? undefined : enabledFilter === "true",
        page: 0,
        size: pageSize,
      });
    },
    [
      searchQuery,
      verificationStatusFilter,
      enabledFilter,
      pageSize,
      getAll,
      setPage,
    ],
  );

  const handleVerificationStatusChange = useCallback(
    (value: string) => {
      const newStatus = value as VerificationStatus | "";
      setVerificationStatusFilter(newStatus);
      setPage(0);
      getAll({
        query: searchQuery || undefined,
        role: roleFilter || undefined,
        verificationStatus: newStatus || undefined,
        enabled: enabledFilter === "" ? undefined : enabledFilter === "true",
        page: 0,
        size: pageSize,
      });
    },
    [searchQuery, roleFilter, enabledFilter, pageSize, getAll, setPage],
  );

  const handleEnabledFilterChange = useCallback(
    (value: string) => {
      setEnabledFilter(value);
      setPage(0);
      getAll({
        query: searchQuery || undefined,
        role: roleFilter || undefined,
        verificationStatus: verificationStatusFilter || undefined,
        enabled: value === "" ? undefined : value === "true",
        page: 0,
        size: pageSize,
      });
    },
    [
      searchQuery,
      roleFilter,
      verificationStatusFilter,
      pageSize,
      getAll,
      setPage,
    ],
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setPage(page);
      getAll({
        query: searchQuery || undefined,
        role: roleFilter || undefined,
        verificationStatus: verificationStatusFilter || undefined,
        enabled: enabledFilter === "" ? undefined : enabledFilter === "true",
        page: page,
        size: pageSize,
      });
    },
    [
      searchQuery,
      roleFilter,
      verificationStatusFilter,
      enabledFilter,
      pageSize,
      getAll,
      setPage,
    ],
  );

  if (showDelayedLoading && !users.length) {
    return (
      <div className={styles.loadingContainer}>
        <LoadingSpinner text="Loading users..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Users"
        subtitle="Manage user roles, verification status and account settings"
      />

      <div className={styles.searchSection}>
        <UserFilters
          onSearchChange={handleSearch}
          roleFilter={roleFilter}
          onRoleFilterChange={handleRoleFilterChange}
          verificationStatusFilter={verificationStatusFilter}
          onVerificationStatusChange={handleVerificationStatusChange}
          enabledFilter={enabledFilter}
          onEnabledFilterChange={handleEnabledFilterChange}
        />
      </div>

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} users
        </div>
      )}

      <div className={styles.content}>
        <UserTable users={users} onRefresh={() => loadUsers(currentPage)} />
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pageSize}
            onPageChange={handlePageChange}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}
    </div>
  );
};
