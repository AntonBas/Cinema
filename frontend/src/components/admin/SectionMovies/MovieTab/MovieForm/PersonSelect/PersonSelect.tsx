import React, { useMemo } from 'react';
import type { PersonResponse, PersonRole } from '@/types/person';
import type { NotificationType } from '@/hooks/common/useNotification';
import { usePersonSelect } from '@/hooks/features/persons/usePersonSelect';
import styles from './PersonSelect.module.css';

interface PersonSelectProps {
    selectedIds: number[];
    selectedPersons?: PersonResponse[];
    onChange: (ids: number[]) => void;
    role: PersonRole;
    placeholder?: string;
    showNotification: (message: string, type?: NotificationType) => void;
}

export const PersonSelect: React.FC<PersonSelectProps> = ({
    selectedIds,
    selectedPersons = [],
    onChange,
    role,
    placeholder = "Search or add new...",
    showNotification
}) => {
    const {
        searchQuery,
        setSearchQuery,
        options,
        allSelectedPersons,
        isLoading,
        isOpen,
        setIsOpen,
        dropdownRef,
        showAddOption,
        handleAddNew
    } = usePersonSelect({
        selectedIds,
        role,
        showNotification
    });

    const displayPersons = useMemo(() => {
        return selectedPersons.length > 0 ? selectedPersons : allSelectedPersons;
    }, [selectedPersons, allSelectedPersons]);

    const handleSelectPerson = (personId: number) => {
        const newSelectedIds = selectedIds.includes(personId)
            ? selectedIds.filter(id => id !== personId)
            : [...selectedIds, personId];
        onChange(newSelectedIds);
    };

    const handleRemovePerson = (personId: number) => {
        const newSelectedIds = selectedIds.filter(id => id !== personId);
        onChange(newSelectedIds);
    };

    const handleAddAndSelect = async () => {
        const newPersonId = await handleAddNew();
        if (newPersonId) {
            const newSelectedIds = [...selectedIds, newPersonId];
            onChange(newSelectedIds);
        }
    };

    return (
        <div className={styles.container} ref={dropdownRef}>
            <div className={styles.searchContainer}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onFocus={() => searchQuery.length >= 2 && setIsOpen(true)}
                    placeholder={placeholder}
                    className={styles.searchInput}
                />
                {isLoading && <div className={styles.spinner}>⏳</div>}
            </div>

            {displayPersons.length > 0 && (
                <div className={styles.selectedItems}>
                    {displayPersons.map(person => (
                        <span key={person.id} className={styles.selectedTag}>
                            {person.name}
                            <button
                                type="button"
                                onClick={() => handleRemovePerson(person.id)}
                                className={styles.removeTag}
                                title={`Remove ${person.name}`}
                            >
                                ×
                            </button>
                        </span>
                    ))}
                </div>
            )}

            {isOpen && (
                <div className={styles.dropdown}>
                    {showAddOption && (
                        <button
                            type="button"
                            onClick={handleAddAndSelect}
                            className={styles.addOption}
                            disabled={isLoading}
                        >
                            ➕ Add & select "{searchQuery}"
                        </button>
                    )}

                    {options.map(person => (
                        <label key={person.id} className={styles.option}>
                            <input
                                type="checkbox"
                                checked={selectedIds.includes(person.id)}
                                onChange={() => handleSelectPerson(person.id)}
                            />
                            <span className={styles.checkmark}></span>
                            <span className={styles.optionLabel}>
                                {person.name}
                                {selectedIds.includes(person.id) && (
                                    <span className={styles.alreadySelected}>(selected)</span>
                                )}
                            </span>
                        </label>
                    ))}

                    {options.length === 0 && searchQuery.length >= 2 && !showAddOption && (
                        <div className={styles.noResults}>No results found</div>
                    )}
                </div>
            )}
        </div>
    );
};