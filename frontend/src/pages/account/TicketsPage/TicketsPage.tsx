import React, { useState, useEffect, useMemo } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { TicketsList } from '@/components/account/TicketsList/TicketsList';
import { TicketQRModal } from '@/components/account/TicketQRModal/TicketQRModal';
import { TicketRefundModal } from '@/components/account/TicketRefundModal/TicketRefundModal';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { Button, Notification } from '@/components/ui';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { useTickets } from '@/hooks/features/tickets/useTickets';
import type { TicketStatus } from '@/types/ticket';
import { Grid, List } from 'lucide-react';
import styles from './TicketsPage.module.css';

export const TicketsPage: React.FC = () => {
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
    const [showQRModal, setShowQRModal] = useState(false);
    const [showRefundModal, setShowRefundModal] = useState(false);
    const [selectedQRCode, setSelectedQRCode] = useState<string>('');
    const [selectedTicket, setSelectedTicket] = useState<any>(null);
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const {
        data,
        loading,
        status,
        filters,
        handlePageChange,
        handleStatusChange,
        handleSearch,
        clearFilters,
        refresh
    } = useTickets();

    useEffect(() => {
    }, []);

    const hasActiveFilters = useMemo(() => {
        return (
            (filters.search && filters.search.trim() !== '') ||
            status !== undefined ||
            (filters.dateRange && filters.dateRange.from && filters.dateRange.to)
        );
    }, [filters, status]);

    const handleShowQR = (ticketCode: string) => {
        setSelectedQRCode(ticketCode);
        setShowQRModal(true);
    };

    const handleRequestRefund = (ticket: any) => {
        setSelectedTicket(ticket);
        setShowRefundModal(true);
    };

    const handleRefundSuccess = () => {
        showNotification('success', 'Refund request submitted successfully');
        refresh();
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const handleClearAllFilters = () => {
        clearFilters();
    };

    const tabs: Array<{ id: TicketStatus | 'all', label: string }> = [
        { id: 'all', label: 'All' },
        { id: 'ACTIVE', label: 'Active' },
        { id: 'USED', label: 'Used' },
        { id: 'REFUNDED', label: 'Refunded' }
    ];

    return (
        <Layout>
            <div className={styles.ticketsPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="tickets" />

                    <div className={styles.mainContent}>
                        {notification && (
                            <Notification
                                id="tickets-notification"
                                message={notification.message}
                                type={notification.type}
                                isVisible={true}
                                onClose={() => setNotification(null)}
                                duration={3000}
                                position={0}
                            />
                        )}

                        <div className={styles.pageHeader}>
                            <div>
                                <h1 className={styles.pageTitle}>My Tickets</h1>
                                <p className={styles.pageSubtitle}>Manage and view your movie tickets</p>
                            </div>
                        </div>

                        <div className={styles.controlsSection}>
                            <div className={styles.searchBox}>
                                <SearchInput
                                    onSearch={handleSearch}
                                    placeholder="Search tickets by movie, hall, or code..."
                                    delay={300}
                                    className={styles.searchInput}
                                    value={filters.search || ''}
                                />
                                {hasActiveFilters && (
                                    <Button
                                        variant="outline"
                                        onClick={handleClearAllFilters}
                                        size="small"
                                        className={styles.clearButton}
                                    >
                                        Clear Filters
                                    </Button>
                                )}
                            </div>

                            <div className={styles.viewControls}>
                                <div className={styles.viewButtons}>
                                    <Button
                                        variant={viewMode === 'grid' ? 'primary' : 'secondary'}
                                        onClick={() => setViewMode('grid')}
                                        size="small"
                                        className={styles.viewButton}
                                    >
                                        <Grid size={18} /> Grid
                                    </Button>
                                    <Button
                                        variant={viewMode === 'list' ? 'primary' : 'secondary'}
                                        onClick={() => setViewMode('list')}
                                        size="small"
                                        className={styles.viewButton}
                                    >
                                        <List size={18} /> List
                                    </Button>
                                </div>
                            </div>
                        </div>

                        <div className={styles.tabsSection}>
                            <div className={styles.mainTabs}>
                                {tabs.map(tab => (
                                    <button
                                        key={tab.id}
                                        className={`${styles.tab} ${status === (tab.id === 'all' ? undefined : tab.id) ? styles.active : ''}`}
                                        onClick={() => handleStatusChange(tab.id === 'all' ? undefined : tab.id)}
                                    >
                                        <span className={styles.tabLabel}>{tab.label}</span>
                                    </button>
                                ))}
                            </div>
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
                                        tickets={data.content || []}
                                        viewMode={viewMode}
                                        onShowQR={handleShowQR}
                                        onRequestRefund={handleRequestRefund}
                                    />
                                    {data.totalPages > 1 && (
                                        <div className={styles.paginationContainer}>
                                            <Pagination
                                                currentPage={data.number}
                                                totalPages={data.totalPages}
                                                totalElements={data.totalElements}
                                                pageSize={data.size}
                                                onPageChange={handlePageChange}
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