import morgan from "morgan";

/**
 * HTTP request logger using morgan
 * Format: :method :url :status :response-time ms - :res[content-length]
 */
export const requestLogger = morgan(
  ":method :url :status :response-time ms - :res[content-length]",
  {
    skip: (req, res) => {
      // Skip logging health check requests to reduce noise
      return req.url === "/health";
    },
  }
);

/**
 * Development logger with more details
 */
export const devLogger = morgan("dev");
