import React, { useState, useEffect } from 'react';

interface Person {
  id?: number;
  firstName: string;
  lastName: string;
  bio: string;
  type: 'ACTOR' | 'DIRECTOR' | 'SCREENWRITER';
}

export const PersonTab: React.FC = () => {
  const [persons, setPersons] = useState<Person[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<Person | null>(null);
  const [formData, setFormData] = useState<Person>({ 
    firstName: '', 
    lastName: '', 
    bio: '', 
    type: 'ACTOR' 
  });

  return (
    <div className="tab-content-inner">
      <div className="tab-header">
        <h2>People Management</h2>
        <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
          Add New Person
        </button>
      </div>
    </div>
  );
};