import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  const body = await req.json();

  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/api/auth/register`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    },
  );

  if (res.status === 201) {
    return NextResponse.json({ ok: true }, { status: 201 });
  }
  if (res.status === 409) {
    return NextResponse.json(
      { error: "An account with this email already exists." },
      { status: 409 },
    );
  }
  if (res.status === 400) {
    return NextResponse.json(
      { error: "Please fill in all required fields correctly." },
      { status: 400 },
    );
  }

  return NextResponse.json({ error: "Registration failed." }, { status: 502 });
}
