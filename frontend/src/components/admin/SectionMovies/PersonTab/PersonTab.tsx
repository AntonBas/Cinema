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
  const [allPersons, setAllPersons] = useState<PersonResponse[]>([]);
  const [currentPagination, setCurrentPagination] = useState<any>(null);

  const { persons, loading, error: searchError, searchPersons } = usePersonSearch();
  const { createPerson, updatePerson, deletePerson, loading: mutationLoading, error: mutationError } = usePersonMutation();
  const { notifications, showNotification, hideNotification } = useNotification();

  const loadPersons = useCallback(async (reset: boolean = false, pageOverride?: number) => {
    const page = reset ? 0 : (pageOverride ?? currentPage);
    const role = activeTab === 'ALL' ? undefined : activeTab;

    const result = await searchPersons({ query: searchQuery, role, page, size: 12 });
    setCurrentPage(page);

    if (result) {
      setCurrentPagination(result);
      if (reset) {
        setAllPersons(result.content);
      }
    }
  }, [activeTab, searchQuery, currentPage, searchPersons]);

  useEffect(() => {
    loadPersons(true);
  }, [activeTab, searchQuery]);

  useEffect(() => {
    if (persons.length > 0 && currentPage > 0) {
      setAllPersons(prev => [...prev, ...persons]);
    }
  }, [persons, currentPage]);

  useEffect(() => {
    if (searchError) showNotification(searchError, 'error');
  }, [searchError, showNotification]);

  useEffect(() => {
    if (mutationError) showNotification(mutationError, 'error');
  }, [mutationError, showNotification]);

  useEffect(() => {
    refreshAllCounts();
  }, []);

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

  const handleLoadMore = () => {
    if (!loading && currentPagination && currentPage < currentPagination.totalPages - 1) {
      loadPersons(false, currentPage + 1);
    }
  };

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  }, []);

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
    if (searchQuery) {
      const filtered = allPersons.filter(person =>
        activeTab === 'ALL' || person.role === activeTab
      );
      return {
        ALL: activeTab === 'ALL' ? filtered.length : allPersons.length,
        [PersonRole.ACTOR]: allPersons.filter(p => p.role === PersonRole.ACTOR).length,
        [PersonRole.DIRECTOR]: allPersons.filter(p => p.role === PersonRole.DIRECTOR).length,
        [PersonRole.SCREENWRITER]: allPersons.filter(p => p.role === PersonRole.SCREENWRITER).length,
      };
    }
    return totalCounts;
  };

  if (loading && allPersons.length === 0) {
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

      {currentPagination && (
        <div className={styles.resultsInfo}>
          Showing {allPersons.length} of {currentPagination.totalElements} people
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <PersonTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        stats={getDisplayedCounts()}
      />

      <PersonList
        persons={allPersons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {currentPagination && currentPagination.totalPages > 1 && currentPage < currentPagination.totalPages - 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={currentPagination.totalPages}
          totalElements={currentPagination.totalElements}
          pageSize={12}
          onLoadMore={handleLoadMore}
          variant="load-more"
          loading={loading}
          showInfo={false}
          className={styles.pagination}
        />
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