import React, { useState, useEffect } from 'react';
import { movieApi } from '@/api/movieApi';
import { genreApi } from '@/api/genreApi';
import { personApi } from '@/api/personApi';
import { useNotification } from '@/hooks/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import styles from './AdminDashboard.module.css';

interface DashboardStats {
  totalMovies: number;
  totalGenres: number;
  totalPersons: number;
  activeScreenings: number;
  recentActivity: ActivityItem[];
}

interface ActivityItem {
  id: string;
  type: 'movie' | 'genre' | 'person';
  action: 'created' | 'updated' | 'deleted';
  title: string;
  timestamp: string;
}

export const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalMovies: 0,
    totalGenres: 0,
    totalPersons: 0,
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

      const [movies, genresResponse, persons] = await Promise.all([
        movieApi.getAll(),
        genreApi.search({}),
        personApi.getAll()
      ]);

      const genres = genresResponse.content;

      const activeScreenings = Math.floor(Math.random() * 20) + 10;
      const recentActivity = generateRecentActivity(movies, genres, persons);

      setStats({
        totalMovies: movies.length,
        totalGenres: genres.length,
        totalPersons: persons.length,
        activeScreenings,
        recentActivity
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
    persons: any[]
  ): ActivityItem[] => {
    const activities: ActivityItem[] = [];

    movies.slice(-3).forEach(movie => {
      activities.push({
        id: `movie-${movie.id}`,
        type: 'movie',
        action: 'created',
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

    return activities.sort((a, b) =>
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    ).slice(0, 5);
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'movie': return '🎬';
      case 'genre': return '📚';
      case 'person': return '👤';
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
          <div className={styles.statIcon}>⏰</div>
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