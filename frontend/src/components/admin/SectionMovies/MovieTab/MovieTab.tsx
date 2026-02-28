import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useNotification } from '@/hooks/common/useNotification';
import { MovieList } from './MovieList/MovieList';
import { MovieForm } from './MovieForm/MovieForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Badge } from '@/components/ui/Badge/Badge';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { Notification } from '@/components/ui/Notification/Notification';
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
  const [isInitialLoading, setIsInitialLoading] = useState(true);

  const {
    adminCurrent,
    adminUpcoming,
    adminArchived,
    adminCurrentPagination,
    adminUpcomingPagination,
    adminArchivedPagination,
    loading: moviesLoading,
    getAdminCurrent,
    getAdminUpcoming,
    getAdminArchived,
    getById,
    remove,
    clearCache
  } = useMovies();

  const { notifications, showNotification, hideNotification } = useNotification();
  const showLoading = useDelayedLoading(moviesLoading && isInitialLoading, { delay: 150, minDisplayTime: 300 });
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const initialLoadRef = useRef(false);
  const loadingDataRef = useRef(false);
  const prevParamsRef = useRef<string>('');

  useEffect(() => {
    clearCache();
  }, [clearCache]);

  const loadTabCounts = useCallback(async () => {
    try {
      const [currentResponse, upcomingResponse, archivedResponse] = await Promise.all([
        getAdminCurrent({ page: 0, size: 12 }),
        getAdminUpcoming({ page: 0, size: 12 }),
        getAdminArchived({ page: 0, size: 12 })
      ]);

      setTabCounts({
        CURRENT: currentResponse?.totalElements || 0,
        UPCOMING: upcomingResponse?.totalElements || 0,
        ARCHIVED: archivedResponse?.totalElements || 0
      });
    } catch (error) {
      console.error('Failed to load tab counts:', error);
    }
  }, [getAdminCurrent, getAdminUpcoming, getAdminArchived]);

  const loadMovies = useCallback(async () => {
    const paramsKey = `${activeTab}_${currentPage}_${searchQuery}`;
    if (loadingDataRef.current || prevParamsRef.current === paramsKey) {
      return;
    }

    prevParamsRef.current = paramsKey;
    loadingDataRef.current = true;

    try {
      const baseParams = {
        page: currentPage,
        size: 12,
        sort: activeTab === 'UPCOMING' ? 'releaseDate,asc' : 'releaseDate,desc'
      };

      const params = searchQuery
        ? { ...baseParams, title: searchQuery }
        : baseParams;

      switch (activeTab) {
        case 'CURRENT':
          await getAdminCurrent(params);
          break;
        case 'UPCOMING':
          await getAdminUpcoming(params);
          break;
        case 'ARCHIVED':
          await getAdminArchived(params);
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
      setIsInitialLoading(false);
    }
  }, [activeTab, currentPage, searchQuery, getAdminCurrent, getAdminUpcoming, getAdminArchived, showNotification]);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      Promise.all([loadTabCounts(), loadMovies()]);
    } else {
      loadMovies();
    }
  }, [activeTab, currentPage, searchQuery, loadTabCounts, loadMovies]);

  const currentPagination = useMemo(() => {
    switch (activeTab) {
      case 'CURRENT': return adminCurrentPagination;
      case 'UPCOMING': return adminUpcomingPagination;
      case 'ARCHIVED': return adminArchivedPagination;
      default: return null;
    }
  }, [activeTab, adminCurrentPagination, adminUpcomingPagination, adminArchivedPagination]);

  const currentMovies: MovieCardResponse[] = useMemo(() => {
    switch (activeTab) {
      case 'CURRENT': return adminCurrent;
      case 'UPCOMING': return adminUpcoming;
      case 'ARCHIVED': return adminArchived;
      default: return [];
    }
  }, [activeTab, adminCurrent, adminUpcoming, adminArchived]);

  const handleTabChange = useCallback((tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setCurrentPage(0);
    prevParamsRef.current = '';
  }, []);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    searchTimeoutRef.current = setTimeout(() => {
      setCurrentPage(0);
      prevParamsRef.current = '';
    }, 500);
  }, []);

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
    prevParamsRef.current = '';
  }, []);

  const handleEdit = useCallback(async (movie: MovieCardResponse) => {
    try {
      const response = await getById(movie.id, true);
      if (response) {
        setEditingMovie(response);
        setIsModalOpen(true);
      }
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
      prevParamsRef.current = '';
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
    prevParamsRef.current = '';
    Promise.all([loadTabCounts(), loadMovies()]);
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

  if (showLoading && !currentMovies.length && !searchQuery) {
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
          loading={moviesLoading && !currentMovies.length}
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
        isDeleting={moviesLoading}
      />
    </div>
  );
};