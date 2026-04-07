import { auth } from "@/auth";
import { signSpringJwt } from "./spring-auth";
import { NextResponse } from "next/server";

type AuthResult =
  | { headers: { Authorization: string } }
  | { response: NextResponse };

/**
 * Resolves the current session and returns auth headers for Spring API calls.
 * Returns a 401 NextResponse if not authenticated.
 */
export async function withAuth(): Promise<AuthResult> {
  const session = await auth();
  if (!session?.user?.id) {
    return {
      response: NextResponse.json({ error: "Unauthorized" }, { status: 401 }),
    };
  }
  const token = await signSpringJwt(session.user.id, session.user.email ?? "");
  return { headers: { Authorization: `Bearer ${token}` } };
}
