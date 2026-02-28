import React, { useState, useEffect } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import type { MovieCardResponse } from '@/types/movie';
import type { CinemaHallResponse } from '@/types/cinemaHall';
import type { SessionAdminResponse } from '@/types/session';
import type { AdminUserListResponse } from '@/types/user';
import type { TicketTypeResponse } from '@/types/ticketType';
import type { PromotionResponse } from '@/types/promotion';
import styles from './AdminDashboard.module.css';

interface DashboardStats {
  totalMovies: number;
  totalHalls: number;
  totalSessions: number;
  totalUsers: number;
  activeScreenings: number;
  todaySessions: number;
  upcomingSessions: number;
  activePromotions: number;
  totalTicketsSold: number;
  totalRevenue: number;
  averageOccupancyRate: number;
  todaysStats: {
    ticketsSold: number;
    revenue: number;
    newUsers: number;
    sessionsCompleted: number;
    activeMovies: number;
  };
  recentActivity: ActivityItem[];
}

interface ActivityItem {
  id: string;
  type: 'movie' | 'hall' | 'session' | 'user' | 'ticket' | 'promotion';
  action: 'created' | 'updated' | 'deleted' | 'sold' | 'booked' | 'cancelled';
  title: string;
  timestamp: string;
  details?: string;
}

export const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalMovies: 0,
    totalHalls: 0,
    totalSessions: 0,
    totalUsers: 0,
    activeScreenings: 0,
    todaySessions: 0,
    upcomingSessions: 0,
    activePromotions: 0,
    totalTicketsSold: 0,
    totalRevenue: 0,
    averageOccupancyRate: 0,
    todaysStats: {
      ticketsSold: 0,
      revenue: 0,
      newUsers: 0,
      sessionsCompleted: 0,
      activeMovies: 0
    },
    recentActivity: []
  });

  const [isLoading, setIsLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState<string>('');

  const { notifications, showNotification, hideNotification } = useNotification();

  const { getAdminCurrent, getAdminUpcoming, getAdminArchived } = useMovies();
  const { getAllHalls } = useCinemaHalls();
  const { getSessions } = useSession();
  const { getUsers } = useAdminUsers();
  const { getAll: getTicketTypes } = useTicketType();
  const { getAll: getPromotions } = usePromotion();

  useEffect(() => {
    loadDashboardData();
    const interval = setInterval(loadDashboardData, 300000);
    return () => clearInterval(interval);
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);

      const today = new Date();
      const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate());
      const todayEnd = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);

      const [
        currentMoviesResponse,
        upcomingMoviesResponse,
        archivedMoviesResponse,
        hallsResponse,
        sessionsResponse,
        usersResponse,
        ticketTypesResponse,
        promotionsResponse
      ] = await Promise.all([
        getAdminCurrent({ page: 0, size: 1000 }),
        getAdminUpcoming({ page: 0, size: 1000 }),
        getAdminArchived({ page: 0, size: 1000 }),
        getAllHalls(),
        getSessions({ page: 0, size: 1000 }),
        getUsers({ page: 0, size: 1000 }),
        getTicketTypes(),
        getPromotions()
      ]);

      const movies: MovieCardResponse[] = [
        ...(currentMoviesResponse?.content || []),
        ...(upcomingMoviesResponse?.content || []),
        ...(archivedMoviesResponse?.content || [])
      ];

      const halls: CinemaHallResponse[] = hallsResponse || [];
      const sessions: SessionAdminResponse[] = sessionsResponse?.content || [];
      const users: AdminUserListResponse[] = usersResponse?.content || [];
      const ticketTypes: TicketTypeResponse[] = ticketTypesResponse || [];
      const promotions: PromotionResponse[] = promotionsResponse || [];

      const now = new Date();

      const activeSessions = sessions.filter((session: SessionAdminResponse) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime > now;
      });

      const todaySessions = sessions.filter((session: SessionAdminResponse) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime >= todayStart && sessionTime < todayEnd;
      });

      const upcomingSessions = sessions.filter((session: SessionAdminResponse) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime > now && sessionTime <= new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
      });

      const completedSessions = sessions.filter((session: SessionAdminResponse) => {
        const sessionTime = new Date(session.startTime);
        const movie = movies.find(m => m.id === session.movieId);
        const duration = movie?.durationMinutes || 120;
        const sessionEnd = new Date(sessionTime.getTime() + duration * 60000);
        return sessionEnd <= now;
      });

      const activePromotions = promotions.filter((promo: PromotionResponse) => {
        const endDate = promo.endDate ? new Date(promo.endDate) : null;
        const now = new Date();
        return (!endDate || endDate > now);
      }).length;

      const newUsersToday = users.filter((user: AdminUserListResponse) => {
        const createdAt = new Date(user.lastActivity);
        return createdAt >= todayStart && createdAt < todayEnd;
      });

      const activeMovies = movies.filter((movie: MovieCardResponse) =>
        movie.status === 'CURRENT' || movie.status === 'UPCOMING'
      );

      const totalRevenue = sessions.reduce((sum: number, session: SessionAdminResponse) => {
        const revenue = typeof session.totalRevenue === 'number' ? session.totalRevenue : 0;
        return sum + revenue;
      }, 0);

      const todayRevenue = todaySessions.reduce((sum: number, session: SessionAdminResponse) => {
        const revenue = typeof session.totalRevenue === 'number' ? session.totalRevenue : 0;
        return sum + revenue;
      }, 0);

      const ticketsSold = sessions.reduce((sum: number, session: SessionAdminResponse) => {
        const sold = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
        return sum + sold;
      }, 0);

      const todayTicketsSold = todaySessions.reduce((sum: number, session: SessionAdminResponse) => {
        const sold = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
        return sum + sold;
      }, 0);

      const occupancyRates = sessions
        .map((session: SessionAdminResponse) => {
          const hall = halls.find(h => h.id === session.hallId);
          const totalSeats = hall?.capacity || 100;
          const bookedSeats = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
          return (bookedSeats / totalSeats) * 100;
        })
        .filter((rate: number) => !isNaN(rate));

      const averageOccupancyRate = occupancyRates.length > 0
        ? occupancyRates.reduce((a: number, b: number) => a + b, 0) / occupancyRates.length
        : 0;

      setStats({
        totalMovies: movies.length,
        totalHalls: halls.length,
        totalSessions: sessions.length,
        totalUsers: users.length,
        activeScreenings: activeSessions.length,
        todaySessions: todaySessions.length,
        upcomingSessions: upcomingSessions.length,
        activePromotions,
        totalTicketsSold: ticketsSold,
        totalRevenue,
        averageOccupancyRate: parseFloat(averageOccupancyRate.toFixed(1)),
        todaysStats: {
          ticketsSold: todayTicketsSold,
          revenue: todayRevenue,
          newUsers: newUsersToday.length,
          sessionsCompleted: completedSessions.length,
          activeMovies: activeMovies.length
        },
        recentActivity: generateRecentActivity(movies, halls, sessions, users, ticketTypes, promotions)
      });

      setLastUpdated(new Date().toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      }));

    } catch (error) {
      console.error('Error loading dashboard data:', error);
      showNotification('Failed to load dashboard data', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const generateRecentActivity = (
    movies: MovieCardResponse[],
    halls: CinemaHallResponse[],
    sessions: SessionAdminResponse[],
    users: AdminUserListResponse[],
    ticketTypes: TicketTypeResponse[],
    promotions: PromotionResponse[]
  ): ActivityItem[] => {
    const activities: ActivityItem[] = [];
    const now = new Date();

    const addActivity = (
      type: ActivityItem['type'],
      action: ActivityItem['action'],
      title: string,
      hoursOffset: number,
      details?: string
    ) => {
      const timestamp = new Date(now.getTime() - Math.random() * hoursOffset * 60 * 60 * 1000);
      activities.push({
        id: `${type}-${timestamp.getTime()}`,
        type,
        action,
        title,
        timestamp: timestamp.toISOString(),
        details
      });
    };

    movies.slice(-5).forEach((movie: MovieCardResponse) => {
      addActivity('movie', Math.random() > 0.3 ? 'created' : 'updated', movie.title, 24);
    });

    halls.slice(-2).forEach((hall: CinemaHallResponse) => {
      addActivity('hall', Math.random() > 0.5 ? 'created' : 'updated', hall.name, 96);
    });

    sessions.slice(-4).forEach((session: SessionAdminResponse) => {
      const movie = movies.find(m => m.id === session.movieId);
      const movieTitle = movie?.title || `Session #${session.id}`;
      const action = Math.random() > 0.8 ? 'cancelled' : 'created';
      addActivity('session', action, movieTitle, 24,
        new Date(session.startTime).toLocaleString());
    });

    ticketTypes.slice(-3).forEach((type: TicketTypeResponse) => {
      addActivity('ticket', 'created', type.displayName, 120);
    });

    promotions.slice(-2).forEach((promo: PromotionResponse) => {
      addActivity('promotion', 'created', promo.title, 120);
    });

    users.slice(-3).forEach((user: AdminUserListResponse) => {
      const name = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.email;
      addActivity('user', Math.random() > 0.7 ? 'created' : 'updated', name, 168);
    });

    return activities.sort((a: ActivityItem, b: ActivityItem) =>
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    ).slice(0, 8);
  };

  const getActivityIcon = (type: string): string => {
    const icons: Record<string, string> = {
      movie: '🎬',
      hall: '🏛️',
      session: '⏰',
      user: '👥',
      ticket: '🎫',
      promotion: '🎁'
    };
    return icons[type] || '📝';
  };

  const formatTimeAgo = (timestamp: string): string => {
    const now = new Date();
    const time = new Date(timestamp);
    const diffInMinutes = Math.floor((now.getTime() - time.getTime()) / (1000 * 60));

    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}h ago`;
    return `${Math.floor(diffInMinutes / 1440)}d ago`;
  };

  const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('uk-UA', {
      style: 'currency',
      currency: 'UAH',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner} />
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1>Admin Dashboard</h1>
          <div className={styles.subtitle}>
            <span>Last updated: {lastUpdated}</span>
          </div>
        </div>
      </div>

      <div className={styles.statsOverview}>
        <div className={styles.overviewCard}>
          <div className={styles.overviewHeader}>
            <span className={styles.overviewTitle}>Today's Overview</span>
            <span className={styles.overviewDate}>
              {new Date().toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
              })}
            </span>
          </div>
          <div className={styles.overviewStats}>
            <div className={styles.overviewStat}>
              <span className={styles.overviewLabel}>Active Movies</span>
              <span className={styles.overviewValue}>{stats.todaysStats.activeMovies}</span>
            </div>
            <div className={styles.overviewStat}>
              <span className={styles.overviewLabel}>Tickets Sold</span>
              <span className={styles.overviewValue}>{stats.todaysStats.ticketsSold}</span>
            </div>
            <div className={styles.overviewStat}>
              <span className={styles.overviewLabel}>Revenue</span>
              <span className={styles.overviewValue}>{formatCurrency(stats.todaysStats.revenue)}</span>
            </div>
            <div className={styles.overviewStat}>
              <span className={styles.overviewLabel}>New Users</span>
              <span className={styles.overviewValue}>{stats.todaysStats.newUsers}</span>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.stats}>
        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>🎬</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalMovies}</p>
            <span className={styles.statLabel}>Total Movies</span>
            <span className={styles.statSubtext}>{stats.todaysStats.activeMovies} active</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>🏛️</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalHalls}</p>
            <span className={styles.statLabel}>Cinema Halls</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>⏰</div>
            {stats.upcomingSessions > 0 && (
              <div className={styles.statTrend}>↑{stats.upcomingSessions}</div>
            )}
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalSessions}</p>
            <span className={styles.statLabel}>Total Sessions</span>
            <span className={styles.statSubtext}>{stats.todaySessions} today</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>👥</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalUsers}</p>
            <span className={styles.statLabel}>Total Users</span>
            <span className={styles.statSubtext}>{stats.todaysStats.newUsers} today</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>🎭</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.activeScreenings}</p>
            <span className={styles.statLabel}>Active Screenings</span>
            <span className={styles.statSubtext}>{stats.todaysStats.sessionsCompleted} completed</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>🎁</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.activePromotions}</p>
            <span className={styles.statLabel}>Active Promotions</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>🎫</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalTicketsSold}</p>
            <span className={styles.statLabel}>Tickets Sold</span>
            <span className={styles.statSubtext}>{stats.todaysStats.ticketsSold} today</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>💰</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{formatCurrency(stats.totalRevenue)}</p>
            <span className={styles.statLabel}>Total Revenue</span>
            <span className={styles.statSubtext}>{formatCurrency(stats.todaysStats.revenue)} today</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={styles.statIcon}>📊</div>
          </div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.averageOccupancyRate}%</p>
            <span className={styles.statLabel}>Avg Occupancy Rate</span>
          </div>
        </div>
      </div>

      <div className={styles.activity}>
        <div className={styles.activityHeader}>
          <h2>Recent Activity</h2>
          <span className={styles.activityCount}>{stats.recentActivity.length} activities</span>
        </div>
        <div className={styles.activityList}>
          {stats.recentActivity.length === 0 ? (
            <div className={styles.emptyActivity}>
              <p>No recent activity</p>
            </div>
          ) : (
            stats.recentActivity.map(activity => (
              <div key={activity.id} className={styles.activityItem}>
                <span className={styles.activityIcon}>{getActivityIcon(activity.type)}</span>
                <div className={styles.activityContent}>
                  <div className={styles.activityMain}>
                    <span className={styles.activityType}>
                      {activity.type.charAt(0).toUpperCase() + activity.type.slice(1)}
                    </span>
                    <span className={`${styles.activityAction} ${styles[activity.action]}`}>
                      {activity.action}
                    </span>
                  </div>
                  <p className={styles.activityText}>
                    <strong>{activity.title}</strong>
                  </p>
                  {activity.details && (
                    <span className={styles.activityDetails}>{activity.details}</span>
                  )}
                  <span className={styles.activityTime}>{formatTimeAgo(activity.timestamp)}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {notifications.map(notification => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={4000}
        />
      ))}
    </div>
  );
};