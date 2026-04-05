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

const SEARCH_DELAY = 300;
const MIN_SEARCH_LENGTH = 2;
const MAX_OPTIONS = 10;

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
    const searchTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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
                name: query,
                role: role,
                page: 0,
                size: MAX_OPTIONS
            });
            if (response?.data) {
                setLocalOptions(response.data.content || []);
                setIsOpen(true);
            } else {
                setLocalOptions([]);
            }
        } catch (error) {
            console.error('Error searching persons:', error);
            setLocalOptions([]);
        } finally {
            setIsSearching(false);
        }
    }, [role]);

    useEffect(() => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        if (searchQuery.length >= MIN_SEARCH_LENGTH) {
            searchTimeoutRef.current = setTimeout(() => {
                searchPersons(searchQuery);
            }, SEARCH_DELAY);
        } else {
            setLocalOptions([]);
            setIsOpen(false);
        }

        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [searchQuery, searchPersons]);

    const handleAddNewPerson = useCallback(async () => {
        if (!searchQuery.trim() || isCreating) return;

        setIsCreating(true);
        try {
            const response = await personApi.admin.quickCreate({ name: searchQuery.trim(), role });
            if (response?.data) {
                const newPerson = response.data;
                const newSelectedIds = [...selectedIds, newPerson.id];
                const updatedPersons = [...selectedPersons, newPerson];
                onChange(newSelectedIds, updatedPersons);
                setSearchQuery('');
                setIsOpen(false);
            }
        } catch (error) {
            console.error('Error creating person:', error);
        } finally {
            setIsCreating(false);
        }
    }, [searchQuery, role, selectedIds, selectedPersons, onChange, isCreating]);

    const handleSelectPerson = useCallback((personId: number) => {
        const isSelected = selectedIds.includes(personId);
        let newSelectedIds: number[];
        let updatedPersons: PersonResponse[];

        if (isSelected) {
            newSelectedIds = selectedIds.filter(id => id !== personId);
            updatedPersons = selectedPersons.filter(p => p.id !== personId);
        } else {
            newSelectedIds = [...selectedIds, personId];
            const selectedPerson = localOptions.find(p => p.id === personId);
            if (selectedPerson && !selectedPersons.some(p => p.id === personId)) {
                updatedPersons = [...selectedPersons, selectedPerson];
            } else {
                updatedPersons = [...selectedPersons];
            }
        }

        onChange(newSelectedIds, updatedPersons);
    }, [selectedIds, selectedPersons, localOptions, onChange]);

    const handleRemovePerson = useCallback((personId: number) => {
        const newSelectedIds = selectedIds.filter(id => id !== personId);
        const updatedPersons = selectedPersons.filter(p => p.id !== personId);
        onChange(newSelectedIds, updatedPersons);
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
                    aria-label={`Search ${roleLabel}s`}
                />
                {(isSearching || isCreating) && <div className={styles.spinner} aria-label="Loading">⏳</div>}
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
                                aria-label={`Remove ${person.name}`}
                            >
                                ×
                            </button>
                        </span>
                    ))}
                </div>
            )}

            {isOpen && (
                <div className={styles.dropdown} role="listbox">
                    {showAddOption && (
                        <button
                            type="button"
                            onClick={handleAddNewPerson}
                            className={styles.addOption}
                            disabled={isSearching || isCreating}
                            role="option"
                            aria-label={`Add and select "${searchQuery}" as new ${roleLabel}`}
                        >
                            {isCreating ? '⏳ Adding...' : `➕ Add & select "${searchQuery}" as new ${roleLabel}`}
                        </button>
                    )}

                    {localOptions.map(person => (
                        <label key={person.id} className={styles.option} role="option">
                            <input
                                type="checkbox"
                                checked={selectedIds.includes(person.id)}
                                onChange={() => handleSelectPerson(person.id)}
                                className={styles.checkbox}
                                aria-label={`Select ${person.name}`}
                            />
                            <span className={styles.checkmark} />
                            <span className={styles.optionLabel}>
                                {person.name}
                                {selectedIds.includes(person.id) && <span className={styles.alreadySelected}> (selected)</span>}
                            </span>
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