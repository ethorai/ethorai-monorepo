import { auth } from "@/auth";
import { AdminUserDetail } from "@/lib/api";
import { notFound } from "next/navigation";
import {
  HeaderSection,
  HeroSection,
  AreasOfSupportSection,
  HowIWorkSection,
  WhatYouCanExpectSection,
  SessionFormatsSection,
  ContactSection,
  DisclaimerSection,
  FooterSection,
} from "@/components/section-renderers";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

async function fetchUserDetail(
  userId: string,
  springToken: string,
): Promise<AdminUserDetail | null> {
  const res = await fetch(`${API_BASE_URL}/api/admin/users/${userId}`, {
    cache: "no-store",
    headers: { Authorization: `Bearer ${springToken}` },
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error("Failed to fetch user detail");
  return res.json() as Promise<AdminUserDetail>;
}

export default async function AdminUserDetailPage({
  params,
}: {
  params: Promise<{ userId: string }>;
}) {
  const { userId } = await params;
  const session = await auth();

  if (!session?.user?.springToken) notFound();

  const detail = await fetchUserDetail(userId, session.user.springToken);
  if (!detail) notFound();

  const { page } = detail;

  return (
    <div>
      <div className="mb-4">
        <a
          href="/admin/users"
          className="text-sm text-stone-400 hover:text-stone-600 transition-colors"
        >
          ← Retour à la liste
        </a>
      </div>

      <div className="mb-6 bg-white rounded-xl border border-stone-200 p-5">
        <h1 className="text-lg font-semibold text-stone-800 mb-3">
          {detail.name ?? detail.email}
        </h1>
        <dl className="grid grid-cols-2 gap-x-8 gap-y-2 text-sm">
          <dt className="text-stone-400">Email</dt>
          <dd className="text-stone-700">{detail.email}</dd>
          <dt className="text-stone-400">ID utilisateur</dt>
          <dd className="text-stone-500 font-mono text-xs">{detail.userId}</dd>
          <dt className="text-stone-400">Inscrit le</dt>
          <dd className="text-stone-700">
            {new Date(detail.createdAt).toLocaleDateString("fr-FR", {
              day: "numeric",
              month: "long",
              year: "numeric",
            })}
          </dd>
          {page && (
            <>
              <dt className="text-stone-400">Statut page</dt>
              <dd>
                <span
                  className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    page.status === "PUBLISHED"
                      ? "bg-emerald-100 text-emerald-700"
                      : "bg-amber-100 text-amber-700"
                  }`}
                >
                  {page.status}
                </span>
              </dd>
              {page.subdomain && (
                <>
                  <dt className="text-stone-400">Sous-domaine</dt>
                  <dd className="text-stone-600 font-mono text-xs">
                    <a
                      href={`https://${page.subdomain}.ethorai.fr`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="hover:underline"
                    >
                      {page.subdomain}.ethorai.fr
                    </a>
                  </dd>
                </>
              )}
            </>
          )}
        </dl>
      </div>

      {page ? (
        <div>
          <h2 className="text-sm font-medium text-stone-500 uppercase tracking-wide mb-4">
            Aperçu de la page (lecture seule)
          </h2>
          <div className="rounded-xl overflow-hidden border border-stone-200 bg-white">
            <HeaderSection data={page.sections.HEADER} />
            <HeroSection data={page.sections.HERO} photoUrl={page.photoUrl} />
            <AreasOfSupportSection data={page.sections.AREAS_OF_SUPPORT} />
            <HowIWorkSection data={page.sections.HOW_I_WORK} />
            <WhatYouCanExpectSection data={page.sections.WHAT_YOU_CAN_EXPECT} />
            <SessionFormatsSection data={page.sections.SESSION_FORMATS} />
            <ContactSection data={page.sections.CONTACT} />
            <DisclaimerSection data={page.sections.DISCLAIMER} />
            <FooterSection data={page.sections.FOOTER} />
          </div>
        </div>
      ) : (
        <div className="text-center py-16 text-stone-400">
          Cet utilisateur n&apos;a pas encore de page générée.
        </div>
      )}
    </div>
  );
}
