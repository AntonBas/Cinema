export interface ApiResponse {
    success: boolean;
    message: string;
    data?: any;
    errors?: any;
}

export interface ErrorResponse {
    success: boolean;
    message: string;
    errors?: any;
}