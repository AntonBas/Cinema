import React, { useState, useEffect, useCallback } from 'react';
import { PersonTabs } from './PersonTabs';
import { PersonList } from './PersonList';
import { PersonForm } from './PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Notification } from '@/components/ui/Notification';
import { SearchInput } from '@/components/ui/SearchInput';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { useNotification } from '@/hooks/common/useNotification';
import { usePersonSearch, usePersonMutation } from '@/hooks/features/persons';
import type { PersonResponse, PersonRequest } from '@/types/person';
import { PersonRole } from '@/types/person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonResponse | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonResponse | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    [PersonRole.ACTOR]: 0,
    [PersonRole.DIRECTOR]: 0,
    [PersonRole.SCREENWRITER]: 0,
  });

  const {
    persons,
    pagination,
    loading,
    error: searchError,
    searchPersons,
    getAllPersonsPaginated,
    getByRole,
    clearError: clearSearchError
  } = usePersonSearch();

  const {
    createPerson,
    updatePerson,
    deletePerson,
    loading: mutationLoading,
    error: mutationError,
    clearError: clearMutationError
  } = usePersonMutation();

  const { notifications, showNotification, hideNotification } = useNotification();

  const loadPersons = useCallback(async () => {
    try {
      if (searchQuery.trim()) {
        await searchPersons({
          query: searchQuery,
          role: activeTab === 'ALL' ? undefined : activeTab,
          page: currentPage,
          size: 12
        });
      } else if (activeTab === 'ALL') {
        await getAllPersonsPaginated(currentPage, 12);
      } else {
        await getByRole(activeTab, currentPage, 12);
      }
    } catch (error) {
      console.error('Failed to load persons:', error);
    }
  }, [activeTab, currentPage, searchQuery, searchPersons, getAllPersonsPaginated, getByRole]);

  const refreshAllCounts = useCallback(async () => {
    if (searchQuery.trim()) return;

    try {
      const [allResult, actorsResult, directorsResult, screenwritersResult] = await Promise.all([
        getAllPersonsPaginated(0, 1),
        getByRole(PersonRole.ACTOR, 0, 1),
        getByRole(PersonRole.DIRECTOR, 0, 1),
        getByRole(PersonRole.SCREENWRITER, 0, 1),
      ]);

      setTotalCounts({
        ALL: allResult.totalElements,
        [PersonRole.ACTOR]: actorsResult.totalElements,
        [PersonRole.DIRECTOR]: directorsResult.totalElements,
        [PersonRole.SCREENWRITER]: screenwritersResult.totalElements,
      });
    } catch (error) {
      console.error('Failed to refresh counts:', error);
    }
  }, [getAllPersonsPaginated, getByRole, searchQuery]);

  useEffect(() => {
    loadPersons();
  }, [loadPersons]);

  useEffect(() => {
    if (!searchQuery.trim()) {
      refreshAllCounts();
    }
  }, [searchQuery, refreshAllCounts]);

  useEffect(() => {
    if (searchError) {
      showNotification(searchError, 'error');
      clearSearchError();
    }
  }, [searchError, showNotification, clearSearchError]);

  useEffect(() => {
    if (mutationError) {
      showNotification(mutationError, 'error');
      clearMutationError();
    }
  }, [mutationError, showNotification, clearMutationError]);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  }, []);

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
    setSearchQuery('');
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleSubmit = async (data: PersonRequest) => {
    try {
      if (editingPerson?.id) {
        await updatePerson(editingPerson.id, data);
        showNotification('Person updated successfully!', 'success');
      } else {
        await createPerson(data);
        showNotification('Person created successfully!', 'success');
      }

      resetForm();
      await loadPersons();
      await refreshAllCounts();
    } catch (error) {
      console.error('Failed to save person:', error);
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
      await deletePerson(personToDelete.id);
      showNotification('Person deleted successfully!', 'success');
      await loadPersons();
      await refreshAllCounts();
    } catch (error) {
      console.error('Failed to delete person:', error);
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

  const getTabStats = () => {
    if (searchQuery.trim()) {
      const filtered = persons.filter(person =>
        activeTab === 'ALL' || person.role === activeTab
      );

      const actors = persons.filter(p => p.role === PersonRole.ACTOR);
      const directors = persons.filter(p => p.role === PersonRole.DIRECTOR);
      const screenwriters = persons.filter(p => p.role === PersonRole.SCREENWRITER);

      return {
        ALL: activeTab === 'ALL' ? filtered.length : persons.length,
        [PersonRole.ACTOR]: actors.length,
        [PersonRole.DIRECTOR]: directors.length,
        [PersonRole.SCREENWRITER]: screenwriters.length,
      };
    }

    return totalCounts;
  };

  const tabStats = getTabStats();

  if (loading && !persons.length) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
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

      {pagination && (
        <div className={styles.resultsInfo}>
          Showing {(currentPage * 12) + 1}-
          {Math.min((currentPage + 1) * 12, pagination.totalElements)} of {pagination.totalElements} people
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <PersonTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        stats={tabStats}
      />

      <PersonList
        persons={persons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.pageSize}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading}
            className={styles.pagination}
          />
        </div>
      )}

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={resetForm}
          isLoading={mutationLoading}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={mutationLoading}
      />

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