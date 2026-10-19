import React, { useState } from "react";
import { ChevronDown, SlidersHorizontal } from "lucide-react";
import styles from "./FilterPanel.module.css";

interface FilterPanelProps {
  activeCount?: number;
  children: React.ReactNode;
}

export const FilterPanel: React.FC<FilterPanelProps> = ({
  activeCount = 0,
  children,
}) => {
  const [open, setOpen] = useState(false);

  return (
    <div className={styles.panel}>
      <button
        type="button"
        className={styles.toggle}
        onClick={() => setOpen((current) => !current)}
        aria-expanded={open}
      >
        <SlidersHorizontal size={16} />
        <span>Filters</span>
        {activeCount > 0 && <span className={styles.count}>{activeCount}</span>}
        <ChevronDown
          size={16}
          className={`${styles.chevron} ${open ? styles.chevronOpen : ""}`}
        />
      </button>
      <div className={`${styles.body} ${open ? styles.bodyOpen : ""}`}>
        {children}
      </div>
    </div>
  );
};
