import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useNotification } from '@/hooks/common/useNotification';
import { MovieList } from './MovieList/MovieList';
import { MovieForm } from './MovieForm/MovieForm';
import {
  DeleteConfirmModal,
  Button,
  SearchInput,
  Badge,
  Pagination,
  LoadingSpinner
} from '@/components/ui';
import { Notification } from '@/components/ui/Notification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
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
  const [tabCounts, setTabCounts] = useState({
    CURRENT: 0,
    UPCOMING: 0,
    ARCHIVED: 0
  });

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

  const { notifications, showNotification, hideNotification } = useNotification();
  const showDelayedLoading = useDelayedLoading(loading, 300);
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const initialLoadRef = useRef(false);
  const loadingDataRef = useRef(false);

  useEffect(() => {
    clearCache();
  }, []);

  const loadTabCounts = useCallback(async () => {
    try {
      const [currentResponse, upcomingResponse, archivedResponse] = await Promise.all([
        getCurrentlyShowing({ page: 0, size: 1 }, true),
        getUpcoming({ page: 0, size: 1 }, true),
        getArchived({ page: 0, size: 1 })
      ]);

      setTabCounts({
        CURRENT: currentResponse?.totalElements || 0,
        UPCOMING: upcomingResponse?.totalElements || 0,
        ARCHIVED: archivedResponse?.totalElements || 0
      });
    } catch (error) {
      console.error('Failed to load tab counts:', error);
    }
  }, [getCurrentlyShowing, getUpcoming, getArchived]);

  const loadMovies = useCallback(async () => {
    if (loadingDataRef.current) return;

    loadingDataRef.current = true;

    try {
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
      if (isApiErrorException(err)) {
        showNotification(err.message, 'error');
      } else {
        showNotification(`Failed to load ${activeTab.toLowerCase()} movies`, 'error');
      }
    } finally {
      loadingDataRef.current = false;
    }
  }, [activeTab, currentPage, searchQuery, getCurrentlyShowing, getUpcoming, getArchived, showNotification]);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadTabCounts();
      loadMovies();
    }
  }, []);

  useEffect(() => {
    if (initialLoadRef.current) {
      const timer = setTimeout(() => {
        loadMovies();
      }, 100);
      return () => clearTimeout(timer);
    }
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

  const handleTabChange = useCallback((tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setCurrentPage(0);
  }, []);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    searchTimeoutRef.current = setTimeout(() => {
      setCurrentPage(0);
    }, 500);
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
      showNotification(isApiErrorException(error) ? error.message : 'Failed to load movie details', 'error');
    }
  }, [getById, showNotification]);

  const handleDeleteClick = useCallback((movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingMovie?.id) return;

    try {
      await remove(deletingMovie.id);
      showNotification(`Movie "${deletingMovie.title}" deleted successfully!`, 'success');

      const newPage = currentMovies.length === 1 && currentPage > 0 ? currentPage - 1 : currentPage;
      setCurrentPage(newPage);
      await Promise.all([loadTabCounts(), loadMovies()]);
    } catch (err) {
      if (isApiErrorException(err) && err.isConflict?.()) {
        showNotification(`Cannot delete movie "${deletingMovie.title}" because it has associated sessions.`, 'error');
      } else {
        showNotification(isApiErrorException(err) ? err.message : 'Failed to delete movie', 'error');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  }, [deletingMovie, remove, currentMovies.length, currentPage, loadMovies, loadTabCounts, showNotification]);

  const handleDeleteCancel = useCallback(() => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  }, []);

  const handleFormSuccess = useCallback((result?: MovieDetailResponse) => {
    setIsModalOpen(false);
    setEditingMovie(null);
    if (result) {
      showNotification(`Movie "${result.title}" successfully ${editingMovie ? 'updated' : 'created'}!`, 'success');
    }
    loadMovies();
    loadTabCounts();
  }, [editingMovie, loadMovies, loadTabCounts, showNotification]);

  const handleFormCancel = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
  }, []);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) clearTimeout(searchTimeoutRef.current);
    };
  }, []);

  const displayRange = useMemo(() => {
    if (!currentPagination) return { start: 0, end: 0, total: 0 };
    const pageSize = currentPagination.size || 12;
    const start = (currentPage * pageSize) + 1;
    const end = Math.min((currentPage + 1) * pageSize, currentPagination.totalElements || 0);
    return { start, end, total: currentPagination.totalElements || 0 };
  }, [currentPagination, currentPage]);

  if (showDelayedLoading && !currentMovies.length && !searchQuery) {
    return <div className={styles.loading}><LoadingSpinner text={`Loading ${activeTab.toLowerCase()} movies...`} /></div>;
  }

  return (
    <div className={styles.container}>
      {notifications.map((notification) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={notification.duration}
        />
      ))}

      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <Button onClick={handleAddNew} variant="primary">Add Movie</Button>
      </div>

      <div className={styles.searchContainer}>
        <SearchInput onSearch={handleSearch} placeholder="Search movies by title..." delay={500} />
      </div>

      <div className={styles.tabs}>
        {(['CURRENT', 'UPCOMING', 'ARCHIVED'] as const).map((tab) => (
          <button
            key={tab}
            className={`${styles.tab} ${activeTab === tab ? styles.active : ''}`}
            onClick={() => handleTabChange(tab)}
          >
            <span className={styles.tabLabel}>
              {tab === 'CURRENT' ? 'Currently Showing' : tab === 'UPCOMING' ? 'Upcoming' : 'Archived'}
            </span>
            <Badge variant={activeTab === tab ? 'primary' : 'secondary'}>{tabCounts[tab]}</Badge>
          </button>
        ))}
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
            totalElements={currentPagination.totalElements || 0}
            pageSize={currentPagination.size || 12}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading}
            showInfo={false}
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