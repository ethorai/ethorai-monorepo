import { auth } from "@/auth";
import { NextResponse } from "next/server";

type AuthResult =
  | { headers: { Authorization: string } }
  | { response: NextResponse };

export async function withAuth(): Promise<AuthResult> {
  const session = await auth();
  if (!session?.user?.springToken) {
    return {
      response: NextResponse.json({ error: "Unauthorized" }, { status: 401 }),
    };
  }
  return { headers: { Authorization: `Bearer ${session.user.springToken}` } };
}
