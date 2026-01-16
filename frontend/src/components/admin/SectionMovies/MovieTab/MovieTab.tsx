import React, { useState, useEffect, useCallback } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { useMovieMutation } from '@/hooks/features/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { MovieList } from './MovieList';
import { MovieForm } from './MovieForm';
import {
  DeleteConfirmModal,
  Notification,
  Button,
  SearchInput,
  Badge,
  Pagination
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
  const [pagination, setPagination] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(12);
  const [tabStats, setTabStats] = useState({
    CURRENT: 0,
    UPCOMING: 0,
    ARCHIVED: 0
  });

  const { deleteMovie, loading: mutationLoading, error: mutationError } = useMovieMutation();
  const { notifications, showNotification, hideNotification } = useNotification();

  const loadMovies = useCallback(async () => {
    setLoading(true);
    try {
      let response;

      if (searchQuery.trim()) {
        response = await movieApi.public.getFilteredMovies(
          searchQuery,
          activeTab === 'CURRENT' ? 'CURRENT' :
            activeTab === 'UPCOMING' ? 'UPCOMING' :
              'ARCHIVED',
          currentPage,
          pageSize
        );
      } else {
        switch (activeTab) {
          case 'CURRENT':
            response = await movieApi.public.getCurrentlyShowingPaginated(currentPage, pageSize);
            break;
          case 'UPCOMING':
            response = await movieApi.public.getUpcomingPaginated(currentPage, pageSize);
            break;
          case 'ARCHIVED':
            response = await movieApi.admin.getArchivedMovies(currentPage, pageSize);
            break;
          default:
            response = { content: [], totalElements: 0, totalPages: 0, size: pageSize, number: 0, first: true, last: true, empty: true };
        }
      }

      setMovies(response.content);
      setPagination(response);
    } catch (err) {
      const message = err instanceof Error ? err.message : `Failed to load ${activeTab.toLowerCase()} movies`;
      showNotification(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [activeTab, currentPage, searchQuery, pageSize, showNotification]);

  const loadTabStats = useCallback(async () => {
    try {
      const [currentResponse, upcomingResponse, archivedResponse] = await Promise.all([
        movieApi.public.getCurrentlyShowingPaginated(0, 1),
        movieApi.public.getUpcomingPaginated(0, 1),
        movieApi.admin.getArchivedMovies(0, 1)
      ]);

      return {
        CURRENT: currentResponse.totalElements,
        UPCOMING: upcomingResponse.totalElements,
        ARCHIVED: archivedResponse.totalElements
      };
    } catch (error) {
      console.error('Failed to load tab stats:', error);
      return { CURRENT: 0, UPCOMING: 0, ARCHIVED: 0 };
    }
  }, []);

  useEffect(() => {
    loadMovies();
  }, [loadMovies]);

  useEffect(() => {
    loadTabStats().then(stats => setTabStats(stats));
  }, [loadTabStats]);

  useEffect(() => {
    if (mutationError) {
      showNotification(mutationError, 'error');
    }
  }, [mutationError, showNotification]);

  useEffect(() => {
    const intervalId = setInterval(() => {
      if (!isModalOpen && !isDeleteModalOpen) {
        loadMovies();
        loadTabStats().then(stats => setTabStats(stats));
      }
    }, 30000);

    return () => clearInterval(intervalId);
  }, [isModalOpen, isDeleteModalOpen, loadMovies, loadTabStats]);

  const handleTabChange = (tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setCurrentPage(0);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleEdit = async (movie: MovieCardResponse) => {
    try {
      const fullMovie = await movieApi.public.getById(movie.id);
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
      await loadMovies();
      await loadTabStats().then(stats => setTabStats(stats));
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
    loadMovies();
    loadTabStats().then(stats => setTabStats(stats));
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

  const getDisplayRange = () => {
    if (!pagination) return { start: 0, end: 0, total: 0 };

    const start = (currentPage * pageSize) + 1;
    const end = Math.min((currentPage + 1) * pageSize, pagination.totalElements);

    return { start, end, total: pagination.totalElements };
  };

  const { start, end, total } = getDisplayRange();

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <Button onClick={handleAddNew} variant="primary">
          Add Movie
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

      {pagination && !pagination.empty && (
        <div className={styles.resultsInfo}>
          Showing {start}-{end} of {total} movies
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <div className={styles.content}>
        <MovieList
          movies={movies}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
          loading={loading}
          onCreateNew={handleAddNew}
        />
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pageSize}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading}
          />
        </div>
      )}

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