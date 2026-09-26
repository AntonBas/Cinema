import React, { useEffect, useCallback } from "react";
import { UserTable } from "./UserTable/UserTable";
import { UserFilters } from "./UserFilters/UserFilters";
import { useAdminUsers } from "@/hooks/features/user/useAdminUsers";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import {
  parseBooleanParam,
  parseOptionalEnumParam,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import {
  UserRoleDisplay,
  VerificationStatusDisplay,
  type UserRole,
  type VerificationStatus,
} from "@/types/user";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionUsers.module.css";

const USER_ROLES = Object.keys(UserRoleDisplay) as UserRole[];
const VERIFICATION_STATUSES = Object.keys(
  VerificationStatusDisplay,
) as VerificationStatus[];

export const SectionUsers: React.FC = () => {
  const {
    page,
    query,
    sort,
    getParam,
    setFilters,
    setPage,
    setSearch,
    setSort,
  } = useUrlParams();
  const role = parseOptionalEnumParam(getParam("role"), USER_ROLES);
  const verificationStatus = parseOptionalEnumParam(
    getParam("verification"),
    VERIFICATION_STATUSES,
  );
  const enabled = parseBooleanParam(getParam("enabled"));

  const { users, pagination, loading, getAll } = useAdminUsers();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadUsers = useCallback(() => {
    getAll({
      query,
      role,
      verificationStatus,
      enabled,
      page,
      size: DEFAULT_PAGE_SIZE_COMPACT,
      sort,
    });
  }, [query, role, verificationStatus, enabled, page, sort, getAll]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleRoleFilterChange = useCallback(
    (value: string) => {
      setFilters({ role: value.toLowerCase() });
    },
    [setFilters],
  );

  const handleVerificationStatusChange = useCallback(
    (value: string) => {
      setFilters({ verification: value.toLowerCase() });
    },
    [setFilters],
  );

  const handleEnabledFilterChange = useCallback(
    (value: string) => {
      setFilters({ enabled: value });
    },
    [setFilters],
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
          searchValue={query}
          onSearchChange={setSearch}
          roleFilter={role ?? ""}
          onRoleFilterChange={handleRoleFilterChange}
          verificationStatusFilter={verificationStatus ?? ""}
          onVerificationStatusChange={handleVerificationStatusChange}
          enabledFilter={enabled === undefined ? "" : String(enabled)}
          onEnabledFilterChange={handleEnabledFilterChange}
          sort={sort ?? ""}
          onSortChange={setSort}
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
        <UserTable users={users} onRefresh={loadUsers} />
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={page}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={DEFAULT_PAGE_SIZE_COMPACT}
            onPageChange={setPage}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}
    </div>
  );
};
