export interface ApiResponse {
    success: boolean;
    message: string;
}

export interface ErrorResponse {
    success: boolean;
    message: string;
    errors?: Array<{
        field: string;
        message: string;
    }>;
}