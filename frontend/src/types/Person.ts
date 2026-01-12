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
}

export const PersonRoleDisplay: Record<PersonRole, string> = {
    ACTOR: 'Actor',
    DIRECTOR: 'Director',
    SCREENWRITER: 'Screenwriter'
};

export const PersonRoleColors: Record<PersonRole, string> = {
    ACTOR: 'primary',
    DIRECTOR: 'success',
    SCREENWRITER: 'info'
};

export interface PersonsListResponse {
    content: PersonResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface PersonSearchRequest {
    name?: string;
    role?: PersonRole;
    page?: number;
    size?: number;
}