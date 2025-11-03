import React, { useState, useEffect, useRef } from 'react';
import { type PersonDto, PersonRole } from '@/types/person';
import { personApi } from '@/api/personApi';
import type { NotificationType } from '@/hooks/useNotification';
import styles from './PersonSelect.module.css';

interface PersonSelectProps {
    selectedIds: number[];
    onChange: (ids: number[]) => void;
    role: PersonRole;
    placeholder?: string;
    showNotification: (message: string, type?: NotificationType) => void;
}

export const PersonSelect: React.FC<PersonSelectProps> = ({
    selectedIds,
    onChange,
    role,
    placeholder = "Search or add new...",
    showNotification
}) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [options, setOptions] = useState<PersonDto[]>([]);
    const [allSelectedPersons, setAllSelectedPersons] = useState<PersonDto[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const loadSelectedPersons = async () => {
            if (selectedIds.length === 0) {
                setAllSelectedPersons([]);
                return;
            }

            try {
                const personsData = await Promise.all(
                    selectedIds.map(id => personApi.getById(id))
                );
                setAllSelectedPersons(personsData);
            } catch (error) {
                console.error('Failed to load selected persons:', error);
                showNotification('Failed to load selected persons', 'error');
            }
        };

        loadSelectedPersons();
    }, [selectedIds, showNotification]);

    useEffect(() => {
        const searchPersons = async () => {
            if (searchQuery.length < 2) {
                setOptions([]);
                setIsOpen(false);
                return;
            }

            setIsLoading(true);
            try {
                const result = await personApi.search({
                    query: searchQuery,
                    role,
                    page: 0,
                    size: 10
                });
                setOptions(result.content);
                setIsOpen(true);
            } catch (error) {
                console.error('Search failed:', error);
                showNotification('Failed to search persons', 'error');
            } finally {
                setIsLoading(false);
            }
        };

        const timeoutId = setTimeout(searchPersons, 300);
        return () => clearTimeout(timeoutId);
    }, [searchQuery, role, showNotification]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleAddNew = async () => {
        if (!searchQuery.trim()) return;

        setIsLoading(true);
        try {
            const newPerson = await personApi.quickCreate({
                name: searchQuery.trim(),
                role: role
            });

            const newSelectedIds = [...selectedIds, newPerson.id!];
            onChange(newSelectedIds);

            setAllSelectedPersons(prev => [...prev, newPerson]);

            setSearchQuery('');
            setOptions([]);
            setIsOpen(false);

            showNotification(`✅ ${newPerson.name} added and selected!`, 'success');

        } catch (error) {
            console.error('Failed to create person:', error);
            showNotification(error instanceof Error ? error.message : 'Failed to create person', 'error');
        } finally {
            setIsLoading(false);
        }
    };

    const handleSelectPerson = (personId: number) => {
        const newSelectedIds = selectedIds.includes(personId)
            ? selectedIds.filter(id => id !== personId)
            : [...selectedIds, personId];
        onChange(newSelectedIds);
    };

    const handleRemovePerson = (personId: number) => {
        const newSelectedIds = selectedIds.filter(id => id !== personId);
        onChange(newSelectedIds);
        setAllSelectedPersons(prev => prev.filter(person => person.id !== personId));
    };

    const exactMatch = options.some(option =>
        option.name.toLowerCase() === searchQuery.toLowerCase()
    );

    const showAddOption = searchQuery.length >= 2 && !exactMatch && !isLoading;

    const displaySelectedPersons = allSelectedPersons;

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

            {displaySelectedPersons.length > 0 && (
                <div className={styles.selectedItems}>
                    {displaySelectedPersons.map(person => (
                        <span key={person.id} className={styles.selectedTag}>
                            {person.name}
                            <button
                                type="button"
                                onClick={() => handleRemovePerson(person.id!)}
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
                            onClick={handleAddNew}
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
                                checked={selectedIds.includes(person.id!)}
                                onChange={() => handleSelectPerson(person.id!)}
                            />
                            <span className={styles.checkmark}></span>
                            <span className={styles.optionLabel}>
                                {person.name}
                                {selectedIds.includes(person.id!) && (
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