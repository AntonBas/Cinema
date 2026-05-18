import React, { useState, useEffect, useCallback } from "react";
import { PersonTabs } from "./PersonTabs/PersonTabs";
import { PersonTable } from "./PersonTable/PersonTable";
import { PersonForm } from "./PersonForm/PersonForm";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { usePerson } from "@/hooks/features/persons/usePerson";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { usePagination } from "@/hooks/common/usePagination";
import type {
  PersonRequest,
  PersonRole,
  PersonListResponse,
} from "@/types/person";
import styles from "./PersonTab.module.css";

export const PersonTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonListResponse | null>(
    null,
  );
  const [personToDelete, setPersonToDelete] =
    useState<PersonListResponse | null>(null);
  const [activeTab, setActiveTab] = useState<PersonRole | "ALL">("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  const { params, setPage } = usePagination({ size: 12 });
  const { persons, pagination, loading, getAll, create, update, remove } =
    usePerson();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const currentPage = params.page ?? 0;
  const pageSize = params.size ?? 12;

  const loadPersons = useCallback(
    (page: number = currentPage) => {
      getAll({
        query: searchQuery || undefined,
        role: activeTab === "ALL" ? undefined : activeTab,
        page: page,
        size: pageSize,
      });
    },
    [searchQuery, activeTab, pageSize, getAll],
  );

  useEffect(() => {
    loadPersons(0);
  }, []);

  const handleSearch = useCallback(
    (query: string) => {
      setSearchQuery(query);
      setPage(0);
      getAll({
        query: query || undefined,
        role: activeTab === "ALL" ? undefined : activeTab,
        page: 0,
        size: pageSize,
      });
    },
    [activeTab, pageSize, getAll, setPage],
  );

  const handleTabChange = useCallback(
    (tab: PersonRole | "ALL") => {
      setActiveTab(tab);
      setPage(0);
      getAll({
        query: searchQuery || undefined,
        role: tab === "ALL" ? undefined : tab,
        page: 0,
        size: pageSize,
      });
    },
    [searchQuery, pageSize, getAll, setPage],
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setPage(page);
      getAll({
        query: searchQuery || undefined,
        role: activeTab === "ALL" ? undefined : activeTab,
        page: page,
        size: pageSize,
      });
    },
    [searchQuery, activeTab, pageSize, getAll, setPage],
  );

  const handleSubmit = useCallback(
    async (data: PersonRequest) => {
      if (editingPerson) {
        await update(editingPerson.id, data);
      } else {
        await create(data);
      }
      setIsModalOpen(false);
      setEditingPerson(null);
      loadPersons(currentPage);
    },
    [editingPerson, create, update, loadPersons, currentPage],
  );

  const handleDelete = useCallback(async () => {
    if (!personToDelete) return;
    await remove(personToDelete.id);
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
    if (persons.length === 1 && currentPage > 0) {
      setPage(currentPage - 1);
      getAll({
        query: searchQuery || undefined,
        role: activeTab === "ALL" ? undefined : activeTab,
        page: currentPage - 1,
        size: pageSize,
      });
    } else {
      loadPersons(currentPage);
    }
  }, [
    personToDelete,
    remove,
    persons.length,
    currentPage,
    searchQuery,
    activeTab,
    pageSize,
    getAll,
    setPage,
    loadPersons,
  ]);

  if (showDelayedLoading && !persons.length && !searchQuery) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading persons" />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h2>People Management</h2>
          <p className={styles.description}>
            Manage actors, directors and screenwriters
          </p>
        </div>
        <Button variant="primary" onClick={() => setIsModalOpen(true)}>
          Add Person
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search people by name..."
          delay={300}
        />
      </div>

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} people
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <PersonTabs activeTab={activeTab} onTabChange={handleTabChange} />

      <PersonTable
        persons={persons}
        onEdit={(person: PersonListResponse) => {
          setEditingPerson(person);
          setIsModalOpen(true);
        }}
        onDelete={(person: PersonListResponse) => {
          setPersonToDelete(person);
          setIsDeleteModalOpen(true);
        }}
      />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={pagination.number}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={handlePageChange}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

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
