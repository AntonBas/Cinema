import React, { useState, useEffect, useCallback } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { TicketsList } from "@/components/account/TicketSection/TicketsList/TicketsList";
import { TicketQRModal } from "@/components/account/TicketSection/TicketQRModal/TicketQRModal";
import { TicketRefundModal } from "@/components/account/TicketSection/TicketRefundModal/TicketRefundModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { useTicket } from "@/hooks/features/ticket/useTicket";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import type { TicketResponse, TicketStatus } from "@/types/ticket";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./TicketsPage.module.css";

const TABS: ReadonlyArray<TabItem<TicketStatus | "all">> = [
  { id: "all", label: "All" },
  { id: "ACTIVE", label: "Active" },
  { id: "USED", label: "Used" },
  { id: "REFUNDED", label: "Refunded" },
  { id: "EXPIRED", label: "Expired" },
];

const TAB_IDS = TABS.map((tab) => tab.id);

export const TicketsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [showQRModal, setShowQRModal] = useState(false);
  const [showRefundModal, setShowRefundModal] = useState(false);
  const [selectedQRCode, setSelectedQRCode] = useState<string>("");
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(
    null,
  );
  const { page, query, getParam, setFilters, clearParams, setPage, setSearch } =
    useUrlParams();
  const activeTab = parseEnumParam(getParam("status"), TAB_IDS, "all");
  const statusFilter = activeTab === "all" ? undefined : activeTab;

  const { tickets, pagination, loading, getMine } = useTicket();

  const loadTickets = useCallback(() => {
    getMine({
      page,
      size: DEFAULT_PAGE_SIZE_COMPACT,
      status: statusFilter,
      movieTitle: query,
    });
  }, [page, statusFilter, query, getMine]);

  useEffect(() => {
    loadTickets();
  }, [loadTickets]);

  const hasActiveFilters = query !== undefined || statusFilter !== undefined;

  const handleStatusChange = (tab: TicketStatus | "all") => {
    setFilters({ status: toUrlEnumValue(tab, "all") });
  };

  return (
    <AccountPageLayout
      title="My Tickets"
      subtitle="Manage and view your movie tickets"
    >
      <div className={styles.controlsSection}>
        <div className={styles.searchBox}>
          <SearchInput
            value={query}
            onSearch={setSearch}
            placeholder="Search tickets by movie title..."
            delay={300}
          />
          {hasActiveFilters && (
            <Button variant="secondary" onClick={clearParams} size="small">
              Clear Filters
            </Button>
          )}
        </div>

        <div className={styles.viewControls}>
          <Button
            variant={viewMode === "grid" ? "primary" : "secondary"}
            onClick={() => setViewMode("grid")}
            size="small"
          >
            Grid
          </Button>
          <Button
            variant={viewMode === "list" ? "primary" : "secondary"}
            onClick={() => setViewMode("list")}
            size="small"
          >
            List
          </Button>
        </div>
      </div>

      <Tabs
        items={TABS}
        activeId={activeTab}
        onChange={handleStatusChange}
        ariaLabel="Ticket status"
      />

      <div className={styles.ticketsSection}>
        {loading ? (
          <LoadingSpinner text="Loading tickets..." />
        ) : (
          <>
            <TicketsList
              tickets={tickets}
              viewMode={viewMode}
              onShowQR={(code) => {
                setSelectedQRCode(code);
                setShowQRModal(true);
              }}
              onRequestRefund={(ticket) => {
                setSelectedTicket(ticket);
                setShowRefundModal(true);
              }}
            />
            {pagination && pagination.totalPages > 1 && (
              <div className={styles.paginationContainer}>
                <Pagination
                  currentPage={pagination.number}
                  totalPages={pagination.totalPages}
                  totalElements={pagination.totalElements}
                  pageSize={pagination.size}
                  onPageChange={setPage}
                  variant="pages"
                  showInfo={true}
                />
              </div>
            )}
          </>
        )}
      </div>

      {showQRModal && (
        <TicketQRModal
          ticketCode={selectedQRCode}
          onClose={() => setShowQRModal(false)}
        />
      )}

      {showRefundModal && (
        <TicketRefundModal
          ticket={selectedTicket}
          onClose={() => {
            setShowRefundModal(false);
            setSelectedTicket(null);
          }}
          onRefundSuccess={loadTickets}
        />
      )}
    </AccountPageLayout>
  );
};
