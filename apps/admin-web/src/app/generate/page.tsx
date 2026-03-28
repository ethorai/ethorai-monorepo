"use client";

import { FormEvent, useMemo, useState } from "react";
import {
  generateLandingPage,
  GeneratedPageResponse,
  SectionType,
  TherapistInput,
} from "@/lib/api";

type FormState = {
  fullName: string;
  role: TherapistInput["role"];
  location: string;
  audiences: string;
  areasOfSupport: string;
  approach: string;
  sessionFormat: TherapistInput["sessionFormat"];
  expectations: string;
  contactMethod: TherapistInput["contactMethod"];
  contactValue: string;
};

const initialState: FormState = {
  fullName: "",
  role: "THERAPIST",
  location: "",
  audiences: "Adults",
  areasOfSupport: "Stress, Anxiety",
  approach: "Integrative",
  sessionFormat: "ONLINE",
  expectations: "Confidentiality, Respect",
  contactMethod: "EMAIL",
  contactValue: "",
};

const sectionOrder: SectionType[] = [
  "HEADER",
  "HERO",
  "AREAS_OF_SUPPORT",
  "HOW_I_WORK",
  "WHAT_YOU_CAN_EXPECT",
  "SESSION_FORMATS",
  "CONTACT",
  "DISCLAIMER",
  "FOOTER",
];

function toList(value: string): string[] {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export default function GeneratePage() {
  const [form, setForm] = useState<FormState>(initialState);
  const [result, setResult] = useState<GeneratedPageResponse | null>(null);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isSubmitDisabled = useMemo(() => {
    return isSubmitting || !form.fullName.trim() || !form.contactValue.trim();
  }, [form.contactValue, form.fullName, isSubmitting]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setResult(null);
    setIsSubmitting(true);

    const payload: TherapistInput = {
      fullName: form.fullName.trim(),
      role: form.role,
      location: form.location.trim(),
      audiences: toList(form.audiences),
      areasOfSupport: toList(form.areasOfSupport),
      approach: form.approach.trim(),
      sessionFormat: form.sessionFormat,
      expectations: toList(form.expectations),
      contactMethod: form.contactMethod,
      contactValue: form.contactValue.trim(),
    };

    try {
      const generated = await generateLandingPage(payload);
      setResult(generated);
    } catch (submitError) {
      const message =
        submitError instanceof Error
          ? submitError.message
          : "Generation failed.";
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,_#fbe3d3_0%,_#f7f5ee_42%,_#e8f6e8_100%)] px-4 py-10 text-stone-900 sm:px-8">
      <div className="mx-auto grid w-full max-w-7xl gap-8 lg:grid-cols-[1.05fr_0.95fr]">
        <section className="rounded-3xl border border-stone-900/15 bg-white/85 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur-sm sm:p-8">
          <p className="text-xs font-bold uppercase tracking-[0.22em] text-stone-500">
            Therapist Admin
          </p>
          <h1 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
            Generate Landing Page Draft
          </h1>
          <p className="mt-3 max-w-2xl text-sm text-stone-600 sm:text-base">
            Fill this form to create a compliant first draft from your Spring
            API.
          </p>

          <form className="mt-8 grid gap-5" onSubmit={handleSubmit}>
            <label className="grid gap-2">
              <span className="text-sm font-medium">Full name</span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.fullName}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, fullName: e.target.value }))
                }
                placeholder="Dr Jane Doe"
                required
              />
            </label>

            <div className="grid gap-4 sm:grid-cols-2">
              <label className="grid gap-2">
                <span className="text-sm font-medium">Role</span>
                <select
                  className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                  value={form.role}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      role: e.target.value as FormState["role"],
                    }))
                  }
                >
                  <option value="THERAPIST">THERAPIST</option>
                  <option value="PSYCHOLOGIST">PSYCHOLOGIST</option>
                  <option value="COUNSELOR">COUNSELOR</option>
                </select>
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium">Session format</span>
                <select
                  className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                  value={form.sessionFormat}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      sessionFormat: e.target
                        .value as FormState["sessionFormat"],
                    }))
                  }
                >
                  <option value="ONLINE">ONLINE</option>
                  <option value="IN_PERSON">IN_PERSON</option>
                  <option value="BOTH">BOTH</option>
                </select>
              </label>
            </div>

            <label className="grid gap-2">
              <span className="text-sm font-medium">Location</span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.location}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, location: e.target.value }))
                }
                placeholder="Paris"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Audiences (comma-separated)
              </span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.audiences}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, audiences: e.target.value }))
                }
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Areas of support (comma-separated)
              </span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.areasOfSupport}
                onChange={(e) =>
                  setForm((prev) => ({
                    ...prev,
                    areasOfSupport: e.target.value,
                  }))
                }
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">Approach</span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.approach}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, approach: e.target.value }))
                }
                placeholder="Integrative"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Expectations (comma-separated)
              </span>
              <input
                className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                value={form.expectations}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, expectations: e.target.value }))
                }
              />
            </label>

            <div className="grid gap-4 sm:grid-cols-[0.42fr_0.58fr]">
              <label className="grid gap-2">
                <span className="text-sm font-medium">Contact method</span>
                <select
                  className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                  value={form.contactMethod}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      contactMethod: e.target
                        .value as FormState["contactMethod"],
                    }))
                  }
                >
                  <option value="EMAIL">EMAIL</option>
                  <option value="PHONE">PHONE</option>
                  <option value="BOOKING_LINK">BOOKING_LINK</option>
                </select>
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium">Contact value</span>
                <input
                  className="rounded-xl border border-stone-300 bg-stone-50 px-4 py-2.5 outline-none ring-emerald-300 transition focus:ring"
                  value={form.contactValue}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      contactValue: e.target.value,
                    }))
                  }
                  placeholder="hello@example.com"
                  required
                />
              </label>
            </div>

            <button
              type="submit"
              disabled={isSubmitDisabled}
              className="mt-2 rounded-xl bg-stone-900 px-5 py-3 text-sm font-semibold tracking-wide text-white transition hover:bg-stone-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? "Generating..." : "Generate Draft"}
            </button>

            {error ? (
              <p className="rounded-lg border border-rose-300 bg-rose-50 px-4 py-2 text-sm text-rose-700">
                {error}
              </p>
            ) : null}
          </form>
        </section>

        <section className="rounded-3xl border border-stone-900/15 bg-stone-950 p-6 text-stone-100 shadow-[0_10px_40px_rgba(0,0,0,0.18)] sm:p-8">
          <h2 className="text-xl font-semibold tracking-tight">Preview</h2>
          <p className="mt-2 text-sm text-stone-300">
            Generated content appears here after successful submission.
          </p>

          {!result ? (
            <div className="mt-8 rounded-xl border border-dashed border-stone-700 bg-stone-900/70 p-6 text-sm text-stone-400">
              No draft yet. Submit the form to render sections.
            </div>
          ) : (
            <div className="mt-6 space-y-4">
              <div className="rounded-xl border border-stone-700 bg-stone-900/65 p-4 text-sm">
                <p>
                  <span className="text-stone-400">Page ID:</span>{" "}
                  {result.pageId}
                </p>
                <p>
                  <span className="text-stone-400">Profile ID:</span>{" "}
                  {result.profileId}
                </p>
                <p>
                  <span className="text-stone-400">Status:</span>{" "}
                  {result.status}
                </p>
              </div>

              {sectionOrder.map((sectionKey) => (
                <article
                  key={sectionKey}
                  className="rounded-xl border border-stone-700 bg-stone-900/65 p-4"
                >
                  <h3 className="text-xs font-bold tracking-[0.16em] text-emerald-300">
                    {sectionKey}
                  </h3>
                  <p className="mt-2 whitespace-pre-line text-sm leading-6 text-stone-100">
                    {result.sections[sectionKey] ?? "Missing"}
                  </p>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
