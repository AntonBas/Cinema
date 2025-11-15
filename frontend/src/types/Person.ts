export enum PersonRole {
    ACTOR = 'ACTOR',
    DIRECTOR = 'DIRECTOR',
    SCREENWRITER = 'SCREENWRITER'
}

export interface PersonResponse {
    id: number;
    name: string;
    role: PersonRole;
    type?: string;
}

export interface PersonRequest {
    name: string;
    role: PersonRole;
}

export interface QuickCreatePersonRequest {
    name: string;
    role: PersonRole;
}