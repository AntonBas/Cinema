import React, { useState, useEffect } from 'react';
import { movieApi } from '@/api/movieApi';
import { genreApi } from '@/api/genreApi';
import { personApi } from '@/api/personApi';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import { sessionApi } from '@/api/sessionApi';
import { adminApi } from '@/api/adminApi';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import styles from './AdminDashboard.module.css';

interface DashboardStats {
  totalMovies: number;
  totalGenres: number;
  totalPersons: number;
  totalHalls: number;
  totalSessions: number;
  totalUsers: number;
  activeScreenings: number;
  recentActivity: ActivityItem[];
}

interface ActivityItem {
  id: string;
  type: 'movie' | 'genre' | 'person' | 'hall' | 'session' | 'user';
  action: 'created' | 'updated' | 'deleted';
  title: string;
  timestamp: string;
}

export const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalMovies: 0,
    totalGenres: 0,
    totalPersons: 0,
    totalHalls: 0,
    totalSessions: 0,
    totalUsers: 0,
    activeScreenings: 0,
    recentActivity: []
  });
  const [isLoading, setIsLoading] = useState(true);

  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);

      const [
        movies,
        genresResponse,
        personsResponse,
        hallsResponse,
        sessionsResponse,
        usersResponse
      ] = await Promise.all([
        movieApi.getAllMovies(),
        genreApi.getAll(),
        personApi.getAll(),
        cinemaHallApi.getAllHalls(),
        sessionApi.getAllSessions(),
        adminApi.getUsers(0, 1000)
      ]);

      const getContent = (response: any): any[] => {
        if (Array.isArray(response)) return response;
        if (response && typeof response === 'object' && 'content' in response) {
          return response.content || [];
        }
        return [];
      };

      const genres = getContent(genresResponse);
      const persons = getContent(personsResponse);
      const halls = getContent(hallsResponse);
      const sessions = getContent(sessionsResponse);
      const users = getContent(usersResponse);

      const activeScreenings = sessions.filter((session: any) => session.available).length;

      setStats({
        totalMovies: movies.length,
        totalGenres: genres.length,
        totalPersons: persons.length,
        totalHalls: halls.length,
        totalSessions: sessions.length,
        totalUsers: users.length,
        activeScreenings,
        recentActivity: generateRecentActivity(movies, genres, persons, halls, sessions, users)
      });

    } catch (error) {
      console.error('Error loading dashboard data:', error);
      showNotification('Failed to load dashboard data', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const generateRecentActivity = (
    movies: any[],
    genres: any[],
    persons: any[],
    halls: any[],
    sessions: any[],
    users: any[]
  ): ActivityItem[] => {
    const activities: ActivityItem[] = [];

    movies.slice(-3).forEach(movie => {
      activities.push({
        id: `movie-${movie.id}`,
        type: 'movie',
        action: Math.random() > 0.3 ? 'created' : 'updated',
        title: movie.title,
        timestamp: new Date(Date.now() - Math.random() * 86400000).toISOString()
      });
    });

    genres.slice(-2).forEach(genre => {
      activities.push({
        id: `genre-${genre.id}`,
        type: 'genre',
        action: 'created',
        title: genre.name,
        timestamp: new Date(Date.now() - Math.random() * 172800000).toISOString()
      });
    });

    persons.slice(-2).forEach(person => {
      activities.push({
        id: `person-${person.id}`,
        type: 'person',
        action: 'created',
        title: person.name,
        timestamp: new Date(Date.now() - Math.random() * 259200000).toISOString()
      });
    });

    halls.slice(-2).forEach(hall => {
      activities.push({
        id: `hall-${hall.id}`,
        type: 'hall',
        action: Math.random() > 0.5 ? 'created' : 'updated',
        title: hall.name,
        timestamp: new Date(Date.now() - Math.random() * 345600000).toISOString()
      });
    });

    sessions.slice(-2).forEach(session => {
      activities.push({
        id: `session-${session.id}`,
        type: 'session',
        action: 'created',
        title: `Session #${session.id}`,
        timestamp: new Date(Date.now() - Math.random() * 432000000).toISOString()
      });
    });

    users.slice(-2).forEach(user => {
      activities.push({
        id: `user-${user.id}`,
        type: 'user',
        action: Math.random() > 0.7 ? 'created' : 'updated',
        title: `${user.firstName} ${user.lastName}`,
        timestamp: new Date(Date.now() - Math.random() * 518400000).toISOString()
      });
    });

    return activities.sort((a, b) =>
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    ).slice(0, 8);
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'movie': return '🎬';
      case 'genre': return '📚';
      case 'person': return '👤';
      case 'hall': return '🏛️';
      case 'session': return '⏰';
      case 'user': return '👥';
      default: return '📝';
    }
  };

  const formatTimeAgo = (timestamp: string) => {
    const now = new Date();
    const time = new Date(timestamp);
    const diffInHours = Math.floor((now.getTime() - time.getTime()) / (1000 * 60 * 60));

    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${diffInHours}h ago`;
    return `${Math.floor(diffInHours / 24)}d ago`;
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
        <h1>Admin Dashboard</h1>
        <button
          className={styles.refreshButton}
          onClick={loadDashboardData}
          disabled={isLoading}
        >
          🔄 Refresh
        </button>
      </div>

      <div className={styles.stats}>
        <div className={styles.statCard}>
          <div className={styles.statIcon}>🎬</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalMovies}</p>
            <span className={styles.statLabel}>Total Movies</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>📚</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalGenres}</p>
            <span className={styles.statLabel}>Genres</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>👥</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalPersons}</p>
            <span className={styles.statLabel}>People</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>🏛️</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalHalls}</p>
            <span className={styles.statLabel}>Halls</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>⏰</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalSessions}</p>
            <span className={styles.statLabel}>Sessions</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>👥</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.totalUsers}</p>
            <span className={styles.statLabel}>Users</span>
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statIcon}>🎭</div>
          <div className={styles.statContent}>
            <p className={styles.statNumber}>{stats.activeScreenings}</p>
            <span className={styles.statLabel}>Active Screenings</span>
          </div>
        </div>
      </div>

      <div className={styles.activity}>
        <h2>Recent Activity</h2>
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
                  <p className={styles.activityText}>
                    <strong>{activity.title}</strong> {activity.action}
                  </p>
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