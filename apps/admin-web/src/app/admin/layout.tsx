import { auth } from "@/auth";
import { redirect } from "next/navigation";

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await auth();
  if (!session?.user?.isAdmin) {
    redirect("/");
  }

  return (
    <div className="min-h-screen bg-stone-50">
      <header className="border-b border-stone-200 bg-white px-6 py-3 flex items-center gap-4">
        <span className="font-semibold text-stone-800">Ethorai Admin</span>
        <nav className="flex gap-4 text-sm text-stone-500">
          <a href="/admin/users" className="hover:text-stone-800 transition-colors">
            Utilisateurs
          </a>
        </nav>
        <div className="ml-auto text-xs text-stone-400">{session.user.email}</div>
      </header>
      <main className="p-6">{children}</main>
    </div>
  );
}
