import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

type Params = {
  params: Promise<{ id: string; sectionType: string }>;
};

export async function POST(_request: NextRequest, { params }: Params) {
  const { id, sectionType } = await params;

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/pages/${id}/sections/${sectionType}/regenerate`,
      { method: "POST" },
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
