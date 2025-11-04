import React, { useState, useEffect } from 'react';
import { PersonTabs } from './PersonTabs';
import { PersonList } from './PersonList';
import { PersonForm } from './PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification';
import { usePersonSearch, usePersonMutation } from '@/hooks/features/persons';
import type { PersonDto, PersonRequest } from '@/types/person';
import { PersonRole } from '@/types/person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonDto | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonDto | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    [PersonRole.ACTOR]: 0,
    [PersonRole.DIRECTOR]: 0,
    [PersonRole.SCREENWRITER]: 0,
  });

  const { persons, pagination, loading, error: searchError, searchPersons } = usePersonSearch();
  const { createPerson, updatePerson, deletePerson, loading: mutationLoading, error: mutationError } = usePersonMutation();
  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(searchQuery), 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    loadPersons(true);
  }, [activeTab, debouncedSearch]);

  useEffect(() => {
    if (searchError) showNotification(searchError, 'error');
  }, [searchError, showNotification]);

  useEffect(() => {
    if (mutationError) showNotification(mutationError, 'error');
  }, [mutationError, showNotification]);

  useEffect(() => {
    refreshAllCounts();
  }, []);

  const loadPersons = async (reset: boolean = false, pageOverride?: number) => {
    const page = reset ? 0 : (pageOverride ?? currentPage);
    const role = activeTab === 'ALL' ? undefined : activeTab;

    await searchPersons({ query: debouncedSearch, role, page, size: 10 });

    if (pagination) {
      setHasMore(!pagination.last);
      setCurrentPage(page);
    }
  };

  const refreshAllCounts = async () => {
    const roles: (PersonRole | 'ALL')[] = ['ALL', PersonRole.ACTOR, PersonRole.DIRECTOR, PersonRole.SCREENWRITER];

    const newCounts = {
      ALL: 0,
      [PersonRole.ACTOR]: 0,
      [PersonRole.DIRECTOR]: 0,
      [PersonRole.SCREENWRITER]: 0,
    };

    for (const role of roles) {
      const roleFilter = role === 'ALL' ? undefined : role;
      try {
        const result = await searchPersons({ query: '', role: roleFilter, page: 0, size: 1 });
        if (result && typeof result.totalElements === 'number') {
          newCounts[role] = result.totalElements;
        }
      } catch (error) {
        console.error(`Failed to load count for ${role}:`, error);
      }
    }

    setTotalCounts(newCounts);
  };

  const loadMore = () => {
    if (!loading && hasMore) {
      const nextPage = currentPage + 1;
      loadPersons(false, nextPage);
    }
  };

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const clearSearch = () => {
    setSearchQuery('');
    setDebouncedSearch('');
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
      await loadPersons(true);
      await refreshAllCounts();
    } catch { }
  };

  const handleEdit = (person: PersonDto) => {
    setEditingPerson(person);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (person: PersonDto) => {
    setPersonToDelete(person);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!personToDelete?.id) return;
    try {
      await deletePerson(personToDelete.id);
      showNotification('Person deleted successfully!', 'success');
      await loadPersons(true);
      await refreshAllCounts();
    } catch { } finally {
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

  const getDisplayedCounts = () => {
    if (debouncedSearch) {
      return {
        ALL: persons.length,
        [PersonRole.ACTOR]: persons.filter(p => p.role === PersonRole.ACTOR).length,
        [PersonRole.DIRECTOR]: persons.filter(p => p.role === PersonRole.DIRECTOR).length,
        [PersonRole.SCREENWRITER]: persons.filter(p => p.role === PersonRole.SCREENWRITER).length,
      };
    }
    return totalCounts;
  };

  if (loading && persons.length === 0) {
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
        <button className={styles.primaryButton} onClick={handleAddNew}>
          <span className={styles.buttonIcon}>+</span>
          Add Person
        </button>
      </div>

      <div className={styles.searchContainer}>
        <div className={styles.searchWrapper}>
          <input
            type="text"
            placeholder="Search people by name..."
            value={searchQuery}
            onChange={handleSearchChange}
            className={styles.searchInput}
          />
          {loading && <div className={styles.searchSpinner}></div>}
        </div>
        {searchQuery && (
          <button className={styles.clearSearch} onClick={clearSearch}>
            Clear
          </button>
        )}
      </div>

      {pagination && (
        <div className={styles.resultsInfo}>
          Showing {persons.length} of {pagination.totalElements} people
          {debouncedSearch && ` for "${debouncedSearch}"`}
        </div>
      )}

      <PersonTabs activeTab={activeTab} onTabChange={handleTabChange} stats={getDisplayedCounts()} />

      <PersonList
        persons={persons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {hasMore && (
        <div className={styles.loadMoreContainer}>
          <button className={styles.loadMoreButton} onClick={loadMore} disabled={loading}>
            {loading ? (
              <>
                <div className={styles.loadingSpinnerSmall}></div>
                Loading...
              </>
            ) : (
              'Load More'
            )}
          </button>
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