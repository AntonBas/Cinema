import React, { useState, useMemo, useRef, useEffect, useCallback } from 'react';
import type { PersonResponse, PersonRole } from '@/types/person';
import { personApi } from '@/api/personApi';
import styles from './PersonSelect.module.css';

interface PersonSelectProps {
    selectedIds: number[];
    selectedPersons?: PersonResponse[];
    onChange: (ids: number[], persons?: PersonResponse[]) => void;
    role: PersonRole;
    placeholder?: string;
}

const MIN_SEARCH_LENGTH = 2;

export const PersonSelect: React.FC<PersonSelectProps> = ({
    selectedIds,
    selectedPersons = [],
    onChange,
    role,
    placeholder = "Search or add new..."
}) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [localOptions, setLocalOptions] = useState<PersonResponse[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const [isCreating, setIsCreating] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const timeoutRef = useRef<number | null>(null);

    const displayPersons = useMemo(() =>
        selectedPersons.filter(person => selectedIds.includes(person.id)),
        [selectedPersons, selectedIds]
    );

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
                setSearchQuery('');
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const searchPersons = useCallback(async (query: string) => {
        if (!query.trim() || query.length < MIN_SEARCH_LENGTH) {
            setLocalOptions([]);
            return;
        }

        setIsSearching(true);
        try {
            const response = await personApi.admin.getAll({
                query,
                role,
            });
            setLocalOptions(response?.data?.content || []);
            setIsOpen(true);
        } catch {
            setLocalOptions([]);
        } finally {
            setIsSearching(false);
        }
    }, [role]);

    useEffect(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        if (searchQuery.length >= MIN_SEARCH_LENGTH) {
            timeoutRef.current = window.setTimeout(() => {
                searchPersons(searchQuery);
            }, 300);
        } else {
            setLocalOptions([]);
            setIsOpen(false);
        }

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [searchQuery, searchPersons]);

    const handleAddNewPerson = useCallback(async () => {
        if (!searchQuery.trim() || isCreating) return;

        setIsCreating(true);
        try {
            const response = await personApi.admin.create({ name: searchQuery.trim(), role });
            const newPerson = response?.data;
            if (newPerson) {
                onChange([...selectedIds, newPerson.id], [...selectedPersons, newPerson]);
                setSearchQuery('');
                setIsOpen(false);
            }
        } catch {
        } finally {
            setIsCreating(false);
        }
    }, [searchQuery, role, selectedIds, selectedPersons, onChange, isCreating]);

    const handleSelectPerson = useCallback((personId: number) => {
        const isSelected = selectedIds.includes(personId);

        if (isSelected) {
            onChange(
                selectedIds.filter(id => id !== personId),
                selectedPersons.filter(p => p.id !== personId)
            );
        } else {
            const selectedPerson = localOptions.find(p => p.id === personId);
            const updatedPersons = selectedPerson && !selectedPersons.some(p => p.id === personId)
                ? [...selectedPersons, selectedPerson]
                : [...selectedPersons];
            onChange([...selectedIds, personId], updatedPersons);
        }
    }, [selectedIds, selectedPersons, localOptions, onChange]);

    const handleRemovePerson = useCallback((personId: number) => {
        onChange(
            selectedIds.filter(id => id !== personId),
            selectedPersons.filter(p => p.id !== personId)
        );
    }, [selectedIds, selectedPersons, onChange]);

    const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(e.target.value);
        if (!e.target.value) {
            setIsOpen(false);
            setLocalOptions([]);
        }
    }, []);

    const handleFocus = useCallback(() => {
        if (searchQuery.length >= MIN_SEARCH_LENGTH && localOptions.length > 0) {
            setIsOpen(true);
        }
    }, [searchQuery.length, localOptions.length]);

    const showAddOption = useMemo(() => {
        if (searchQuery.trim().length < MIN_SEARCH_LENGTH) return false;
        return !localOptions.some(person =>
            person.name.toLowerCase().includes(searchQuery.toLowerCase())
        );
    }, [searchQuery, localOptions]);

    const roleLabel = useMemo(() => {
        switch (role) {
            case 'ACTOR': return 'actor';
            case 'DIRECTOR': return 'director';
            case 'SCREENWRITER': return 'screenwriter';
            default: return 'person';
        }
    }, [role]);

    return (
        <div className={styles.container} ref={dropdownRef}>
            <div className={styles.searchContainer}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={handleSearchChange}
                    onFocus={handleFocus}
                    placeholder={placeholder}
                    className={styles.searchInput}
                />
                {(isSearching || isCreating) && <div className={styles.spinner}>⏳</div>}
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
                            onClick={handleAddNewPerson}
                            className={styles.addOption}
                            disabled={isSearching || isCreating}
                        >
                            {isCreating ? 'Adding...' : `+ Add & select "${searchQuery}" as new ${roleLabel}`}
                        </button>
                    )}

                    {localOptions.map(person => (
                        <label key={person.id} className={styles.option}>
                            <input
                                type="checkbox"
                                checked={selectedIds.includes(person.id)}
                                onChange={() => handleSelectPerson(person.id)}
                                className={styles.checkbox}
                            />
                            <span className={styles.checkmark} />
                            <span className={styles.optionLabel}>{person.name}</span>
                        </label>
                    ))}

                    {localOptions.length === 0 && searchQuery.length >= MIN_SEARCH_LENGTH && !showAddOption && (
                        <div className={styles.noResults}>No {roleLabel}s found</div>
                    )}
                </div>
            )}
        </div>
    );
};