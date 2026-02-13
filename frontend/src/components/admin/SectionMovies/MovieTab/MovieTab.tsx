import React, { useState, useEffect, useCallback, useRef } from 'react';
import type { MovieDetailResponse, MovieCardResponse, MovieStatus } from '@/types/movie';
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
import styles from './MovieTab.module.css';

type MovieTabType = Extract<MovieStatus, 'CURRENT' | 'UPCOMING' | 'ARCHIVED'>;

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
  const [tabStats, setTabStats] = useState({
    CURRENT: 0,
    UPCOMING: 0,
    ARCHIVED: 0
  });

  const {
    allMovies: movies,
    pagination,
    loading,
    getCurrentlyShowing,
    getUpcoming,
    getAdminMovies,
    getById,
    remove
  } = useMovies();

  const initialLoadRef = useRef(false);

  const loadMovies = useCallback(async (tab: MovieTabType, page: number, query: string) => {
    try {
      setErrorMessage('');

      const params: any = {
        page,
        size: 12
      };

      if (query.trim()) {
        params.search = query;
      }

      if (tab === 'CURRENT') {
        await getCurrentlyShowing(params);
      } else if (tab === 'UPCOMING') {
        await getUpcoming(params);
      } else {
        await getAdminMovies({ ...params, status: 'ARCHIVED' });
      }
    } catch (err) {
      setErrorMessage(`Failed to load ${tab.toLowerCase()} movies`);
    }
  }, []);

  const loadTabStats = useCallback(async () => {
    try {
      const [currentResponse, upcomingResponse] = await Promise.all([
        getCurrentlyShowing({ page: 0, size: 1 }),
        getUpcoming({ page: 0, size: 1 })
      ]);

      const archivedResponse = await getAdminMovies({ page: 0, size: 1, status: 'ARCHIVED' });

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
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadMovies(activeTab, currentPage, searchQuery);
      loadTabStats().then(stats => setTabStats(stats));
    }
  }, []);

  const handleTabChange = (tab: MovieTabType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setCurrentPage(0);
    loadMovies(tab, 0, '');
  };

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
    loadMovies(activeTab, 0, query);
  }, [activeTab, loadMovies]);

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
    loadMovies(activeTab, page, searchQuery);
  }, [activeTab, searchQuery, loadMovies]);

  const handleEdit = useCallback(async (movie: MovieCardResponse) => {
    try {
      const fullMovie = await getById(movie.id);
      setEditingMovie(fullMovie);
      setIsModalOpen(true);
      setErrorMessage('');
    } catch (error) {
      setErrorMessage('Failed to load movie details');
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
      await loadMovies(activeTab, currentPage, searchQuery);
      const stats = await loadTabStats();
      setTabStats(stats);
    } catch (error) {
      setErrorMessage('Failed to delete movie');
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  }, [deletingMovie, remove, activeTab, currentPage, searchQuery, loadMovies, loadTabStats]);

  const handleDeleteCancel = useCallback(() => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  }, []);

  const handleFormSuccess = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
    setSuccessMessage(editingMovie ? 'Movie updated successfully!' : 'Movie created successfully!');
    loadMovies(activeTab, currentPage, searchQuery);
    loadTabStats().then(stats => setTabStats(stats));
  }, [activeTab, currentPage, searchQuery, editingMovie, loadMovies, loadTabStats]);

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

  const getDisplayRange = useCallback(() => {
    if (!pagination) return { start: 0, end: 0, total: 0 };
    const pageSize = pagination.size || 12;
    const start = (currentPage * pageSize) + 1;
    const end = Math.min((currentPage + 1) * pageSize, pagination.totalElements);
    return { start, end, total: pagination.totalElements };
  }, [pagination, currentPage]);

  const { start, end, total } = getDisplayRange();

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
            pageSize={pagination.size || 12}
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