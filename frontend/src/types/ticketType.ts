export type TicketTypeCategory = 'STANDARD' | 'CHILD' | 'STUDENT' | 'DISABLED' | 'MILITARY' | 'SENIOR' | 'SPECIAL';

export interface TicketTypeCreateRequest {
    displayName: string;
    priceMultiplier: string;
    minAge?: number;
    maxAge?: number;
    requiresDocument: boolean;
    documentType?: string;
    active: boolean;
    category: TicketTypeCategory;
}

export interface TicketTypeUpdateRequest {
    displayName?: string;
    priceMultiplier?: string;
    minAge?: number;
    maxAge?: number;
    requiresDocument?: boolean;
    documentType?: string;
    active?: boolean;
    category?: TicketTypeCategory;
}

export interface TicketTypeAdminResponse {
    id: number;
    displayName: string;
    priceMultiplier: string;
    minAge?: number;
    maxAge?: number;
    requiresDocument: boolean;
    documentType?: string;
    active: boolean;
    category: TicketTypeCategory;
}

export const TicketTypeCategoryDisplay: Record<TicketTypeCategory, string> = {
    STANDARD: 'Standard',
    CHILD: 'Child',
    STUDENT: 'Student',
    DISABLED: 'Disabled',
    MILITARY: 'Military',
    SENIOR: 'Senior',
    SPECIAL: 'Special'
};