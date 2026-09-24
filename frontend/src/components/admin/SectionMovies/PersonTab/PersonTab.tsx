import React, { useState, useEffect, useCallback } from "react";
import { PersonTable } from "./PersonTable/PersonTable";
import { PersonForm } from "./PersonForm/PersonForm";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { usePerson } from "@/hooks/features/person/usePerson";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { DEFAULT_PAGE_SIZE } from "@/utils/paginationUtils";
import type {
  PersonRequest,
  PersonRole,
  PersonListResponse,
} from "@/types/person";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./PersonTab.module.css";

const PERSON_TABS: ReadonlyArray<TabItem<PersonRole | "ALL">> = [
  { id: "ALL", label: "All People" },
  { id: "ACTOR", label: "Actors" },
  { id: "DIRECTOR", label: "Directors" },
  { id: "SCREENWRITER", label: "Screenwriters" },
];

const PERSON_TAB_IDS = PERSON_TABS.map((tab) => tab.id);

export const PersonTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonListResponse | null>(
    null,
  );
  const [personToDelete, setPersonToDelete] =
    useState<PersonListResponse | null>(null);
  const { page, query, getParam, setParams, setPage, setSearch } =
    useUrlParams();
  const activeTab = parseEnumParam(getParam("role"), PERSON_TAB_IDS, "ALL");
  const { persons, pagination, loading, getAll, create, update, remove } =
    usePerson();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadPersons = useCallback(() => {
    getAll({
      query,
      role: activeTab === "ALL" ? undefined : activeTab,
      page,
      size: DEFAULT_PAGE_SIZE,
    });
  }, [query, activeTab, page, getAll]);

  useEffect(() => {
    loadPersons();
  }, [loadPersons]);

  const handleTabChange = useCallback(
    (tab: PersonRole | "ALL") => {
      setParams({ role: toUrlEnumValue(tab, "ALL"), page: undefined });
    },
    [setParams],
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
      loadPersons();
    },
    [editingPerson, create, update, loadPersons],
  );

  const handleDelete = useCallback(async () => {
    if (!personToDelete) return;
    await remove(personToDelete.id);
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
    if (persons.length === 1 && page > 0) {
      setPage(page - 1);
    } else {
      loadPersons();
    }
  }, [personToDelete, remove, persons.length, page, setPage, loadPersons]);

  if (showDelayedLoading && !persons.length && !query) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading people..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h2>People</h2>
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
          onSearch={setSearch}
          value={query}
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
          {query && ` for "${query}"`}
        </div>
      )}

      <Tabs
        items={PERSON_TABS}
        activeId={activeTab}
        onChange={handleTabChange}
        ariaLabel="Person categories"
      />

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
            onPageChange={setPage}
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
