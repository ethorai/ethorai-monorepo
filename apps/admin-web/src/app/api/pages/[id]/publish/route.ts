import { NextRequest, NextResponse } from "next/server";
import { revalidatePath } from "next/cache";
import { withAuth } from "@/lib/spring-fetch";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

type Params = {
  params: Promise<{ id: string }>;
};

export async function POST(_request: NextRequest, { params }: Params) {
  const authResult = await withAuth();
  if ("response" in authResult) return authResult.response;

  const { id } = await params;

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/pages/${id}/publish`, {
      method: "POST",
      headers: { ...authResult.headers },
    });
  } catch (err) {
    console.error("[publish] fetch to Spring failed:", err);
    return NextResponse.json(
      {
        code: "ADMIN_PROXY_ERROR",
        message: "Admin API could not reach the backend service.",
      },
      { status: 502 },
    );
  }

  const responseText = await response.text();

  if (response.ok) {
    try {
      revalidatePath(`/p/${id}`);
    } catch (err) {
      console.error("[publish] revalidatePath failed:", err);
    }
  }

  return new NextResponse(responseText, {
    status: response.status,
    headers: {
      "Content-Type":
        response.headers.get("Content-Type") ?? "application/json",
    },
  });
}
