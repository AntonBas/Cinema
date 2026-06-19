export interface ApiError {
  timestamp: string;
  status: string;
  statusCode: number;
  message: string;
  debugMessage?: string;
  path?: string;
  subErrors?: ApiSubError[];
}

export interface ApiSubError {
  object?: string;
  field?: string;
  rejectedValue?: any;
  message?: string;
}

export interface ApiValidationError extends ApiSubError {
  object: string;
  field?: string;
  rejectedValue?: any;
  message: string;
}
