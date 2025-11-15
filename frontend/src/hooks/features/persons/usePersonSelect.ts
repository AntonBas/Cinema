import { useState, useEffect, useRef } from 'react';
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
        handleAddNew
    };
};