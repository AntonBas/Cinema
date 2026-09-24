import React, { useEffect, useMemo, useState } from "react";
import { Modal } from "@/components/ui/Modal/Modal";
import { Tabs } from "@/components/ui/Tabs/Tabs";
import type { TabItem } from "@/components/ui/Tabs/Tabs";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { BookingTable } from "@/components/admin/SectionBookings/BookingTable/BookingTable";
import { BookingDetailsModal } from "@/components/admin/SectionBookings/BookingDetailsModal/BookingDetailsModal";
import { RefundTable } from "@/components/admin/SectionRefunds/RefundTable/RefundTable";
import { EntityHistoryModal } from "@/components/admin/SectionAuditLogs/EntityHistoryModal/EntityHistoryModal";
import { BonusBalanceCard } from "@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard";
import { BonusTransactions } from "@/components/account/BonusSection/BonusTransactions/BonusTransactions";
import { useAdminBookings } from "@/hooks/features/booking/useAdminBookings";
import { useAdminRefunds } from "@/hooks/features/refund/useAdminRefunds";
import { useAdminUserBonus } from "@/hooks/features/bonus/useAdminUserBonus";
import type { AdminUserListResponse } from "@/types/user";
import styles from "./UserDetailsModal.module.css";

type UserTab = "bookings" | "refunds" | "bonus";

const TABS: ReadonlyArray<TabItem<UserTab>> = [
  { id: "bookings", label: "Bookings" },
  { id: "refunds", label: "Refunds" },
  { id: "bonus", label: "Bonus" },
];

interface UserDetailsModalProps {
  user: AdminUserListResponse;
  onClose: () => void;
}

interface TabProps {
  userId: number;
  onOpenBooking: (bookingId: number) => void;
}

const UserBookingsTab: React.FC<TabProps> = ({ userId, onOpenBooking }) => {
  const [page, setPage] = useState(0);
  const filters = useMemo(() => ({ userId }), [userId]);
  const { bookings, pagination, loading, refresh } = useAdminBookings({
    filters,
    page,
  });

  useEffect(() => {
    refresh();
  }, [refresh]);

  if (loading && !bookings.length) {
    return <LoadingSpinner text="Loading bookings..." />;
  }

  return (
    <>
      <BookingTable bookings={bookings} onSelect={onOpenBooking} />
      {pagination && pagination.totalPages > 1 && (
        <div className={styles.pagination}>
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
  );
};

const UserRefundsTab: React.FC<
  TabProps & { onOpenHistory: (refundId: number) => void }
> = ({ userId, onOpenBooking, onOpenHistory }) => {
  const [page, setPage] = useState(0);
  const filters = useMemo(() => ({ userId }), [userId]);
  const { refunds, pagination, loading, refresh } = useAdminRefunds({
    filters,
    page,
  });

  useEffect(() => {
    refresh();
  }, [refresh]);

  if (loading && !refunds.length) {
    return <LoadingSpinner text="Loading refunds..." />;
  }

  return (
    <>
      <RefundTable
        refunds={refunds}
        onOpenBooking={onOpenBooking}
        onOpenHistory={onOpenHistory}
      />
      {pagination && pagination.totalPages > 1 && (
        <div className={styles.pagination}>
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
  );
};

const UserBonusTab: React.FC<{ userId: number }> = ({ userId }) => {
  const {
    balance,
    balanceLoading,
    transactions,
    transactionsPage,
    transactionsLoading,
    getBalance,
    getTransactions,
  } = useAdminUserBonus(userId);

  useEffect(() => {
    getBalance();
    getTransactions(0);
  }, [getBalance, getTransactions]);

  return (
    <div className={styles.bonus}>
      <BonusBalanceCard balance={balance} loading={balanceLoading} />
      <BonusTransactions
        transactions={transactions}
        loading={transactionsLoading}
        onPageChange={getTransactions}
        currentPage={transactionsPage?.number ?? 0}
        totalPages={transactionsPage?.totalPages ?? 1}
        totalElements={transactionsPage?.totalElements ?? 0}
      />
    </div>
  );
};

export const UserDetailsModal: React.FC<UserDetailsModalProps> = ({
  user,
  onClose,
}) => {
  const [activeTab, setActiveTab] = useState<UserTab>("bookings");
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [historyRefundId, setHistoryRefundId] = useState<number | null>(null);

  return (
    <>
      <Modal
        isOpen={true}
        onClose={onClose}
        title={`${user.firstName} ${user.lastName} · ${user.email}`}
        size="fullscreen"
      >
        <div className={styles.body}>
          <Tabs
            items={TABS}
            activeId={activeTab}
            onChange={setActiveTab}
            ariaLabel="User activity"
          />
          {activeTab === "bookings" && (
            <UserBookingsTab userId={user.id} onOpenBooking={setBookingId} />
          )}
          {activeTab === "refunds" && (
            <UserRefundsTab
              userId={user.id}
              onOpenBooking={setBookingId}
              onOpenHistory={setHistoryRefundId}
            />
          )}
          {activeTab === "bonus" && <UserBonusTab userId={user.id} />}
        </div>
      </Modal>

      {bookingId !== null && (
        <BookingDetailsModal
          bookingId={bookingId}
          onClose={() => setBookingId(null)}
        />
      )}

      {historyRefundId !== null && (
        <EntityHistoryModal
          entityType="Refund"
          entityId={historyRefundId}
          onClose={() => setHistoryRefundId(null)}
        />
      )}
    </>
  );
};
