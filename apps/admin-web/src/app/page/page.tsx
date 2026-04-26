import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { Workspace } from "@/components/workspace/workspace";
import type { GeneratedPageResponse } from "@/lib/api";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

async function fetchMyPage(
  token: string,
): Promise<GeneratedPageResponse | null> {
  const response = await fetch(`${API_BASE_URL}/api/me/page`, {
    method: "GET",
    cache: "no-store",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (response.status === 204 || !response.ok) return null;
  return (await response.json()) as GeneratedPageResponse;
}

export default async function PageWorkspace() {
  const session = await auth();
  if (!session?.user?.springToken) redirect("/login");

  const page = await fetchMyPage(session.user.springToken);
  if (!page) redirect("/onboarding");

  return <Workspace initialPage={page} />;
}
