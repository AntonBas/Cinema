import React, { useState, useMemo, useRef, useEffect } from 'react';
import type { PersonResponse, PersonRole } from '@/types/person';
import { usePerson } from '@/hooks/features/persons/usePerson';
import styles from './PersonSelect.module.css';

interface PersonSelectProps {
    selectedIds: number[];
    selectedPersons?: PersonResponse[];
    onChange: (ids: number[]) => void;
    role: PersonRole;
    placeholder?: string;
    showNotification?: (message: string, type?: 'success' | 'error' | 'warning' | 'info') => void;
}

export const PersonSelect: React.FC<PersonSelectProps> = ({
    selectedIds,
    selectedPersons = [],
    onChange,
    role,
    placeholder = "Search or add new...",
    showNotification
}) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [localOptions, setLocalOptions] = useState<PersonResponse[]>([]);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const {
        getActors,
        getDirectors,
        getScreenwriters,
        quickCreate,
        loading
    } = usePerson();

    const handlePersonSearch = async (query: string, personRole: PersonRole) => {
        if (!query.trim()) return [];

        let result;
        if (personRole === 'ACTOR') {
            result = await getActors({ name: query, page: 0, size: 10 });
        } else if (personRole === 'DIRECTOR') {
            result = await getDirectors({ name: query, page: 0, size: 10 });
        } else {
            result = await getScreenwriters({ name: query, page: 0, size: 10 });
        }

        return result?.content || [];
    };

    const handleAddNewPerson = async (name: string, personRole: PersonRole) => {
        try {
            const newPerson = await quickCreate({ name, role: personRole });
            if (showNotification) {
                showNotification(`Person "${name}" created successfully`, 'success');
            }
            return newPerson.id;
        } catch (error) {
            if (showNotification) {
                showNotification('Failed to create person', 'error');
            }
            return null;
        }
    };

    const displayPersons = useMemo(() => {
        return selectedPersons.length > 0 ? selectedPersons : [];
    }, [selectedPersons]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (searchQuery.length >= 2) {
            const search = async () => {
                const results = await handlePersonSearch(searchQuery, role);
                setLocalOptions(results);
                setIsOpen(true);
            };
            search();
        } else {
            setLocalOptions([]);
            setIsOpen(false);
        }
    }, [searchQuery, role]);

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
        const newPersonId = await handleAddNewPerson(searchQuery, role);
        if (newPersonId) {
            const newSelectedIds = [...selectedIds, newPersonId];
            onChange(newSelectedIds);
            setSearchQuery('');
        }
    };

    const showAddOption = searchQuery.trim().length >= 2 &&
        !localOptions.some(person =>
            person.name.toLowerCase().includes(searchQuery.toLowerCase())
        );

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
                {loading && <div className={styles.spinner}>⏳</div>}
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
                            disabled={loading}
                        >
                            ➕ Add & select "{searchQuery}"
                        </button>
                    )}

                    {localOptions.map(person => (
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

                    {localOptions.length === 0 && searchQuery.length >= 2 && !showAddOption && (
                        <div className={styles.noResults}>No results found</div>
                    )}
                </div>
            )}
        </div>
    );
};