import React, { useEffect, useState } from 'react';
import { createPerson, updatePerson } from '../api/personApi';
import type { Person } from '../api/personApi';


interface Props {
  person: Person | null;
  onSuccess: () => void;
}

const PersonForm: React.FC<Props> = ({ person, onSuccess }) => {
  const [name, setName] = useState('');
  const [role, setRole] = useState<'ACTOR' | 'DIRECTOR' | 'PRODUCER'>('ACTOR');

  useEffect(() => {
    if (person) {
      setName(person.name);
      setRole(person.role);
    }
  }, [person]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newPerson: Person = { name, role };
    if (person?.id) {
      updatePerson(person.id, newPerson).then(() => onSuccess());
    } else {
      createPerson(newPerson).then(() => onSuccess());
    }
    setName('');
    setRole('ACTOR');
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={name} onChange={e => setName(e.target.value)} placeholder="Name" required />
      <select value={role} onChange={e => setRole(e.target.value as Person['role'])}>
        <option value="ACTOR">Actor</option>
        <option value="DIRECTOR">Director</option>
        <option value="PRODUCER">Producer</option>
      </select>
      <button type="submit">{person ? 'Update' : 'Create'}</button>
    </form>
  );
};

export default PersonForm;
