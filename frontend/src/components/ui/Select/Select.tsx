import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import styles from './Select.module.css';

export interface SelectOption {
    value: string | number;
    label: string;
    disabled?: boolean;
}

export interface SelectProps {
    options: SelectOption[];
    value?: string | number;
    onChange: (value: string | number) => void;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    required?: boolean;
}

export const Select: React.FC<SelectProps> = ({
    options,
    value,
    onChange,
    placeholder = "Select an option",
    disabled = false,
    className = ''
}) => {
    const [isOpen, setIsOpen] = useState(false);
    const [position, setPosition] = useState({ top: 0, left: 0, width: 0 });
    const [openUpward, setOpenUpward] = useState(false);
    const selectRef = useRef<HTMLDivElement>(null);
    const triggerRef = useRef<HTMLDivElement>(null);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const selectedOption = options.find(option => option.value === value);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (isOpen && triggerRef.current) {
            const rect = triggerRef.current.getBoundingClientRect();
            const viewportHeight = window.innerHeight;
            const dropdownHeight = 200;

            const spaceBelow = viewportHeight - rect.bottom;
            const spaceAbove = rect.top;

            const upward = spaceBelow < dropdownHeight && spaceAbove > spaceBelow;
            setOpenUpward(upward);

            setPosition({
                top: upward ? rect.top + window.scrollY - dropdownHeight : rect.bottom + window.scrollY,
                left: rect.left + window.scrollX,
                width: rect.width
            });
        }
    }, [isOpen]);

    useEffect(() => {
        const handleScroll = () => {
            if (isOpen) {
                setIsOpen(false);
            }
        };

        window.addEventListener('scroll', handleScroll, true);
        return () => window.removeEventListener('scroll', handleScroll, true);
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && dropdownRef.current) {
            const handleDropdownScroll = (e: Event) => {
                e.stopPropagation();
            };

            dropdownRef.current.addEventListener('wheel', handleDropdownScroll);
            return () => dropdownRef.current?.removeEventListener('wheel', handleDropdownScroll);
        }
    }, [isOpen]);

    const handleSelect = (option: SelectOption) => {
        if (option.disabled) return;
        onChange(option.value);
        setIsOpen(false);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (disabled) return;

        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            setIsOpen(!isOpen);
        } else if (e.key === 'Escape') {
            setIsOpen(false);
        }
    };

    return (
        <div
            className={`${styles.selectContainer} ${className} ${disabled ? styles.disabled : ''}`}
            ref={selectRef}
        >
            <div
                ref={triggerRef}
                className={`${styles.selectTrigger} ${isOpen ? styles.open : ''}`}
                onClick={() => !disabled && setIsOpen(!isOpen)}
                onKeyDown={handleKeyDown}
                tabIndex={disabled ? -1 : 0}
                role="combobox"
                aria-haspopup="listbox"
                aria-expanded={isOpen}
            >
                <span className={selectedOption ? styles.selectedValue : styles.placeholder}>
                    {selectedOption ? selectedOption.label : placeholder}
                </span>
                <span className={styles.arrow}>▾</span>
            </div>

            {isOpen && createPortal(
                <div
                    ref={dropdownRef}
                    className={`${styles.dropdownPortal} ${openUpward ? styles.dropdownPortalUpward : styles.dropdownPortalDownward}`}
                    style={{
                        top: `${position.top}px`,
                        left: `${position.left}px`,
                        width: `${position.width}px`
                    }}
                    role="listbox"
                >
                    {options.map((option) => (
                        <div
                            key={option.value}
                            className={`${styles.option} ${option.value === value ? styles.selected : ''} ${option.disabled ? styles.disabledOption : ''}`}
                            onClick={() => handleSelect(option)}
                            role="option"
                            aria-selected={option.value === value}
                        >
                            {option.label}
                        </div>
                    ))}
                </div>,
                document.body
            )}
        </div>
    );
};