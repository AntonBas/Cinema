import React from "react";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import styles from "./PromotionFilters.module.css";

interface PromotionFiltersProps {
  onSearch?: (query: string) => void;
}

export const PromotionFilters: React.FC<PromotionFiltersProps> = ({
  onSearch,
}) => {
  return (
    <div className={styles.container}>
      {onSearch && (
        <SearchInput
          onSearch={onSearch}
          placeholder="Search promotions..."
          delay={300}
          className={styles.searchInput}
        />
      )}
    </div>
  );
};
