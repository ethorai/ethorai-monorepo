import { NextRequest, NextResponse } from "next/server";
import { withAuth } from "@/lib/spring-fetch";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

type Params = {
  params: Promise<{ id: string }>;
};

export async function GET(_request: NextRequest, { params }: Params) {
  const authResult = await withAuth();
  if ("response" in authResult) return authResult.response;

  const { id } = await params;

  try {
    const response = await fetch(`${API_BASE_URL}/api/pages/${id}`, {
      method: "GET",
      cache: "no-store",
      headers: { ...authResult.headers },
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
