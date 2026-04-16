import type { NextAuthConfig } from "next-auth";

/**
 * Lightweight edge-compatible auth config used by middleware.
 * Must NOT import bcryptjs, pg, or any other Node.js-only module.
 * Full providers are wired in auth.ts for server-side use.
 */
export const authConfig: NextAuthConfig = {
  pages: { signIn: "/login" },
  session: { strategy: "jwt" },
  providers: [],
  callbacks: {
    authorized({ auth }) {
      return !!auth?.user;
    },
  },
};
