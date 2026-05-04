import NextAuth from "next-auth";
import { authConfig } from "@/auth.config";
import { NextResponse } from "next/server";

const { auth } = NextAuth(authConfig);

export default auth((req) => {
  const hostname = req.headers.get("host") ?? "";
  const subdomainMatch = hostname.match(/^([^.]+)\.ethorai\.fr$/);

  if (subdomainMatch) {
    const slug = subdomainMatch[1];
    const url = req.nextUrl.clone();
    url.pathname = `/s/${slug}`;
    return NextResponse.rewrite(url);
  }

  if (!req.auth) {
    return NextResponse.redirect(new URL("/login", req.url));
  }
});

export const config = {
  matcher: [
    "/",
    "/((?!api/auth|api/register|login|register|p/|s/|_next/static|_next/image|favicon.ico).+)",
  ],
};
