import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from './MovieList/MovieList';
import { MovieForm } from './MovieForm/MovieForm';
import {
  DeleteConfirmModal,
  Notification,
  Button,
  SearchInput,
  Badge,
  Pagination
} from '@/components/ui';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDetailResponse | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(null);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const {
    currentlyShowing,
    upcoming,
    archived,
    currentlyShowingPagination,
    upcomingPagination,
    archivedPagination,
    loading,
    getCurrentlyShowing,
    getUpcoming,
    getArchived,
    getById,
    remove,
    clearCache
  } = useMovies();

  useEffect(() => {
    clearCache();
  }, []);

  const loadMovies = useCallback(async () => {
    try {
      setErrorMessage('');
      const params = {
        page: currentPage,
        size: 12,
        title: searchQuery.trim() || undefined
      };

      switch (activeTab) {
        case 'CURRENT':
          await getCurrentlyShowing(params, true);
          break;
        case 'UPCOMING':
          await getUpcoming(params, true);
          break;
        case 'ARCHIVED':
          await getArchived(params);
          break;
      }
    } catch (err) {
      setErrorMessage(isApiErrorException(err) ? err.message : `Failed to load ${activeTab.toLowerCase()} movies`);
    }
  }, [activeTab, currentPage, searchQuery]);

  useEffect(() => {
    loadMovies();
  }, [activeTab, currentPage, searchQuery]);

  const currentPagination = useMemo(() => {
    switch (activeTab) {
      case 'CURRENT': return currentlyShowingPagination;
      case 'UPCOMING': return upcomingPagination;
      case 'ARCHIVED': return archivedPagination;
      default: return null;
    }
  }, [activeTab, currentlyShowingPagination, upcomingPagination, archivedPagination]);

  const currentMovies = useMemo(() => {
    switch (activeTab) {
      case 'CURRENT': return currentlyShowing;
      case 'UPCOMING': return upcoming;
      case 'ARCHIVED': return archived;
      default: return [];
    }
  }, [activeTab, currentlyShowing, upcoming, archived]);

  const tabCounts = useMemo(() => ({
    CURRENT: currentlyShowingPagination?.totalElements || 0,
    UPCOMING: upcomingPagination?.totalElements || 0,
    ARCHIVED: archivedPagination?.totalElements || 0
  }), [currentlyShowingPagination, upcomingPagination, archivedPagination]);

  const handleTabChange = useCallback((tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setCurrentPage(0);
  }, []);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  }, []);

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
  }, []);

  const handleEdit = useCallback(async (movie: MovieCardResponse) => {
    try {
      const fullMovie = await getById(movie.id, true);
      setEditingMovie(fullMovie);
      setIsModalOpen(true);
    } catch (error) {
      setErrorMessage(isApiErrorException(error) ? error.message : 'Failed to load movie details');
    }
  }, [getById]);

  const handleDeleteClick = useCallback((movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingMovie?.id) return;

    try {
      await remove(deletingMovie.id);
      setSuccessMessage('Movie deleted successfully!');
      await loadMovies();
    } catch (err) {
      if (isApiErrorException(err) && err.isConflict?.()) {
        setErrorMessage(`Cannot delete movie "${deletingMovie.title}" because it has associated sessions.`);
      } else {
        setErrorMessage(isApiErrorException(err) ? err.message : 'Failed to delete movie');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  }, [deletingMovie, remove, loadMovies]);

  const handleDeleteCancel = useCallback(() => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  }, []);

  const handleFormSuccess = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
    setSuccessMessage(editingMovie ? 'Movie updated successfully!' : 'Movie created successfully!');
    loadMovies();
  }, [editingMovie, loadMovies]);

  const handleFormCancel = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
  }, []);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  const handleCloseNotification = useCallback(() => {
    setErrorMessage('');
    setSuccessMessage('');
  }, []);

  const displayRange = useMemo(() => {
    if (!currentPagination) return { start: 0, end: 0, total: 0 };
    const pageSize = currentPagination.size || 12;
    const start = (currentPage * pageSize) + 1;
    const end = Math.min((currentPage + 1) * pageSize, currentPagination.totalElements);
    return { start, end, total: currentPagination.totalElements };
  }, [currentPagination, currentPage]);

  return (
    <div className={styles.container}>
      {successMessage && (
        <Notification
          id="success"
          message={successMessage}
          type="success"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

      {errorMessage && (
        <Notification
          id="error"
          message={errorMessage}
          type="error"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

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
            {tabCounts.CURRENT}
          </Badge>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'UPCOMING' ? styles.active : ''}`}
          onClick={() => handleTabChange('UPCOMING')}
        >
          <span className={styles.tabLabel}>Upcoming</span>
          <Badge variant={activeTab === 'UPCOMING' ? 'primary' : 'secondary'}>
            {tabCounts.UPCOMING}
          </Badge>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'ARCHIVED' ? styles.active : ''}`}
          onClick={() => handleTabChange('ARCHIVED')}
        >
          <span className={styles.tabLabel}>Archived</span>
          <Badge variant={activeTab === 'ARCHIVED' ? 'primary' : 'secondary'}>
            {tabCounts.ARCHIVED}
          </Badge>
        </button>
      </div>

      {currentPagination && currentPagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {displayRange.start}-{displayRange.end} of {displayRange.total} movies
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <div className={styles.content}>
        <MovieList
          movies={currentMovies}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
          loading={loading}
          onCreateNew={handleAddNew}
        />
      </div>

      {currentPagination && currentPagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={currentPage}
            totalPages={currentPagination.totalPages}
            totalElements={currentPagination.totalElements}
            pageSize={currentPagination.size || 12}
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
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={deletingMovie?.title}
        itemType="movie"
        isDeleting={loading}
      />
    </div>
  );
};