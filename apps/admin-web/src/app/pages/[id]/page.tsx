"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import {
  GeneratedPageResponse,
  getPage,
  parseSections,
  publishPage,
} from "@/lib/api";
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

type DetailPageProps = {
  params: Promise<{ id: string }>;
};

export default function PageDetail({ params }: DetailPageProps) {
  const [pageId, setPageId] = useState("");
  const [page, setPage] = useState<GeneratedPageResponse | null>(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isPublishing, setIsPublishing] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadPage() {
      try {
        const { id } = await params;
        if (!active) {
          return;
        }

        setPageId(id);
        const loadedPage = await getPage(id);
        if (!active) {
          return;
        }

        setPage(loadedPage);
        setError("");
      } catch (requestError) {
        if (!active) {
          return;
        }
        const message =
          requestError instanceof Error
            ? requestError.message
            : "Unable to load page.";
        setError(message);
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void loadPage();

    return () => {
      active = false;
    };
  }, [params]);

  const canPublish = useMemo(() => {
    return !!page && page.status !== "PUBLISHED" && !isPublishing;
  }, [isPublishing, page]);

  const parsedSections = useMemo(() => {
    if (!page) {
      return null;
    }

    try {
      return parseSections(page.sections);
    } catch {
      return null;
    }
  }, [page]);

  async function handlePublish() {
    if (!page) {
      return;
    }

    setIsPublishing(true);
    setError("");

    try {
      await publishPage(page.pageId);
      const refreshed = await getPage(page.pageId);
      setPage(refreshed);
    } catch (requestError) {
      const message =
        requestError instanceof Error
          ? requestError.message
          : "Unable to publish page.";
      setError(message);
    } finally {
      setIsPublishing(false);
    }
  }

  return (
    <main className="min-h-screen bg-[linear-gradient(180deg,_#f4efe4_0%,_#faf7f1_44%,_#ebf5ee_100%)] px-4 py-10 text-stone-900 sm:px-8">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8">
        <section className="rounded-3xl border border-stone-900/10 bg-white/90 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.06)] backdrop-blur-sm sm:p-8">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.24em] text-stone-500">
                Page Detail
              </p>
              <h1 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
                Review Generated Draft
              </h1>
              <p className="mt-3 max-w-3xl text-sm leading-6 text-stone-600 sm:text-base">
                Read the full generated content for one page, confirm its
                current status, and publish when ready.
              </p>
            </div>

            <div className="flex flex-wrap gap-3 text-sm">
              <Link
                className="rounded-xl border border-stone-300 px-4 py-2 font-medium text-stone-700 transition hover:bg-stone-100"
                href="/dashboard"
              >
                Back to Dashboard
              </Link>
              <Link
                className="rounded-xl border border-stone-300 px-4 py-2 font-medium text-stone-700 transition hover:bg-stone-100"
                href="/generate"
              >
                New Generation
              </Link>
            </div>
          </div>

          <div className="mt-6 grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
            <div className="rounded-2xl bg-stone-50 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.18em] text-stone-500">
                Page ID
              </p>
              <p className="mt-2 break-all text-sm text-stone-700">
                {pageId || "Loading..."}
              </p>
            </div>

            <button
              type="button"
              onClick={handlePublish}
              disabled={!canPublish}
              className="rounded-xl bg-stone-900 px-5 py-3 text-sm font-semibold tracking-wide text-white transition hover:bg-stone-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isPublishing
                ? "Publishing..."
                : page?.status === "PUBLISHED"
                  ? "Already Published"
                  : "Publish Draft"}
            </button>
          </div>

          {error ? (
            <p className="mt-4 rounded-lg border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {error}
            </p>
          ) : null}
        </section>

        {isLoading ? (
          <section className="rounded-3xl border border-dashed border-stone-400/40 bg-white/55 p-10 text-center text-sm text-stone-500">
            Loading page...
          </section>
        ) : null}

        {!isLoading && page ? (
          <>
            <section className="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
              <article className="rounded-3xl border border-stone-900/10 bg-white/90 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.06)]">
                <p className="text-xs font-bold uppercase tracking-[0.18em] text-stone-500">
                  Profile
                </p>
                <h2 className="mt-3 text-2xl font-semibold tracking-tight">
                  {page.fullName}
                </h2>
                <dl className="mt-5 grid gap-3 text-sm text-stone-700">
                  <div>
                    <dt className="font-medium text-stone-900">Role</dt>
                    <dd>{page.role}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-stone-900">Profile ID</dt>
                    <dd className="break-all">{page.profileId}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-stone-900">Status</dt>
                    <dd>{page.status}</dd>
                  </div>
                </dl>
              </article>

              <article className="rounded-3xl border border-stone-900/10 bg-white/90 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.06)]">
                <p className="text-xs font-bold uppercase tracking-[0.18em] text-stone-500">
                  Workflow
                </p>
                <ul className="mt-4 space-y-3 text-sm leading-6 text-stone-700">
                  <li>
                    1. Review the generated sections below for tone and
                    guardrail compliance.
                  </li>
                  <li>
                    2. Confirm the page is still in draft if content needs
                    review.
                  </li>
                  <li>
                    3. Publish once you are satisfied with the generated output.
                  </li>
                </ul>
              </article>
            </section>

            <section className="overflow-hidden rounded-3xl border border-stone-900/10 bg-white/90 shadow-[0_10px_40px_rgba(0,0,0,0.06)]">
              {parsedSections ? (
                <>
                  <HeaderSection data={parsedSections.HEADER} />
                  <HeroSection data={parsedSections.HERO} />
                  <AreasOfSupportSection
                    data={parsedSections.AREAS_OF_SUPPORT}
                  />
                  <HowIWorkSection data={parsedSections.HOW_I_WORK} />
                  <WhatYouCanExpectSection
                    data={parsedSections.WHAT_YOU_CAN_EXPECT}
                  />
                  <SessionFormatsSection
                    data={parsedSections.SESSION_FORMATS}
                  />
                  <ContactSection data={parsedSections.CONTACT} />
                  <DisclaimerSection data={parsedSections.DISCLAIMER} />
                  <FooterSection data={parsedSections.FOOTER} />
                </>
              ) : (
                <div className="p-6 text-sm text-rose-700">
                  Unable to parse structured section data.
                </div>
              )}
            </section>
          </>
        ) : null}
      </div>
    </main>
  );
}
