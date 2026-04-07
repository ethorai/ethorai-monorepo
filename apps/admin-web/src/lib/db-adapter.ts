import type { Adapter, AdapterUser, AdapterAccount } from "@auth/core/adapters";
import pg from "pg";

const pool = new pg.Pool({ connectionString: process.env.DATABASE_URL });

function toAdapterUser(row: Record<string, unknown>): AdapterUser {
  return {
    id: String(row.id),
    name: row.name as string | null,
    email: row.email as string,
    emailVerified: row.email_verified
      ? new Date(row.email_verified as string)
      : null,
    image: row.image as string | null,
  };
}

export function createDbAdapter(): Adapter {
  return {
    async createUser(user) {
      const { rows } = await pool.query(
        `INSERT INTO app_user (name, email, email_verified, image)
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [user.name, user.email, user.emailVerified, user.image],
      );
      return toAdapterUser(rows[0]);
    },

    async getUser(id) {
      const { rows } = await pool.query(
        "SELECT * FROM app_user WHERE id = $1",
        [id],
      );
      return rows[0] ? toAdapterUser(rows[0]) : null;
    },

    async getUserByEmail(email) {
      const { rows } = await pool.query(
        "SELECT * FROM app_user WHERE email = $1",
        [email],
      );
      return rows[0] ? toAdapterUser(rows[0]) : null;
    },

    async getUserByAccount({ provider, providerAccountId }) {
      const { rows } = await pool.query(
        `SELECT u.* FROM app_user u
         JOIN oauth_account a ON a.user_id = u.id
         WHERE a.provider = $1 AND a.provider_account_id = $2`,
        [provider, providerAccountId],
      );
      return rows[0] ? toAdapterUser(rows[0]) : null;
    },

    async updateUser(user) {
      const { rows } = await pool.query(
        `UPDATE app_user
         SET name = COALESCE($1, name),
             email = COALESCE($2, email),
             email_verified = COALESCE($3, email_verified),
             image = COALESCE($4, image),
             updated_at = now()
         WHERE id = $5 RETURNING *`,
        [user.name, user.email, user.emailVerified, user.image, user.id],
      );
      return toAdapterUser(rows[0]);
    },

    async linkAccount(account: AdapterAccount) {
      await pool.query(
        `INSERT INTO oauth_account
           (user_id, provider, provider_account_id, access_token, refresh_token,
            expires_at, token_type, scope, id_token)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
         ON CONFLICT (provider, provider_account_id) DO NOTHING`,
        [
          account.userId,
          account.provider,
          account.providerAccountId,
          account.access_token ?? null,
          account.refresh_token ?? null,
          account.expires_at ?? null,
          account.token_type ?? null,
          account.scope ?? null,
          account.id_token ?? null,
        ],
      );
    },

    // Not used with JWT session strategy
    async createSession() {
      throw new Error("DB sessions not used");
    },
    async getSessionAndUser() {
      return null;
    },
    async updateSession() {
      return null;
    },
    async deleteSession() {},

    // Verification tokens — for magic link (future)
    async createVerificationToken(token) {
      const { rows } = await pool.query(
        `INSERT INTO verification_token (identifier, token, expires)
         VALUES ($1, $2, $3) RETURNING *`,
        [token.identifier, token.token, token.expires],
      );
      return rows[0] ?? null;
    },
    async useVerificationToken({ identifier, token }) {
      const { rows } = await pool.query(
        `DELETE FROM verification_token
         WHERE identifier = $1 AND token = $2 RETURNING *`,
        [identifier, token],
      );
      return rows[0]
        ? {
            identifier: rows[0].identifier,
            token: rows[0].token,
            expires: new Date(rows[0].expires),
          }
        : null;
    },
  };
}
