import type { ApiError, ApiSubError } from '@/types/api';

export class ApiErrorException extends Error {
    public readonly timestamp: string;
    public readonly status: string;
    public readonly statusCode: number;
    public readonly debugMessage?: string;
    public readonly path?: string;
    public readonly subErrors?: ApiSubError[];

    constructor(apiError: ApiError) {
        const errorMessage = apiError.message || 'Request failed';
        super(errorMessage);

        this.name = 'ApiErrorException';
        this.timestamp = apiError.timestamp || new Date().toISOString();
        this.status = apiError.status || 'UNKNOWN_ERROR';

        if (apiError.statusCode) {
            this.statusCode = apiError.statusCode;
        } else if (apiError.status) {
            this.statusCode = this.getStatusCodeFromStatus(apiError.status);
        } else {
            this.statusCode = 500;
        }

        this.debugMessage = apiError.debugMessage;
        this.path = apiError.path;
        this.subErrors = apiError.subErrors;
    }

    private getStatusCodeFromStatus(status: string): number {
        const statusMap: Record<string, number> = {
            'CONFLICT': 409,
            'BAD_REQUEST': 400,
            'NOT_FOUND': 404,
            'UNAUTHORIZED': 401,
            'FORBIDDEN': 403,
            'INTERNAL_SERVER_ERROR': 500,
            'OK': 200,
            'CREATED': 201
        };

        if (!isNaN(Number(status))) {
            return Number(status);
        }

        return statusMap[status] || 500;
    }

    isNotFound(): boolean {
        return this.statusCode === 404;
    }

    isConflict(): boolean {
        return this.statusCode === 409;
    }

    isValidationError(): boolean {
        return this.statusCode === 400 && !!this.subErrors && this.subErrors.length > 0;
    }

    isUnauthorized(): boolean {
        return this.statusCode === 401;
    }

    isForbidden(): boolean {
        return this.statusCode === 403;
    }

    isServerError(): boolean {
        return this.statusCode >= 500;
    }

    isClientError(): boolean {
        return this.statusCode >= 400 && this.statusCode < 500;
    }

    getValidationErrors(): Record<string, string> {
        if (!this.subErrors) return {};

        const errors: Record<string, string> = {};
        this.subErrors.forEach(error => {
            if (error.field && error.message) {
                const fieldName = this.normalizeFieldName(error.field);
                errors[fieldName] = error.message;
            }
        });
        return errors;
    }

    private normalizeFieldName(field: string): string {
        return field.replace(/\[(\w+)\]/g, '.$1');
    }

    getFirstValidationError(): string | null {
        const errors = this.getValidationErrors();
        const firstError = Object.values(errors)[0];
        return firstError || null;
    }

    toString(): string {
        return `[${this.statusCode}] ${this.message}`;
    }
}

export const handleApiError = async (response: Response): Promise<never> => {
    const errorData: ApiError = await response.json();
    throw new ApiErrorException(errorData);
};

export const isApiErrorException = (error: unknown): error is ApiErrorException => {
    return error instanceof ApiErrorException;
};