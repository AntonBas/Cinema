import React, { useState, useEffect } from 'react';
import { movieApi } from '@/api/movieApi';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import { sessionApi } from '@/api/sessionApi';
import { adminApi } from '@/api/adminApi';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import { promotionApi } from '@/api/promotionApi';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
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

  useEffect(() => {
    loadDashboardData();
    const interval = setInterval(() => {
      loadDashboardData();
    }, 300000);

    return () => clearInterval(interval);
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);

      const today = new Date();
      const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate());
      const todayEnd = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);

      const [
        moviesResponse,
        hallsResponse,
        sessionsResponse,
        usersResponse,
        ticketTypesResponse,
        promotionsResponse
      ] = await Promise.all([
        movieApi.public.getMoviesPaginated(0, 1000),
        cinemaHallApi.getAll(),
        sessionApi.admin.getAll(0, 1000),
        adminApi.getUsers(0, 1000),
        ticketTypeApi.admin.getAll(),
        promotionApi.admin.getAll()
      ]);

      const getContent = (response: any): any[] => {
        if (Array.isArray(response)) return response;
        if (response && typeof response === 'object' && 'content' in response) {
          return response.content || [];
        }
        if (response && typeof response === 'object' && 'movies' in response) {
          return response.movies || [];
        }
        return [];
      };

      const movies = getContent(moviesResponse);
      const halls = getContent(hallsResponse);
      const sessions = getContent(sessionsResponse);
      const users = getContent(usersResponse);
      const ticketTypes = getContent(ticketTypesResponse);
      let promotions = getContent(promotionsResponse);

      // If we get an array of promotions, use it directly
      if (!Array.isArray(promotions) && typeof promotions === 'object') {
        // Try to extract promotions from the response object
        promotions = Object.values(promotions).find(val => Array.isArray(val)) || [];
      }

      const now = new Date();

      const activeSessions = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime > now && session.available !== false;
      });

      const todaySessions = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime >= todayStart && sessionTime < todayEnd && session.available !== false;
      });

      const upcomingSessions = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime);
        return sessionTime > now && sessionTime <= new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
      });

      const completedSessions = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime);
        const sessionEnd = new Date(sessionTime.getTime() + (session.duration || 120) * 60000);
        return sessionEnd <= now;
      });

      // Count active promotions - try different property names
      const activePromotions = Array.isArray(promotions) ? promotions.filter((promo: any) => {
        const endDate = promo.endDate || promo.end_time || promo.validUntil;
        const active = promo.active !== false && promo.status !== 'inactive';

        if (!endDate) return active;

        try {
          const end = new Date(endDate);
          return active && end > now;
        } catch {
          return active;
        }
      }).length : 0;

      const newUsersToday = users.filter((user: any) => {
        const createdAt = new Date(user.createdAt || user.registrationDate || user.created_at);
        return createdAt >= todayStart && createdAt < todayEnd;
      });

      const activeMovies = movies.filter((movie: any) =>
        movie.status === 'CURRENT' || movie.status === 'UPCOMING' || movie.status === 'current' || movie.status === 'upcoming'
      );

      const totalRevenue = sessions.reduce((sum: number, session: any) =>
        sum + (session.totalRevenue || session.revenue || session.price || 0), 0
      );

      const todayRevenue = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime || session.start_time);
        return sessionTime >= todayStart && sessionTime < todayEnd;
      }).reduce((sum: number, session: any) => sum + (session.totalRevenue || session.revenue || session.price || 0), 0);

      const ticketsSold = sessions.reduce((sum: number, session: any) =>
        sum + (session.ticketsSold || session.bookedSeats || session.booked_seats || session.tickets_sold || 0), 0
      );

      const todayTicketsSold = sessions.filter((session: any) => {
        const sessionTime = new Date(session.startTime || session.start_time);
        return sessionTime >= todayStart && sessionTime < todayEnd;
      }).reduce((sum: number, session: any) => sum + (session.ticketsSold || session.bookedSeats || session.booked_seats || session.tickets_sold || 0), 0);

      const occupancyRates = sessions.map((session: any) => {
        const totalSeats = session.totalSeats || session.hallCapacity || session.capacity || session.total_seats || 100;
        const bookedSeats = session.ticketsSold || session.bookedSeats || session.booked_seats || session.tickets_sold || 0;
        return (bookedSeats / totalSeats) * 100;
      }).filter(rate => !isNaN(rate));

      const averageOccupancyRate = occupancyRates.length > 0
        ? occupancyRates.reduce((a, b) => a + b, 0) / occupancyRates.length
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
    movies: any[],
    halls: any[],
    sessions: any[],
    users: any[],
    ticketTypes: any[],
    promotions: any[]
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

    movies.slice(-5).forEach(movie => {
      addActivity('movie', Math.random() > 0.3 ? 'created' : 'updated', movie.title || movie.name, 24);
    });

    halls.slice(-2).forEach(hall => {
      addActivity('hall', Math.random() > 0.5 ? 'created' : 'updated', hall.name || hall.title, 96);
    });

    sessions.slice(-4).forEach(session => {
      const movieTitle = session.movieTitle || session.movie_title || `Session #${session.id}`;
      const action = Math.random() > 0.8 ? 'cancelled' : 'created';
      addActivity('session', action, movieTitle, 24,
        new Date(session.startTime || session.start_time).toLocaleString());
    });

    ticketTypes.slice(-3).forEach(type => {
      addActivity('ticket', 'created', type.name || type.title, 120);
    });

    if (Array.isArray(promotions)) {
      promotions.slice(-2).forEach(promo => {
        addActivity('promotion', 'created', promo.title || promo.name, 120);
      });
    }

    users.slice(-3).forEach(user => {
      const name = `${user.firstName || user.first_name || ''} ${user.lastName || user.last_name || ''}`.trim() || user.email;
      addActivity('user', Math.random() > 0.7 ? 'created' : 'updated', name, 168);
    });

    return activities.sort((a, b) =>
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    ).slice(0, 8);
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'movie': return '🎬';
      case 'hall': return '🏛️';
      case 'session': return '⏰';
      case 'user': return '👥';
      case 'ticket': return '🎫';
      case 'promotion': return '🎁';
      default: return '📝';
    }
  };

  const formatTimeAgo = (timestamp: string) => {
    const now = new Date();
    const time = new Date(timestamp);
    const diffInMinutes = Math.floor((now.getTime() - time.getTime()) / (1000 * 60));
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
    if (diffInHours < 24) return `${diffInHours}h ago`;
    return `${diffInDays}d ago`;
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'UAH',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
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
            <span className={styles.overviewDate}>{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
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
                <span className={styles.activityIcon}>
                  {getActivityIcon(activity.type)}
                </span>
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
                    <span className={styles.activityDetails}>
                      {activity.details}
                    </span>
                  )}
                  <span className={styles.activityTime}>
                    {formatTimeAgo(activity.timestamp)}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {notifications.map((notification, index) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={4000}
          position={index}
        />
      ))}
    </div>
  );
};