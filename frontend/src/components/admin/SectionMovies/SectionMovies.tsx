import React, { useState } from "react";
import { MovieTab } from "./MovieTab/MovieTab";
import { GenreTab } from "./GenreTab/GenreTab";
import { PersonTab } from "./PersonTab/PersonTab";
import styles from "./SectionMovies.module.css";
import clsx from "clsx";

type TabType = "movies" | "genres" | "persons";

export const SectionMovies: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>("movies");

  const tabs = [
    { id: "movies" as TabType, label: "Movies" },
    { id: "genres" as TabType, label: "Genres" },
    { id: "persons" as TabType, label: "People" },
  ];

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
        <div className={styles.navigation}>
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={clsx(
                styles.button,
                activeTab === tab.id && styles.active,
              )}
              onClick={() => setActiveTab(tab.id)}
            >
              <span className={styles.label}>{tab.label}</span>
              <div className={styles.indicator}></div>
            </button>
          ))}
        </div>

        <div className={styles.contentWrapper}>
          <div className={styles.content}>{renderTabContent()}</div>
        </div>
      </div>
    </div>
  );
};
