export type PersonRole = 'ACTOR' | 'DIRECTOR' | 'SCREENWRITER';

export const PersonRoleEnum = {
    ACTOR: 'ACTOR' as PersonRole,
    DIRECTOR: 'DIRECTOR' as PersonRole,
    SCREENWRITER: 'SCREENWRITER' as PersonRole,
} as const;

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