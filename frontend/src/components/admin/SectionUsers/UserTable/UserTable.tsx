import React from "react";
import { UserTableRow } from "../UserTableRow/UserTableRow";
import type { AdminUserListResponse } from "@/types/user";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";

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

const COLUMN_WIDTHS = ["19%", "14%", "17%", "10%", "8%", "14%", "18%"];

export const UserTable: React.FC<UserTableProps> = ({ users, onRefresh }) => {
  const validUsers = users.filter((user) => user && user.id != null);

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            {COLUMN_WIDTHS.map((width, index) => (
              <col key={TABLE_HEADERS[index]} style={{ width }} />
            ))}
          </colgroup>
          <thead>
            <tr>
              {TABLE_HEADERS.map((header) => (
                <th
                  key={header}
                  className={
                    header === "Actions" ? tableStyles.actionsCol : undefined
                  }
                >
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {validUsers.map((user) => (
              <UserTableRow key={user.id} user={user} onUpdate={onRefresh} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
