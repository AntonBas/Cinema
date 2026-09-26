import React from "react";
import clsx from "clsx";
import styles from "./Tabs.module.css";

export interface TabItem<T extends string> {
  id: T;
  label: React.ReactNode;
  badge?: React.ReactNode;
}

export interface TabsProps<T extends string> {
  items: ReadonlyArray<TabItem<T>>;
  activeId: T;
  onChange: (id: T) => void;
  ariaLabel?: string;
  className?: string;
}

export const Tabs = <T extends string>({
  items,
  activeId,
  onChange,
  ariaLabel,
  className = "",
}: TabsProps<T>) => {
  const handleKeyDown = (
    event: React.KeyboardEvent<HTMLButtonElement>,
    index: number,
  ) => {
    const offset =
      event.key === "ArrowRight" ? 1 : event.key === "ArrowLeft" ? -1 : 0;
    if (!offset) return;
    event.preventDefault();
    const next = items[(index + offset + items.length) % items.length];
    onChange(next.id);
    const buttons =
      event.currentTarget.parentElement?.querySelectorAll<HTMLButtonElement>(
        '[role="tab"]',
      );
    buttons?.[(index + offset + items.length) % items.length]?.focus();
  };

  return (
    <div
      className={clsx(styles.tabs, className)}
      role="tablist"
      aria-label={ariaLabel}
    >
      {items.map((item, index) => {
        const isActive = item.id === activeId;
        return (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={isActive}
            tabIndex={isActive ? 0 : -1}
            className={clsx(styles.tab, isActive && styles.active)}
            onClick={() => onChange(item.id)}
            onKeyDown={(event) => handleKeyDown(event, index)}
          >
            <span>{item.label}</span>
            {item.badge !== undefined && (
              <span className={styles.badge}>{item.badge}</span>
            )}
          </button>
        );
      })}
    </div>
  );
};
