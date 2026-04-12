import { auth } from "@/auth";
import { NextResponse } from "next/server";

export default auth((req) => {
  if (!req.auth) {
    return NextResponse.redirect(new URL("/login", req.url));
  }
});

export const config = {
  matcher: [
    /*
     * Protect all routes EXCEPT:
     *  - /api/auth (Auth.js endpoints)
     *  - /login
     *  - /_next/static, /_next/image
     *  - /favicon.ico
     */
    "/((?!api/auth|api/register|login|register|_next/static|_next/image|favicon.ico).*)",
  ],
};
