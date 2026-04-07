export type PersonRole = 'ACTOR' | 'DIRECTOR' | 'SCREENWRITER';

export interface PersonRequest {
    name: string;
    role: PersonRole;
}

export interface QuickCreatePersonRequest {
    name: string;
    role: PersonRole;
}

export interface PersonResponse {
    id: number;
    name: string;
    role: PersonRole;
    movieCount?: number;
}

export interface PersonInfoResponse {
    id: number;
    name: string;
    role: PersonRole;
}

export const PersonRoleDisplay: Record<PersonRole, string> = {
    ACTOR: 'Actor',
    DIRECTOR: 'Director',
    SCREENWRITER: 'Screenwriter'
};