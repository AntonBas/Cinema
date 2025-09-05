import React, { useEffect, useState } from 'react';
import { getAllPersons, deletePerson } from '../api/personApi';
import type { Person } from '../api/personApi';
import PersonForm from './PersonForm';

const PersonList: React.FC = () => {
  const [persons, setPersons] = useState<Person[]>([]);
  const [editingPerson, setEditingPerson] = useState<Person | null>(null);

  const fetchPersons = () => {
    getAllPersons().then(res => setPersons(res.data));
  };

  useEffect(() => {
    fetchPersons();
  }, []);

  const handleDelete = (id?: number) => {
    if (id) deletePerson(id).then(fetchPersons);
  };

  return (
    <div>
      <h1>Persons</h1>
      <PersonForm person={editingPerson} onSuccess={fetchPersons} />
      <ul>
        {persons.map(p => (
          <li key={p.id}>
            {p.name} ({p.role})
            <button onClick={() => setEditingPerson(p)}>Edit</button>
            <button onClick={() => handleDelete(p.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PersonList;
