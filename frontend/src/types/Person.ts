export enum PersonRole {
    ACTOR = 'ACTOR',
    DIRECTOR = 'DIRECTOR',
    SCREENWRITER = 'SCREENWRITER'
}

export interface PersonDto {
    id: number;
    name: string;
    role: PersonRole;
}

export interface PersonRequest {
    name: string;
    role: PersonRole;
}

export interface QuickCreatePersonDto {
    name: string;
    role: PersonRole;
}