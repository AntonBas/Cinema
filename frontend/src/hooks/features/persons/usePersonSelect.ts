import { useState, useEffect, useRef, useCallback } from 'react';
import type { PersonResponse, PersonRole } from '@/types/person';
import { personApi } from '@/api/personApi';
import type { NotificationType } from '@/hooks/common/useNotification';

interface UsePersonSelectProps {
    selectedIds: number[];
    role: PersonRole;
    showNotification: (message: string, type?: NotificationType) => void;
}

export const usePersonSelect = ({
    selectedIds,
    role,
    showNotification
}: UsePersonSelectProps) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [options, setOptions] = useState<PersonResponse[]>([]);
    const [allSelectedPersons, setAllSelectedPersons] = useState<PersonResponse[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const abortControllerRef = useRef<AbortController | null>(null);

    const loadSelectedPersons = useCallback(async () => {
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
    }, [selectedIds, showNotification]);

    useEffect(() => {
        loadSelectedPersons();
    }, [loadSelectedPersons]);

    useEffect(() => {
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        const searchPersons = async () => {
            if (searchQuery.length < 2) {
                setOptions([]);
                setIsOpen(false);
                return;
            }

            setIsLoading(true);
            try {
                abortControllerRef.current = new AbortController();
                const result = await personApi.search({
                    query: searchQuery,
                    role,
                    page: 0,
                    size: 10
                });

                if (!abortControllerRef.current.signal.aborted) {
                    setOptions(result.content);
                    setIsOpen(true);
                }
            } catch (error) {
                if (error instanceof DOMException && error.name === 'AbortError') {
                    return;
                }
                console.error('Search failed:', error);
                showNotification('Failed to search persons', 'error');
            } finally {
                setIsLoading(false);
            }
        };

        const timeoutId = setTimeout(searchPersons, 300);
        return () => {
            clearTimeout(timeoutId);
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
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

            setAllSelectedPersons(prev => [...prev, newPerson]);
            setSearchQuery('');
            setOptions([]);
            setIsOpen(false);

            showNotification(`${newPerson.name} added and selected!`, 'success');

            return newPerson.id;
        } catch (error) {
            console.error('Failed to create person:', error);
            showNotification(error instanceof Error ? error.message : 'Failed to create person', 'error');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    const refreshSelected = () => {
        loadSelectedPersons();
    };

    const exactMatch = options.some(option =>
        option.name.toLowerCase() === searchQuery.toLowerCase()
    );

    const showAddOption = searchQuery.length >= 2 && !exactMatch && !isLoading;

    return {
        searchQuery,
        setSearchQuery,
        options,
        allSelectedPersons,
        isLoading,
        isOpen,
        setIsOpen,
        dropdownRef,
        showAddOption,
        handleAddNew,
        refreshSelected
    };
};