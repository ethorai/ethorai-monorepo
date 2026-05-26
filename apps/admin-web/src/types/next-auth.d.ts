import "next-auth";
import "next-auth/jwt";

declare module "next-auth" {
  interface Session {
    user: {
      id: string;
      email: string;
      name?: string | null;
      image?: string | null;
      springToken: string;
      isAdmin: boolean;
    };
  }

  interface User {
    springToken?: string;
    isAdmin?: boolean;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    springToken?: string;
    isAdmin?: boolean;
  }
}
