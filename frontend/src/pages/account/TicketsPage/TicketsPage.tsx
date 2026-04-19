import React, { useState, useEffect } from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { AccountSidebar } from "@/components/account/AccountSidebar/AccountSidebar";
import { TicketsList } from "@/components/account/TicketSection/TicketsList/TicketsList";
import { TicketQRModal } from "@/components/account/TicketSection/TicketQRModal/TicketQRModal";
import { TicketRefundModal } from "@/components/account/TicketSection/TicketRefundModal/TicketRefundModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { useTickets } from "@/hooks/features/tickets/useTickets";
import type { TicketResponse, TicketStatus } from "@/types/ticket";
import styles from "./TicketsPage.module.css";

const TABS: Array<{ id: TicketStatus | "all"; label: string }> = [
  { id: "all", label: "All" },
  { id: "ACTIVE", label: "Active" },
  { id: "USED", label: "Used" },
  { id: "REFUNDED", label: "Refunded" },
];

export const TicketsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [showQRModal, setShowQRModal] = useState(false);
  const [showRefundModal, setShowRefundModal] = useState(false);
  const [selectedQRCode, setSelectedQRCode] = useState<string>("");
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(
    null,
  );
  const [statusFilter, setStatusFilter] = useState<TicketStatus | undefined>(
    undefined,
  );
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0);

  const { tickets, pagination, loading, getUserTickets } = useTickets();

  useEffect(() => {
    getUserTickets({
      page: currentPage,
      size: 10,
      status: statusFilter,
      movieTitle: searchQuery || undefined,
    });
  }, [currentPage, statusFilter, searchQuery]);

  const hasActiveFilters =
    searchQuery.trim() !== "" || statusFilter !== undefined;

  const handleStatusChange = (status: TicketStatus | undefined) => {
    setStatusFilter(status);
    setCurrentPage(0);
  };

  const handleClearFilters = () => {
    setStatusFilter(undefined);
    setSearchQuery("");
    setCurrentPage(0);
  };

  const handleRefundSuccess = () => {
    getUserTickets({
      page: currentPage,
      size: 10,
      status: statusFilter,
      movieTitle: searchQuery || undefined,
    });
  };

  return (
    <Layout>
      <div className={styles.ticketsPage}>
        <div className={styles.container}>
          <AccountSidebar />

          <div className={styles.mainContent}>
            <div className={styles.pageHeader}>
              <h1 className={styles.pageTitle}>My Tickets</h1>
              <p className={styles.pageSubtitle}>
                Manage and view your movie tickets
              </p>
            </div>

            <div className={styles.controlsSection}>
              <div className={styles.searchBox}>
                <SearchInput
                  onSearch={setSearchQuery}
                  placeholder="Search tickets by movie title..."
                  delay={300}
                />
                {hasActiveFilters && (
                  <Button
                    variant="secondary"
                    onClick={handleClearFilters}
                    size="small"
                  >
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

            <div className={styles.tabsSection}>
              {TABS.map((tab) => (
                <button
                  key={tab.id}
                  className={`${styles.filterTab} ${statusFilter === (tab.id === "all" ? undefined : tab.id) ? styles.active : ""}`}
                  onClick={() =>
                    handleStatusChange(tab.id === "all" ? undefined : tab.id)
                  }
                >
                  {tab.label}
                </button>
              ))}
            </div>

            <div className={styles.ticketsSection}>
              {loading ? (
                <div className={styles.loadingState}>
                  <div className={styles.spinner}></div>
                  <p>Loading tickets...</p>
                </div>
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
                        onPageChange={setCurrentPage}
                        variant="pages"
                        showInfo={true}
                      />
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
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
            onRefundSuccess={handleRefundSuccess}
          />
        )}
      </div>
    </Layout>
  );
};
