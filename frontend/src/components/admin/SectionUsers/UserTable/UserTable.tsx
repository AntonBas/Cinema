import React from "react";
import { UserTableRow } from "../UserTableRow/UserTableRow";
import type { AdminUserListResponse } from "@/types/user";
import styles from "./UserTable.module.css";

interface UserTableProps {
  users: AdminUserListResponse[];
  onRefresh: () => void;
}

const TABLE_HEADERS = [
  "User",
  "Role",
  "Verification",
  "Status",
  "Tickets",
  "Last Activity",
  "Actions",
] as const;

export const UserTable: React.FC<UserTableProps> = ({ users, onRefresh }) => {
  const validUsers = users.filter((user) => user && user.id != null);

  return (
    <div className={styles.tableWrapper}>
      <div className={styles.tableContainer}>
        <table className={styles.table}>
          <thead className={styles.tableHead}>
            <tr>
              {TABLE_HEADERS.map((header) => (
                <th key={header}>{header}</th>
              ))}
            </tr>
          </thead>
          <tbody className={styles.tableBody}>
            {validUsers.map((user) => (
              <UserTableRow key={user.id} user={user} onUpdate={onRefresh} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
