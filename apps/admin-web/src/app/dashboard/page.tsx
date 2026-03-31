"use client";

import Link from "next/link";
import { FormEvent, useMemo, useState } from "react";
import { getPagesByProfile, type GeneratedPageResponse } from "@/lib/api";

function firstNonEmptySection(page: GeneratedPageResponse): string {
  return (
    page.sections.HERO.heading ||
    page.sections.HERO.subheading ||
    page.sections.HOW_I_WORK.description ||
    page.sections.AREAS_OF_SUPPORT.items[0] ||
    "No rendered content available yet."
  );
}

export default function DashboardPage() {
  const [profileId, setProfileId] = useState("");
  const [pages, setPages] = useState<GeneratedPageResponse[]>([]);
  const [error, setError] = useState("");
  const [hasSearched, setHasSearched] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const isSubmitDisabled = useMemo(() => {
    return isLoading || !profileId.trim();
  }, [isLoading, profileId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsLoading(true);
    setHasSearched(true);

    try {
      const result = await getPagesByProfile(profileId.trim());
      setPages(result);
    } catch (requestError) {
      const message =
        requestError instanceof Error
          ? requestError.message
          : "Unable to load pages.";
      setPages([]);
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-[linear-gradient(135deg,_#f1eee2_0%,_#f8f4ed_46%,_#e1efe5_100%)] px-4 py-10 text-stone-900 sm:px-8">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8">
        <section className="rounded-3xl border border-stone-900/10 bg-white/90 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.06)] backdrop-blur-sm sm:p-8">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.24em] text-stone-500">
                Therapist Admin
              </p>
              <h1 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
                Dashboard by Profile ID
              </h1>
              <p className="mt-3 max-w-2xl text-sm leading-6 text-stone-600 sm:text-base">
                Load all generated pages for one therapist profile to review
                draft state before editing or publishing.
              </p>
            </div>

            <div className="flex flex-wrap gap-3 text-sm">
              <Link
                className="rounded-xl border border-stone-300 px-4 py-2 font-medium text-stone-700 transition hover:bg-stone-100"
                href="/generate"
              >
                New Generation
              </Link>
              <Link
                className="rounded-xl border border-stone-300 px-4 py-2 font-medium text-stone-700 transition hover:bg-stone-100"
                href="/"
              >
                Home
              </Link>
            </div>
          </div>

          <form
            className="mt-8 grid gap-4 lg:grid-cols-[1fr_auto]"
            onSubmit={handleSubmit}
          >
            <label className="grid gap-2">
              <span className="text-sm font-medium">Profile ID</span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none ring-emerald-300 transition focus:ring"
                value={profileId}
                onChange={(event) => setProfileId(event.target.value)}
                placeholder="751be34c-48b5-4d3e-9a11-379c640c778c"
              />
            </label>

            <button
              type="submit"
              disabled={isSubmitDisabled}
              className="rounded-xl bg-stone-900 px-5 py-3 text-sm font-semibold tracking-wide text-white transition hover:bg-stone-700 disabled:cursor-not-allowed disabled:opacity-60 lg:self-end"
            >
              {isLoading ? "Loading..." : "Load Pages"}
            </button>
          </form>

          {error ? (
            <p className="mt-4 rounded-lg border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {error}
            </p>
          ) : null}
        </section>

        {!hasSearched ? (
          <section className="rounded-3xl border border-dashed border-stone-400/40 bg-white/55 p-10 text-center text-sm text-stone-500">
            Enter a profile ID to list generated pages.
          </section>
        ) : null}

        {hasSearched && !error && pages.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-stone-400/40 bg-white/55 p-10 text-center text-sm text-stone-500">
            No pages found for this profile.
          </section>
        ) : null}

        {pages.length > 0 ? (
          <section className="grid gap-5 lg:grid-cols-2">
            {pages.map((page) => (
              <article
                key={page.pageId}
                className="rounded-3xl border border-stone-900/10 bg-white/90 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.06)]"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-[0.22em] text-stone-500">
                      {page.role}
                    </p>
                    <h2 className="mt-2 text-2xl font-semibold tracking-tight">
                      {page.fullName}
                    </h2>
                  </div>
                  <span className="rounded-full border border-emerald-300 bg-emerald-50 px-3 py-1 text-xs font-semibold tracking-[0.18em] text-emerald-700">
                    {page.status}
                  </span>
                </div>

                <dl className="mt-5 grid gap-3 text-sm text-stone-600">
                  <div>
                    <dt className="font-medium text-stone-900">Page ID</dt>
                    <dd className="break-all">{page.pageId}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-stone-900">Profile ID</dt>
                    <dd className="break-all">{page.profileId}</dd>
                  </div>
                </dl>

                <div className="mt-5 rounded-2xl bg-stone-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-[0.18em] text-stone-500">
                    Content Preview
                  </p>
                  <p className="mt-2 line-clamp-4 text-sm leading-6 text-stone-700">
                    {firstNonEmptySection(page)}
                  </p>
                </div>

                <div className="mt-5 flex items-center gap-3">
                  <Link
                    className="inline-flex items-center rounded-xl bg-stone-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-stone-700"
                    href={`/pages/${page.pageId}`}
                  >
                    Open Detail
                  </Link>
                  <Link
                    className="text-xs font-medium text-stone-500 underline-offset-4 transition hover:text-stone-700 hover:underline"
                    href={`/api/pages/${page.pageId}`}
                    target="_blank"
                  >
                    Open JSON
                  </Link>
                </div>
              </article>
            ))}
          </section>
        ) : null}
      </div>
    </main>
  );
}
