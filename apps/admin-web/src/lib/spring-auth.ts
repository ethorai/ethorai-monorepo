import { SignJWT } from "jose";

/**
 * Signs a short-lived HS256 JWT for communication with the Spring API.
 * Uses the shared JWT_SECRET env var (base64url encoded).
 */
export async function signSpringJwt(
  userId: string,
  email: string,
): Promise<string> {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error("JWT_SECRET is not configured");
  }
  const key = Buffer.from(secret, "base64url");
  return new SignJWT({ sub: userId, email })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("5m")
    .sign(key);
}
