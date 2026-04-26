"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { signOut } from "next-auth/react";
import {
  generateLandingPage,
  getGenerationStatus,
  getMyProfile,
  publishPage,
  type GeneratedPageResponse,
} from "@/lib/api";
import {
  saveOnboarding,
  type OnboardingState,
} from "@/lib/onboarding-storage";
import {
  AreasOfSupportSection,
  ContactSection,
  DisclaimerSection,
  FooterSection,
  HeaderSection,
  HeroSection,
  HowIWorkSection,
  SessionFormatsSection,
  WhatYouCanExpectSection,
} from "@/components/section-renderers";

const POLL_INTERVAL_MS = 2000;

type WorkspaceProps = {
  initialPage: GeneratedPageResponse;
};

export function Workspace({ initialPage }: WorkspaceProps) {
  const router = useRouter();
  const [page, setPage] = useState<GeneratedPageResponse>(initialPage);
  const [publishing, setPublishing] = useState(false);
  const [regenerating, setRegenerating] = useState(false);
  const [busy, setBusy] = useState<"modify" | null>(null);
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");
  const toastTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const isPublished = page.status === "PUBLISHED";
  const publicUrl =
    typeof window !== "undefined"
      ? `${window.location.origin}/p/${page.pageId}`
      : `/p/${page.pageId}`;

  function showToast(message: string) {
    setToast(message);
    if (toastTimer.current) clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(""), 4000);
  }

  async function handleModifyResponses() {
    setError("");
    setBusy("modify");
    try {
      const profile = await getMyProfile();
      if (!profile) throw new Error("Aucun profil trouvé.");

      const seeded: OnboardingState = {
        fullName: profile.fullName,
        location: profile.location ?? "",
        role: profile.role,
        audiences: profile.audiences,
        areasOfSupport: profile.areasOfSupport,
        approach: profile.approach ?? "",
        sessionFormat: profile.sessionFormat,
        expectations: profile.expectations,
        contactMethod: profile.contactMethod,
        contactValue: profile.contactValue,
      };
      saveOnboarding({ state: seeded, step: 1 });
      router.push("/onboarding");
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "Impossible de charger vos réponses.",
      );
      setBusy(null);
    }
  }

  async function handleRegenerate() {
    setError("");
    setRegenerating(true);
    try {
      const profile = await getMyProfile();
      if (!profile) throw new Error("Aucun profil trouvé.");

      const job = await generateLandingPage(profile);

      await new Promise<void>((resolve, reject) => {
        const interval = setInterval(async () => {
          try {
            const status = await getGenerationStatus(job.jobId);
            if (status.status === "COMPLETED" && status.pageId) {
              clearInterval(interval);
              router.refresh();
              resolve();
            } else if (status.status === "FAILED") {
              clearInterval(interval);
              reject(new Error(status.error ?? "La génération a échoué."));
            }
          } catch (err) {
            clearInterval(interval);
            reject(err);
          }
        }, POLL_INTERVAL_MS);
      });
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "La régénération a échoué.",
      );
    } finally {
      setRegenerating(false);
    }
  }

  async function handlePublish() {
    setError("");
    setPublishing(true);
    try {
      await publishPage(page.pageId);
      setPage({ ...page, status: "PUBLISHED" });
      try {
        await navigator.clipboard.writeText(publicUrl);
        showToast("Page publiée. Lien copié dans le presse-papier.");
      } catch {
        showToast("Page publiée.");
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "La publication a échoué.");
    } finally {
      setPublishing(false);
    }
  }

  async function handleCopyLink() {
    try {
      await navigator.clipboard.writeText(publicUrl);
      showToast("Lien copié.");
    } catch {
      showToast("Impossible de copier le lien.");
    }
  }

  const sections = page.sections;
  const anyBusy = publishing || regenerating || busy !== null;

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-20 border-b border-stone-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-3 px-6 py-3 sm:px-10">
          <span className="font-serif text-xl font-medium text-stone-900">
            Ethorai
          </span>
          <StatusPill published={isPublished} />

          <div className="ml-auto flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={handleModifyResponses}
              disabled={anyBusy}
              className="rounded-xl border border-stone-300 bg-white px-3 py-2 text-sm font-medium text-stone-700 transition hover:bg-stone-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {busy === "modify" ? "Chargement..." : "Modifier mes réponses"}
            </button>
            <button
              type="button"
              onClick={handleRegenerate}
              disabled={anyBusy}
              className="rounded-xl border border-stone-300 bg-white px-3 py-2 text-sm font-medium text-stone-700 transition hover:bg-stone-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {regenerating ? "Régénération..." : "Régénérer"}
            </button>

            {isPublished ? (
              <>
                <button
                  type="button"
                  onClick={handleCopyLink}
                  className="rounded-xl border border-stone-300 bg-white px-3 py-2 text-sm font-medium text-stone-700 transition hover:bg-stone-50"
                >
                  Copier le lien
                </button>
                <a
                  href={publicUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="rounded-xl bg-stone-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-stone-700"
                >
                  Voir comme un visiteur
                </a>
              </>
            ) : (
              <button
                type="button"
                onClick={handlePublish}
                disabled={anyBusy}
                className="rounded-xl bg-stone-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-stone-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {publishing ? "Publication..." : "Publier ma page"}
              </button>
            )}

            <span className="mx-1 h-6 w-px bg-stone-200" />

            <button
              type="button"
              onClick={() => signOut({ callbackUrl: "/login" })}
              className="text-sm text-stone-500 transition hover:text-stone-900"
            >
              Se déconnecter
            </button>
          </div>
        </div>

        {error ? (
          <div className="border-t border-rose-200 bg-rose-50 px-6 py-2 text-sm text-rose-700 sm:px-10">
            {error}
          </div>
        ) : null}
      </header>

      <main className="flex-1">
        {regenerating ? (
          <div className="flex min-h-[60vh] items-center justify-center">
            <div className="flex flex-col items-center gap-4 text-stone-600">
              <div className="h-10 w-10 animate-spin rounded-full border-2 border-stone-300 border-t-stone-900" />
              <p className="text-sm">
                Régénération en cours... environ 30 secondes.
              </p>
            </div>
          </div>
        ) : (
          <>
            <HeaderSection data={sections.HEADER} />
            <HeroSection data={sections.HERO} />
            <AreasOfSupportSection data={sections.AREAS_OF_SUPPORT} />
            <HowIWorkSection data={sections.HOW_I_WORK} />
            <WhatYouCanExpectSection data={sections.WHAT_YOU_CAN_EXPECT} />
            <SessionFormatsSection data={sections.SESSION_FORMATS} />
            <ContactSection data={sections.CONTACT} />
            <DisclaimerSection data={sections.DISCLAIMER} />
            <FooterSection data={sections.FOOTER} />
          </>
        )}
      </main>

      {toast ? (
        <div className="pointer-events-none fixed inset-x-0 bottom-6 z-30 flex justify-center px-4">
          <div className="pointer-events-auto rounded-2xl bg-stone-900 px-5 py-3 text-sm font-medium text-white shadow-lg">
            {toast}
          </div>
        </div>
      ) : null}
    </div>
  );
}

function StatusPill({ published }: { published: boolean }) {
  if (published) {
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-300 bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
        <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
        Publié
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-stone-300 bg-stone-100 px-3 py-1 text-xs font-semibold text-stone-600">
      <span className="h-1.5 w-1.5 rounded-full bg-stone-400" />
      Brouillon
    </span>
  );
}
