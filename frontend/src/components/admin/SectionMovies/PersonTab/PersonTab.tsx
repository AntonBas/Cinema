import React, { useState, useEffect, useCallback, useRef } from 'react';
import { PersonTabs } from './PersonTabs/PersonTabs';
import { PersonList } from './PersonList/PersonList';
import { PersonForm } from './PersonForm/PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Notification } from '@/components/ui/Notification';
import { SearchInput } from '@/components/ui/SearchInput';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { usePerson } from '@/hooks/features/persons/usePerson';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonResponse | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [notifications, setNotifications] = useState<Array<{ id: string; message: string; type: 'success' | 'error' }>>([]);
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    ACTOR: 0,
    DIRECTOR: 0,
    SCREENWRITER: 0,
  });
  const [tabPagination, setTabPagination] = useState({
    ALL: { totalPages: 0, totalElements: 0, size: 12 },
    ACTOR: { totalPages: 0, totalElements: 0, size: 12 },
    DIRECTOR: { totalPages: 0, totalElements: 0, size: 12 },
    SCREENWRITER: { totalPages: 0, totalElements: 0, size: 12 },
  });

  const isMountedRef = useRef(true);
  const initialLoadRef = useRef(false);
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const loadingRef = useRef(false);
  const notificationCounterRef = useRef(0);

  const {
    allPersons,
    actors,
    directors,
    screenwriters,
    pagination,
    loading,
    getAll,
    getActors,
    getDirectors,
    getScreenwriters,
    create,
    update,
    remove
  } = usePerson();

  const addNotification = useCallback((message: string, type: 'success' | 'error') => {
    const id = `notification-${notificationCounterRef.current++}`;
    setNotifications(prev => [...prev, { id, message, type }]);
  }, []);

  const removeNotification = useCallback((id: string) => {
    setNotifications(prev => prev.filter(notification => notification.id !== id));
  }, []);

  const getLoadMethod = useCallback(() => {
    switch (activeTab) {
      case 'ALL':
        return getAll;
      case 'ACTOR':
        return getActors;
      case 'DIRECTOR':
        return getDirectors;
      case 'SCREENWRITER':
        return getScreenwriters;
      default:
        return getAll;
    }
  }, [activeTab, getAll, getActors, getDirectors, getScreenwriters]);

  const getCurrentData = useCallback(() => {
    switch (activeTab) {
      case 'ALL':
        return allPersons;
      case 'ACTOR':
        return actors;
      case 'DIRECTOR':
        return directors;
      case 'SCREENWRITER':
        return screenwriters;
      default:
        return allPersons;
    }
  }, [activeTab, allPersons, actors, directors, screenwriters]);

  const updateTabPagination = useCallback((tab: PersonRole | 'ALL', data: any) => {
    if (data) {
      setTabPagination(prev => ({
        ...prev,
        [tab]: {
          totalPages: data.totalPages || 0,
          totalElements: data.totalElements || 0,
          size: data.size || 12
        }
      }));
    }
  }, []);

  useEffect(() => {
    if (allPersons.length > 0 && pagination) {
      updateTabPagination('ALL', pagination);
    }
  }, [allPersons, pagination, updateTabPagination]);

  useEffect(() => {
    if (actors.length > 0 && pagination) {
      updateTabPagination('ACTOR', pagination);
    }
  }, [actors, pagination, updateTabPagination]);

  useEffect(() => {
    if (directors.length > 0 && pagination) {
      updateTabPagination('DIRECTOR', pagination);
    }
  }, [directors, pagination, updateTabPagination]);

  useEffect(() => {
    if (screenwriters.length > 0 && pagination) {
      updateTabPagination('SCREENWRITER', pagination);
    }
  }, [screenwriters, pagination, updateTabPagination]);

  const loadPersons = useCallback(async (page: number, query: string) => {
    if (loadingRef.current) return;

    try {
      loadingRef.current = true;
      const loadMethod = getLoadMethod();
      const result = await loadMethod({
        name: query.trim() || undefined,
        page,
        size: 12
      });

      if (result) {
        updateTabPagination(activeTab, result);
      }
    } catch (error) {
      if (isMountedRef.current) {
        addNotification('Failed to load persons', 'error');
      }
    } finally {
      loadingRef.current = false;
    }
  }, [getLoadMethod, addNotification, activeTab, updateTabPagination]);

  const loadCounts = useCallback(async () => {
    try {
      const [allResult, actorsResult, directorsResult, screenwritersResult] = await Promise.allSettled([
        getAll({ page: 0, size: 1 }),
        getActors({ page: 0, size: 1 }),
        getDirectors({ page: 0, size: 1 }),
        getScreenwriters({ page: 0, size: 1 })
      ]);

      if (isMountedRef.current) {
        setTotalCounts({
          ALL: allResult.status === 'fulfilled' ? allResult.value?.totalElements || 0 : 0,
          ACTOR: actorsResult.status === 'fulfilled' ? actorsResult.value?.totalElements || 0 : 0,
          DIRECTOR: directorsResult.status === 'fulfilled' ? directorsResult.value?.totalElements || 0 : 0,
          SCREENWRITER: screenwritersResult.status === 'fulfilled' ? screenwritersResult.value?.totalElements || 0 : 0,
        });

        if (allResult.status === 'fulfilled' && allResult.value) {
          updateTabPagination('ALL', allResult.value);
        }
        if (actorsResult.status === 'fulfilled' && actorsResult.value) {
          updateTabPagination('ACTOR', actorsResult.value);
        }
        if (directorsResult.status === 'fulfilled' && directorsResult.value) {
          updateTabPagination('DIRECTOR', directorsResult.value);
        }
        if (screenwritersResult.status === 'fulfilled' && screenwritersResult.value) {
          updateTabPagination('SCREENWRITER', screenwritersResult.value);
        }
      }
    } catch (error) {
      console.error('Failed to load counts:', error);
    }
  }, [getAll, getActors, getDirectors, getScreenwriters, updateTabPagination]);

  useEffect(() => {
    isMountedRef.current = true;

    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadPersons(currentPage, searchQuery);
      loadCounts();
    }

    return () => {
      isMountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    if (!initialLoadRef.current) return;

    const timeoutId = setTimeout(() => {
      loadPersons(currentPage, searchQuery);
    }, 100);

    return () => clearTimeout(timeoutId);
  }, [activeTab, currentPage, searchQuery]);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      loadPersons(0, query);
    }, 300);
  }, [loadPersons]);

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
    setSearchQuery('');
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  useEffect(() => {
    if (initialLoadRef.current) {
      loadPersons(currentPage, searchQuery);
    }
  }, [currentPage]);

  const handleSubmit = async (data: PersonRequest) => {
    try {
      let result;
      if (editingPerson?.id) {
        result = await update(editingPerson.id, data);
        addNotification(`Person "${result.name}" updated successfully!`, 'success');
      } else {
        result = await create(data);
        addNotification(`Person "${result.name}" created successfully!`, 'success');
      }

      resetForm();
      await Promise.all([
        loadPersons(currentPage, searchQuery),
        loadCounts()
      ]);
    } catch (err) {
      if (isApiErrorException(err)) {
        const validationError = err.getFirstValidationError();
        if (validationError) {
          addNotification(validationError, 'error');
        } else {
          addNotification(err.message, 'error');
        }
      } else {
        addNotification(err instanceof Error ? err.message : 'Failed to save person', 'error');
      }
    }
  };

  const handleEdit = (person: PersonResponse) => {
    setEditingPerson(person);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (person: PersonResponse) => {
    setPersonToDelete(person);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!personToDelete?.id) return;

    try {
      await remove(personToDelete.id);
      addNotification(`Person "${personToDelete.name}" deleted successfully!`, 'success');

      const currentData = getCurrentData();
      const willBeEmpty = currentData.length === 1;
      const newPage = willBeEmpty && currentPage > 0 ? currentPage - 1 : currentPage;

      setCurrentPage(newPage);

      await Promise.all([
        loadPersons(newPage, searchQuery),
        loadCounts()
      ]);

    } catch (err) {
      if (isApiErrorException(err)) {
        if (err.isConflict()) {
          addNotification(`Cannot delete person "${personToDelete.name}" because they are associated with movies.`, 'error');
        } else {
          addNotification(err.message, 'error');
        }
      } else {
        addNotification(err instanceof Error ? err.message : 'Failed to delete person', 'error');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setPersonToDelete(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingPerson(null);
  };

  const handleAddNew = () => {
    setEditingPerson(null);
    setIsModalOpen(true);
  };

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const getTabStats = () => {
    if (searchQuery.trim()) {
      return {
        ALL: allPersons.length,
        ACTOR: actors.length,
        DIRECTOR: directors.length,
        SCREENWRITER: screenwriters.length,
      };
    }
    return totalCounts;
  };

  const getCurrentPagination = () => {
    return tabPagination[activeTab];
  };

  const getDisplayRange = () => {
    const currentPagination = getCurrentPagination();
    if (!currentPagination) return { start: 0, end: 0 };

    const pageSize = currentPagination.size || 12;
    const totalElements = currentPagination.totalElements || 0;

    if (totalElements === 0) return { start: 0, end: 0 };

    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

    return { start: startItem, end: endItem };
  };

  const tabStats = getTabStats();
  const { start, end } = getDisplayRange();
  const currentData = getCurrentData();
  const isLoading = loading;
  const currentPagination = getCurrentPagination();

  const shouldShowPagination = () => {
    if (!currentPagination) return false;
    return currentPagination.totalPages > 1;
  };

  if (isLoading && !currentData.length && !searchQuery) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {notifications.map((notification, index) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={true}
          onClose={removeNotification}
          duration={4000}
          position={index}
        />
      ))}

      <div className={styles.header}>
        <h2>People Management</h2>
        <Button
          variant="primary"
          onClick={handleAddNew}
          className={styles.addButton}
        >
          Add Person
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search people by name..."
          delay={300}
          className={styles.searchInput}
        />
      </div>

      {currentPagination && currentPagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {start}-{end} of {currentPagination.totalElements} people
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <PersonTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        stats={tabStats}
      />

      <PersonList
        persons={currentData}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {shouldShowPagination() && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={currentPage}
            totalPages={currentPagination?.totalPages || 1}
            totalElements={currentPagination?.totalElements || 0}
            pageSize={currentPagination?.size || 12}
            onPageChange={handlePageChange}
            variant="pages"
            loading={isLoading}
            className={styles.pagination}
            showInfo={false}
          />
        </div>
      )}

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={resetForm}
          isLoading={loading}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={loading}
      />
    </div>
  );
};