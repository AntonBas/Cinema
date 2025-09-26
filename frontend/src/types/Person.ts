export enum PersonRole {
    ACTOR = 'ACTOR',
    DIRECTOR = 'DIRECTOR',
    SCREENWRITER = 'SCREENWRITER'
}

export interface PersonDto {
    id?: number;
    name: string;
    role: PersonRole;
}

export interface PersonFormData {
    name: string;
    role: PersonRole;
}