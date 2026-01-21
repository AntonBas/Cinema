import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { TicketsList } from '@/components/account/TicketsList/TicketsList';
import { TicketFilters } from '@/components/account/TicketFilters/TicketFilters';
import { Button, Notification } from '@/components/ui';
import { useTicketManagement } from '@/hooks/features/tickets/useTicketManagement';
import { useTicketFilters } from '@/hooks/features/tickets/useTicketFilters';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import styles from './TicketsPage.module.css';

export const TicketsPage: React.FC = () => {
    const { getUserTickets, loading, error } = useTicketManagement();
    const [tickets, setTickets] = useState<TicketResponse[]>([]);
    const [activeTab, setActiveTab] = useState<TicketStatus | 'all'>('all');
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const {
        filteredTickets,
        updateStatusFilter,
        updateSearchQuery,
        updateDateRange,
        clearFilters,
        statistics,
        hasFilters
    } = useTicketFilters(tickets);

    useEffect(() => {
        loadTickets();
    }, []);

    useEffect(() => {
        updateStatusFilter(activeTab === 'all' ? undefined : activeTab);
    }, [activeTab, updateStatusFilter]);

    const loadTickets = async () => {
        try {
            const data = await getUserTickets();
            setTickets(data);
        } catch (error) {
            console.error('Failed to load tickets:', error);
            showNotification('error', 'Failed to load tickets');
        }
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const tabs: Array<{ id: TicketStatus | 'all', label: string }> = [
        { id: 'all', label: 'All Tickets' },
        { id: 'ACTIVE', label: 'Active' },
        { id: 'USED', label: 'Used' },
        { id: 'CANCELLED', label: 'Cancelled' },
        { id: 'PENDING', label: 'Pending' },
        { id: 'REFUNDED', label: 'Refunded' },
        { id: 'EXPIRED', label: 'Expired' }
    ];

    return (
        <Layout>
            <div className={styles.ticketsPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="tickets" />

                    <div className={styles.content}>
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

                        <div className={styles.header}>
                            <h1 className={styles.title}>My Tickets</h1>
                            <div className={styles.headerActions}>
                                <Button
                                    variant="primary"
                                    onClick={loadTickets}
                                    loading={loading}
                                    disabled={loading}
                                    size="medium"
                                >
                                    Refresh
                                </Button>
                            </div>
                        </div>

                        {error && (
                            <div className={styles.error}>
                                {error}
                            </div>
                        )}

                        <div className={styles.tabs}>
                            {tabs.map(tab => (
                                <button
                                    key={tab.id}
                                    className={`${styles.tab} ${activeTab === tab.id ? styles.active : ''}`}
                                    onClick={() => setActiveTab(tab.id)}
                                >
                                    {tab.label} {tab.id !== 'all' && `(${statistics.byStatus?.[tab.id as TicketStatus] || 0})`}
                                </button>
                            ))}
                        </div>

                        <div className={styles.statistics}>
                            <div className={styles.statItem}>
                                <div className={styles.statLabel}>Total Tickets</div>
                                <div className={styles.statValue}>{statistics.total}</div>
                            </div>
                            <div className={styles.statItem}>
                                <div className={styles.statLabel}>Active</div>
                                <div className={styles.statValue}>{statistics.activeCount}</div>
                            </div>
                            <div className={styles.statItem}>
                                <div className={styles.statLabel}>Used</div>
                                <div className={styles.statValue}>{statistics.usedCount}</div>
                            </div>
                            <div className={styles.statItem}>
                                <div className={styles.statLabel}>Total Revenue</div>
                                <div className={styles.statValue}>{statistics.totalRevenue.toFixed(2)} UAH</div>
                            </div>
                        </div>

                        <div className={styles.filtersSection}>
                            <TicketFilters
                                onSearch={updateSearchQuery}
                                onDateRangeChange={updateDateRange}
                                onClearFilters={clearFilters}
                                hasFilters={hasFilters}
                            />
                        </div>

                        <div className={styles.ticketsContent}>
                            {loading ? (
                                <div className={styles.loading}>
                                    Loading tickets...
                                </div>
                            ) : filteredTickets.length === 0 ? (
                                <div className={styles.noTickets}>
                                    <div className={styles.noTicketsContent}>
                                        <div className={styles.noTicketsIcon}>🎫</div>
                                        <h3 className={styles.noTicketsTitle}>
                                            {tickets.length === 0
                                                ? 'No tickets yet'
                                                : hasFilters
                                                    ? 'No tickets match your filters'
                                                    : 'No tickets found'}
                                        </h3>
                                        <p className={styles.noTicketsDescription}>
                                            {tickets.length === 0
                                                ? 'Purchase your first ticket to see it here'
                                                : 'Try adjusting your search filters'}
                                        </p>
                                        {tickets.length === 0 && (
                                            <Button variant="primary" size="medium">
                                                Browse Movies
                                            </Button>
                                        )}
                                        {hasFilters && tickets.length > 0 && (
                                            <Button variant="secondary" size="medium" onClick={clearFilters}>
                                                Clear Filters
                                            </Button>
                                        )}
                                    </div>
                                </div>
                            ) : (
                                <TicketsList tickets={filteredTickets} />
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};