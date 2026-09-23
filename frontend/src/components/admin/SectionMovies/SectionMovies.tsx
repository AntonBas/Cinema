import React, { useState } from "react";
import { MovieTab } from "./MovieTab/MovieTab";
import { GenreTab } from "./GenreTab/GenreTab";
import { PersonTab } from "./PersonTab/PersonTab";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./SectionMovies.module.css";

type TabType = "movies" | "genres" | "persons";

const SECTION_TABS: ReadonlyArray<TabItem<TabType>> = [
  { id: "movies", label: "Movies" },
  { id: "genres", label: "Genres" },
  { id: "persons", label: "People" },
];

export const SectionMovies: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>("movies");

  const renderTabContent = () => {
    switch (activeTab) {
      case "movies":
        return <MovieTab />;
      case "genres":
        return <GenreTab />;
      case "persons":
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
          onChange={setActiveTab}
          ariaLabel="Movie catalog sections"
        />

        <div className={styles.contentWrapper}>
          <div className={styles.content}>{renderTabContent()}</div>
        </div>
      </div>
    </div>
  );
};
