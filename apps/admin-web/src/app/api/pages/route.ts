import { NextRequest, NextResponse } from "next/server";
import { withAuth } from "@/lib/spring-fetch";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

export async function GET(request: NextRequest) {
  const authResult = await withAuth();
  if ("response" in authResult) return authResult.response;

  const profileId = request.nextUrl.searchParams.get("profileId");

  if (!profileId) {
    return NextResponse.json(
      {
        code: "MISSING_PROFILE_ID",
        message: "profileId query parameter is required.",
      },
      { status: 400 },
    );
  }

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/pages?profileId=${encodeURIComponent(profileId)}`,
      {
        method: "GET",
        cache: "no-store",
        headers: { ...authResult.headers },
      },
    );

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
