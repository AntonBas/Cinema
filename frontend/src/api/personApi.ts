import axios from 'axios';

export const API_URL = 'http://localhost:8080/api/persons';

export interface Person {
    id?: number;
    name: string;
    role: 'ACTOR' | 'DIRECTOR' | 'PRODUCER';
}

export const getAllPersons = () => axios.get<Person[]>(API_URL);
export const getPersonById = (id: number) => axios.get<Person>(`${API_URL}/${id}`);
export const createPerson = (person: Person) => axios.post<Person>(API_URL, person);
export const updatePerson = (id: number, person: Person) => axios.put<Person>(`${API_URL}/${id}`, person);
export const deletePerson = (id: number) => axios.delete(`${API_URL}/${id}`);