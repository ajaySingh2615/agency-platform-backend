import { Request, Response, NextFunction } from "express";
import { AnyZodObject, ZodError } from "zod";
import { BadRequestError } from "../utils/errors";

// Middleware to validate request using Zod schema
export function validateRequest(schema: AnyZodObject) {
  return async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      await schema.parseAsync({
        body: req.body,
        query: req.query,
        params: req.params,
      });
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const errorMessages = error.errors.map((err) => ({
          field: err.path.join("."),
          message: err.message,
        }));

        next(
          new BadRequestError(
            JSON.stringify({
              message: "Validation failed",
              errors: errorMessages,
            })
          )
        );
      } else {
        next(error);
      }
    }
  };
}
