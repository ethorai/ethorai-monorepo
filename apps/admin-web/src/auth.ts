import NextAuth from "next-auth";
import Google from "next-auth/providers/google";
import Credentials from "next-auth/providers/credentials";
import { authConfig } from "@/auth.config";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";
const INTERNAL_SECRET = process.env.INTERNAL_SECRET ?? "";

type SpringAuthResponse = { token: string; userId: string };

async function springLogin(
  email: string,
  password: string,
): Promise<SpringAuthResponse | null> {
  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) return null;
    return res.json() as Promise<SpringAuthResponse>;
  } catch {
    return null;
  }
}

async function springOAuth(
  email: string,
  name: string,
  provider: string,
  providerAccountId: string,
): Promise<SpringAuthResponse | null> {
  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/oauth`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Internal-Secret": INTERNAL_SECRET,
      },
      body: JSON.stringify({ email, name, provider, providerAccountId }),
    });
    if (!res.ok) return null;
    return res.json() as Promise<SpringAuthResponse>;
  } catch {
    return null;
  }
}

function decodeIsAdmin(token: string): boolean {
  try {
    const payload = JSON.parse(
      Buffer.from(token.split(".")[1], "base64url").toString(),
    );
    return payload.isAdmin === true;
  } catch {
    return false;
  }
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  ...authConfig,
  providers: [
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    }),
    Credentials({
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) return null;
        const result = await springLogin(
          String(credentials.email),
          String(credentials.password),
        );
        if (!result) return null;
        return {
          id: result.userId,
          email: String(credentials.email),
          springToken: result.token,
        };
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user, account, profile }) {
      if (user?.springToken) {
        token.sub = user.id ?? token.sub;
        token.springToken = user.springToken;
        token.isAdmin = decodeIsAdmin(user.springToken);
      }
      if (account?.provider === "google" && profile?.email) {
        const result = await springOAuth(
          profile.email,
          (profile.name ?? profile.email) as string,
          "google",
          account.providerAccountId,
        );
        if (result) {
          token.sub = result.userId;
          token.springToken = result.token;
          token.isAdmin = decodeIsAdmin(result.token);
        }
      }
      return token;
    },
    async session({ session, token }) {
      session.user.id = token.sub ?? "";
      session.user.springToken = token.springToken ?? "";
      session.user.isAdmin = token.isAdmin ?? false;
      return session;
    },
  },
});
