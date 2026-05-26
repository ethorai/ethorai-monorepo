import { NextResponse } from "next/server";
import { withAuth } from "@/lib/spring-fetch";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

export async function GET(
  _req: Request,
  { params }: { params: Promise<{ userId: string }> },
) {
  const authResult = await withAuth();
  if ("response" in authResult) return authResult.response;

  const { userId } = await params;

  try {
    const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}`, {
      method: "GET",
      cache: "no-store",
      headers: { ...authResult.headers },
    });

    const text = await response.text();
    return new NextResponse(text, {
      status: response.status,
      headers: {
        "Content-Type":
          response.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch {
    return NextResponse.json(
      { code: "ADMIN_PROXY_ERROR", message: "Could not reach backend." },
      { status: 502 },
    );
  }
}
