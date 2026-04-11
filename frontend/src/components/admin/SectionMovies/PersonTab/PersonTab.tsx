import React, { useState, useEffect, useCallback } from 'react';
import { PersonTabs } from './PersonTabs/PersonTabs';
import { PersonList } from './PersonList/PersonList';
import { PersonForm } from './PersonForm/PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { usePerson } from '@/hooks/features/persons/usePerson';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import type { PersonRequest, PersonRole, PersonListResponse } from '@/types/person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonListResponse | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonListResponse | null>(null);
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [search, setSearch] = useState('');

  const {
    persons,
    loading,
    getAll,
    create,
    update,
    remove,
  } = usePerson();

  const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

  useEffect(() => {
    getAll({
      query: search || undefined,
      role: activeTab === 'ALL' ? undefined : activeTab,
    });
  }, [search, activeTab, getAll]);

  const handleSearch = useCallback((query: string) => {
    setSearch(query);
  }, []);

  const handleTabChange = useCallback((tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
  }, []);

  const handleSubmit = useCallback(async (data: PersonRequest) => {
    if (editingPerson) {
      await update(editingPerson.id, data);
    } else {
      await create(data);
    }
    setIsModalOpen(false);
    setEditingPerson(null);
    getAll({
      query: search || undefined,
      role: activeTab === 'ALL' ? undefined : activeTab,
    });
  }, [editingPerson, create, update, getAll, search, activeTab]);

  const handleDelete = useCallback(async () => {
    if (!personToDelete) return;

    await remove(personToDelete.id);
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
    getAll({
      query: search || undefined,
      role: activeTab === 'ALL' ? undefined : activeTab,
    });
  }, [personToDelete, remove, getAll, search, activeTab]);

  if (showDelayedLoading && !persons.length && !search) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading persons" />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2>People Management</h2>
        <Button variant="primary" onClick={() => setIsModalOpen(true)}>
          Add Person
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput onSearch={handleSearch} placeholder="Search people by name..." delay={300} />
      </div>

      <PersonTabs activeTab={activeTab} onTabChange={handleTabChange} />

      <PersonList
        persons={persons}
        activeTab={activeTab}
        onEdit={(person) => {
          setEditingPerson(person);
          setIsModalOpen(true);
        }}
        onDelete={(person) => {
          setPersonToDelete(person);
          setIsDeleteModalOpen(true);
        }}
        onAddPerson={() => setIsModalOpen(true)}
      />

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={() => {
            setIsModalOpen(false);
            setEditingPerson(null);
          }}
          isLoading={loading}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDelete}
        onCancel={() => {
          setIsDeleteModalOpen(false);
          setPersonToDelete(null);
        }}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={loading}
      />
    </div>
  );
};