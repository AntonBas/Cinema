import React, { useCallback } from "react";
import { MovieTab } from "./MovieTab/MovieTab";
import { GenreTab } from "./GenreTab/GenreTab";
import { PersonTab } from "./PersonTab/PersonTab";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import styles from "./SectionMovies.module.css";

type TabType = "MOVIES" | "GENRES" | "PERSONS";

const SECTION_TABS: ReadonlyArray<TabItem<TabType>> = [
  { id: "MOVIES", label: "Movies" },
  { id: "GENRES", label: "Genres" },
  { id: "PERSONS", label: "People" },
];

const TAB_IDS = SECTION_TABS.map((tab) => tab.id);

export const SectionMovies: React.FC = () => {
  const { getParam, setParams } = useUrlParams();
  const activeTab = parseEnumParam(getParam("tab"), TAB_IDS, "MOVIES");

  const handleTabChange = useCallback(
    (tab: TabType) => {
      setParams({ tab: toUrlEnumValue(tab, "MOVIES") }, { reset: true });
    },
    [setParams],
  );

  const renderTabContent = () => {
    switch (activeTab) {
      case "MOVIES":
        return <MovieTab />;
      case "GENRES":
        return <GenreTab />;
      case "PERSONS":
        return <PersonTab />;
      default:
        return <MovieTab />;
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.tabsContainer}>
        <Tabs
          items={SECTION_TABS}
          activeId={activeTab}
          onChange={handleTabChange}
          ariaLabel="Movie catalog sections"
        />

        <div className={styles.contentWrapper}>
          <div className={styles.content}>{renderTabContent()}</div>
        </div>
      </div>
    </div>
  );
};
