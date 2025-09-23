import React, { useState } from 'react';
import { MovieTab } from './MovieTab.tsx';
import { GenreTab } from './GenreTab.tsx';
import { PersonTab } from './PersonTab.tsx';
import './MoviesSection.css';

type TabType = 'movies' | 'genres' | 'persons';

export const MoviesSection: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('movies');

  const tabs = [
    { id: 'movies' as TabType, label: '🎬 Movies', icon: '🎬' },
    { id: 'genres' as TabType, label: '📚 Genres', icon: '📚' },
    { id: 'persons' as TabType, label: '👥 People', icon: '👥' }
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'movies':
        return <MovieTab />;
      case 'genres':
        return <GenreTab />;
      case 'persons':
        return <PersonTab />;
      default:
        return <MovieTab />;
    }
  };

  return (
    <div className="movies-section">
      <div className="section-header">
        <h1>Content Management</h1>
      </div>

      <div className="tabs-container">
        <div className="tabs-header">
          {tabs.map(tab => (
            <button
              key={tab.id}
              className={`tab-button ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              <span className="tab-icon">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>
        
        <div className="tab-content">
          {renderTabContent()}
        </div>
      </div>
    </div>
  );
};