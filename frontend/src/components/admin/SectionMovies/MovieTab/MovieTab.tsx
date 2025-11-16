import React, { useState, useEffect, useCallback } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { MovieStatus } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { useMovieMutation } from '@/hooks/features/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { usePagination } from '@/hooks/common/usePagination';
import { MovieList } from './MovieList';
import { MovieForm } from './MovieForm';
import {
  DeleteConfirmModal,
  Notification,
  Button,
  SearchInput,
  Badge
} from '@/components/ui';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDetailResponse | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(null);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');
  const [searchQuery, setSearchQuery] = useState('');
  const [movies, setMovies] = useState<MovieCardResponse[]>([]);
  const [allMovies, setAllMovies] = useState<MovieCardResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [, setError] = useState<string | null>(null);

  const { reset: resetPagination } = usePagination();
  const { deleteMovie, loading: mutationLoading, error: mutationError } = useMovieMutation();
  const { notifications, showNotification, hideNotification } = useNotification();

  const loadAllMovies = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await movieApi.getCurrentlyShowing();
      const upcoming = await movieApi.getUpcoming();
      const archived = await movieApi.getArchived();

      const all = [...response, ...upcoming, ...archived];
      setAllMovies(all);
      return all;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load movies';
      setError(message);
      showNotification(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [showNotification]);

  const loadMoviesByStatus = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      let response: MovieCardResponse[];
      switch (activeTab) {
        case 'CURRENT':
          response = await movieApi.getCurrentlyShowing();
          break;
        case 'UPCOMING':
          response = await movieApi.getUpcoming();
          break;
        case 'ARCHIVED':
          response = await movieApi.getArchived();
          break;
        default:
          response = [];
      }
      setMovies(response);
    } catch (err) {
      const message = err instanceof Error ? err.message : `Failed to load ${activeTab.toLowerCase()} movies`;
      setError(message);
      showNotification(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [activeTab, showNotification]);

  const searchMovies = useCallback(async (query: string) => {
    if (!query.trim()) {
      await loadMoviesByStatus();
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const filteredMovies = allMovies.filter(movie =>
        movie.title.toLowerCase().includes(query.toLowerCase()) ||
        movie.slug.toLowerCase().includes(query.toLowerCase())
      );
      setMovies(filteredMovies);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to search movies';
      setError(message);
      showNotification(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [allMovies, loadMoviesByStatus, showNotification]);

  useEffect(() => {
    loadAllMovies();
  }, [loadAllMovies]);

  useEffect(() => {
    if (searchQuery) {
      searchMovies(searchQuery);
    } else {
      loadMoviesByStatus();
    }
  }, [searchQuery, activeTab, searchMovies, loadMoviesByStatus]);

  useEffect(() => {
    if (mutationError) {
      showNotification(mutationError, 'error');
    }
  }, [mutationError, showNotification]);

  const handleTabChange = (tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    resetPagination();
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    resetPagination();
  };

  const handleEdit = async (movie: MovieCardResponse) => {
    try {
      const fullMovie = await movieApi.getById(movie.id);
      setEditingMovie(fullMovie);
      setIsModalOpen(true);
    } catch (error) {
      showNotification('Failed to load movie details', 'error');
      console.error('Failed to load movie details:', error);
    }
  };

  const handleDeleteClick = (movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingMovie?.id) return;

    try {
      await deleteMovie(deletingMovie.id);
      showNotification('Movie deleted successfully!', 'success');
      await loadAllMovies();
      await loadMoviesByStatus();
    } catch (error) {
      console.error('Failed to delete movie:', error);
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  };

  const handleFormSuccess = () => {
    setIsModalOpen(false);
    setEditingMovie(null);
    loadAllMovies();
    loadMoviesByStatus();
    showNotification(
      editingMovie ? 'Movie updated successfully!' : 'Movie created successfully!',
      'success'
    );
  };

  const handleFormCancel = () => {
    setIsModalOpen(false);
    setEditingMovie(null);
  };

  const handleAddNew = () => {
    setEditingMovie(null);
    setIsModalOpen(true);
  };

  const getTabStats = () => {
    return {
      CURRENT: allMovies.filter(m => m.status === MovieStatus.CURRENT).length,
      UPCOMING: allMovies.filter(m => m.status === MovieStatus.UPCOMING).length,
      ARCHIVED: allMovies.filter(m => m.status === MovieStatus.ARCHIVED).length
    };
  };

  const tabStats = getTabStats();

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <Button
          onClick={handleAddNew}
          variant="primary"
        >
          Add New Movie
        </Button>
      </div>

      <div className={styles.searchContainer}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search movies by title..."
          delay={300}
        />
      </div>

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === 'CURRENT' ? styles.active : ''}`}
          onClick={() => handleTabChange('CURRENT')}
        >
          <span className={styles.tabLabel}>Currently Showing</span>
          <Badge variant={activeTab === 'CURRENT' ? 'primary' : 'secondary'}>
            {tabStats.CURRENT}
          </Badge>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'UPCOMING' ? styles.active : ''}`}
          onClick={() => handleTabChange('UPCOMING')}
        >
          <span className={styles.tabLabel}>Upcoming</span>
          <Badge variant={activeTab === 'UPCOMING' ? 'primary' : 'secondary'}>
            {tabStats.UPCOMING}
          </Badge>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'ARCHIVED' ? styles.active : ''}`}
          onClick={() => handleTabChange('ARCHIVED')}
        >
          <span className={styles.tabLabel}>Archived</span>
          <Badge variant={activeTab === 'ARCHIVED' ? 'primary' : 'secondary'}>
            {tabStats.ARCHIVED}
          </Badge>
        </button>
      </div>

      <div className={styles.content}>
        <MovieList
          movies={movies}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
          loading={loading}
        />
      </div>

      {isModalOpen && (
        <MovieForm
          movie={editingMovie}
          onSuccess={handleFormSuccess}
          onCancel={handleFormCancel}
          showNotification={showNotification}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={deletingMovie?.title}
        itemType="movie"
        isDeleting={mutationLoading}
      />

      {notifications.map((notification) => (
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