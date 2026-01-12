export type TicketTypeCategory = 'STANDARD' | 'DISCOUNT' | 'VIP' | 'FAMILY';

export interface TicketTypeCreateRequest {
    code: string;
    displayName: string;
    priceMultiplier: string;
    minAge?: number | null;
    maxAge?: number | null;
    requiresDocument?: boolean;
    documentType?: string | null;
    active?: boolean;
    category: TicketTypeCategory;
}

export interface TicketTypeUpdateRequest {
    displayName?: string;
    priceModifier?: string;
    minAge?: number | null;
    maxAge?: number | null;
    requiresDocument?: boolean;
    documentType?: string | null;
    active?: boolean;
    category?: TicketTypeCategory;
}

export interface TicketTypeResponse {
    id: number;
    code: string;
    displayName: string;
    priceMultiplier: string;
    minAge: number | null;
    maxAge: number | null;
    requiresDocument: boolean;
    documentType: string | null;
    active: boolean;
    category: TicketTypeCategory;
    createdAt: string;
    updatedAt: string;
}

export interface TicketTypeSimpleResponse {
    id: number;
    code: string;
    displayName: string;
    priceMultiplier: string;
    active: boolean;
}

export const TicketTypeCategoryDisplay: Record<TicketTypeCategory, string> = {
    STANDARD: 'Standard',
    DISCOUNT: 'Discount',
    VIP: 'VIP',
    FAMILY: 'Family'
};

export interface TicketTypesListResponse {
    content: TicketTypeResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}