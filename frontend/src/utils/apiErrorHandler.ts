export const handleApiError = async (response: Response): Promise<never> => {
    let errorMessage = 'Request failed';

    try {
        const errorData = await response.json();

        if (errorData.message) {
            errorMessage = errorData.message;
        }
        else if (response.status === 409) {
            errorMessage = 'Resource already exists';
        } else if (response.status === 400) {
            errorMessage = 'Invalid request data';
        } else if (response.status === 401) {
            errorMessage = 'Unauthorized access';
        } else if (response.status === 403) {
            errorMessage = 'Access forbidden';
        } else if (response.status === 404) {
            errorMessage = 'Resource not found';
        } else {
            errorMessage = `Request failed with status ${response.status}`;
        }
    } catch {
        if (response.status === 409) {
            errorMessage = 'Resource already exists';
        } else {
            errorMessage = `Request failed with status ${response.status}`;
        }
    }

    throw new Error(errorMessage);
};