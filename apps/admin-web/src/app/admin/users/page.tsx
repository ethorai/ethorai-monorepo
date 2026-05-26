import { auth } from "@/auth";
import { AdminUserSummary } from "@/lib/api";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

async function fetchUsers(springToken: string): Promise<AdminUserSummary[]> {
  const res = await fetch(`${API_BASE_URL}/api/admin/users`, {
    cache: "no-store",
    headers: { Authorization: `Bearer ${springToken}` },
  });
  if (!res.ok) return [];
  return res.json() as Promise<AdminUserSummary[]>;
}

function statusBadge(status: string | null) {
  if (!status) return <span className="text-stone-400 text-xs">—</span>;
  const color =
    status === "PUBLISHED"
      ? "bg-emerald-100 text-emerald-700"
      : "bg-amber-100 text-amber-700";
  return (
    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${color}`}>
      {status}
    </span>
  );
}

export default async function AdminUsersPage() {
  const session = await auth();
  const users = session?.user?.springToken
    ? await fetchUsers(session.user.springToken)
    : [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-stone-800">
          Utilisateurs ({users.length})
        </h1>
      </div>

      <div className="bg-white rounded-xl border border-stone-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-100 text-left text-stone-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Nom</th>
              <th className="px-4 py-3">Sous-domaine</th>
              <th className="px-4 py-3">Page</th>
              <th className="px-4 py-3">Inscrit le</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr>
                <td
                  colSpan={5}
                  className="px-4 py-8 text-center text-stone-400"
                >
                  Aucun utilisateur.
                </td>
              </tr>
            )}
            {users.map((u) => (
              <tr
                key={u.userId}
                className="border-b border-stone-50 last:border-0 hover:bg-stone-50 transition-colors"
              >
                <td className="px-4 py-3">
                  <a
                    href={`/admin/users/${u.userId}`}
                    className="text-stone-800 hover:underline font-medium"
                  >
                    {u.email}
                  </a>
                </td>
                <td className="px-4 py-3 text-stone-600">{u.fullName ?? u.name ?? "—"}</td>
                <td className="px-4 py-3 text-stone-500 font-mono text-xs">
                  {u.subdomain ? `${u.subdomain}.ethorai.fr` : "—"}
                </td>
                <td className="px-4 py-3">{statusBadge(u.pageStatus)}</td>
                <td className="px-4 py-3 text-stone-400 text-xs">
                  {new Date(u.createdAt).toLocaleDateString("fr-FR")}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
