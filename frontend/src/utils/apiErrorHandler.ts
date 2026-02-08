import type { ApiError, ApiSubError } from '@/types/api';

export class ApiErrorException extends Error {
    public readonly timestamp: string;
    public readonly status: string;
    public readonly statusCode: number;
    public readonly debugMessage?: string;
    public readonly path?: string;
    public readonly subErrors?: ApiSubError[];

    constructor(apiError: ApiError) {
        super(apiError.message);
        this.name = 'ApiErrorException';
        this.timestamp = apiError.timestamp;
        this.status = apiError.status;
        this.statusCode = apiError.statusCode;
        this.debugMessage = apiError.debugMessage;
        this.path = apiError.path;
        this.subErrors = apiError.subErrors;
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