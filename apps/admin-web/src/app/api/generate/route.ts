import { NextRequest, NextResponse } from "next/server";
import { withAuth } from "@/lib/spring-fetch";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const authResult = await withAuth();
  if ("response" in authResult) return authResult.response;

  const body = await request.text();

  try {
    const response = await fetch(`${API_BASE_URL}/api/generate`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...authResult.headers,
      },
      body,
      cache: "no-store",
    });

    const responseText = await response.text();

    return new NextResponse(responseText, {
      status: response.status,
      headers: {
        "Content-Type":
          response.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch {
    return NextResponse.json(
      {
        code: "ADMIN_PROXY_ERROR",
        message: "Admin API could not reach the backend service.",
      },
      { status: 502 },
    );
  }
}
