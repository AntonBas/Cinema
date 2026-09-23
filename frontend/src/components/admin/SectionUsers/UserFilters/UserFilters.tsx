import React, { useMemo } from "react";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Select } from "@/components/ui/Select/Select";
import { UserRoleDisplay, VerificationStatusDisplay } from "@/types/user";
import styles from "./UserFilters.module.css";

interface UserFiltersProps {
  onSearchChange: (value: string) => void;
  roleFilter: string;
  onRoleFilterChange: (value: string) => void;
  verificationStatusFilter: string;
  onVerificationStatusChange: (value: string) => void;
  enabledFilter: string;
  onEnabledFilterChange: (value: string) => void;
  sort: string;
  onSortChange: (value: string) => void;
}

const SORT_OPTIONS = [
  { value: "", label: "Newest first" },
  { value: "createdDate,asc", label: "Oldest first" },
  { value: "email,asc", label: "Email: A–Z" },
  { value: "lastName,asc", label: "Last name: A–Z" },
  { value: "lastActivity,desc", label: "Recently active" },
  { value: "ticketsCount,desc", label: "Most tickets" },
];

const ENABLED_OPTIONS = [
  { value: "", label: "All Accounts" },
  { value: "true", label: "Enabled" },
  { value: "false", label: "Disabled" },
];

export const UserFilters: React.FC<UserFiltersProps> = ({
  onSearchChange,
  roleFilter,
  onRoleFilterChange,
  verificationStatusFilter,
  onVerificationStatusChange,
  enabledFilter,
  onEnabledFilterChange,
  sort,
  onSortChange,
}) => {
  const roleOptions = useMemo(
    () => [
      { value: "", label: "All Roles" },
      ...Object.entries(UserRoleDisplay).map(([value, label]) => ({
        value,
        label,
      })),
    ],
    [],
  );

  const verificationStatusOptions = useMemo(
    () => [
      { value: "", label: "All Verification" },
      ...Object.entries(VerificationStatusDisplay).map(([value, label]) => ({
        value,
        label,
      })),
    ],
    [],
  );

  return (
    <div className={styles.filters}>
      <div className={styles.search}>
        <SearchInput
          onSearch={onSearchChange}
          placeholder="Search by email or name..."
          delay={500}
        />
      </div>

      <div className={styles.selectors}>
        <Select
          value={roleFilter}
          onChange={(value) => onRoleFilterChange(String(value))}
          options={roleOptions}
          placeholder="All Roles"
          className={styles.select}
        />

        <Select
          value={verificationStatusFilter}
          onChange={(value) => onVerificationStatusChange(String(value))}
          options={verificationStatusOptions}
          placeholder="Verification Status"
          className={styles.select}
        />

        <Select
          value={enabledFilter}
          onChange={(value) => onEnabledFilterChange(String(value))}
          options={ENABLED_OPTIONS}
          placeholder="Account Status"
          className={styles.select}
        />

        <Select
          value={sort}
          onChange={(value) => onSortChange(String(value))}
          options={SORT_OPTIONS}
          placeholder="Sort by"
          className={styles.select}
        />
      </div>
    </div>
  );
};
