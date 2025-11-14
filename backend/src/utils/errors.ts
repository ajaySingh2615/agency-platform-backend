export class AppError extends Error {
  public statusCode: number;
  public isOperational: boolean;

  constructor(message: string, statusCode: number = 500) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = true; // Distinguishes from programming errors

    // Maintains proper stack trace
    Error.captureStackTrace(this, this.constructor);
  }
}

// 400 Bad Request - Client sent invalid data
export class BadRequestError extends AppError {
  constructor(message: string = "Bad Request") {
    super(message, 400);
  }
}

// 401 Unauthorized - Authentication failed
export class UnauthorizedError extends AppError {
  constructor(message: string = "Unauthorized") {
    super(message, 401);
  }
}

// 403 Forbidden - User doesn't have permission
export class ForbiddenError extends AppError {
  constructor(message: string = "Forbidden") {
    super(message, 403);
  }
}

// 404 Not Found - Resource doesn't exist
export class NotFoundError extends AppError {
  constructor(message: string = "Resource not found") {
    super(message, 404);
  }
}

// 409 Conflict - Resource already exists
export class ConflictError extends AppError {
  constructor(message: string = "Resource already exists") {
    super(message, 409);
  }
}

// 429 Too Many Requests - Rate limit exceeded
export class RateLimitError extends AppError {
  constructor(message: string = "Too many requests") {
    super(message, 429);
  }
}

// 500 Internal Server Error - Unexpected error
export class InternalServerError extends AppError {
  constructor(message: string = "Internal server error") {
    super(message, 500);
  }
}
