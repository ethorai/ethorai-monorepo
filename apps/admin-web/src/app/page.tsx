import { redirect } from "next/navigation";

export default function Home() {
  redirect("/dashboard");
}

<main className="min-h-screen bg-[radial-gradient(circle_at_top_left,_#fbe3d3_0%,_#f7f5ee_42%,_#e8f6e8_100%)] px-4 py-10 text-stone-900 sm:px-8">
  <div className="mx-auto flex min-h-[82vh] w-full max-w-6xl flex-col items-start justify-between rounded-3xl border border-stone-900/15 bg-white/85 p-8 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur-sm sm:p-12">
    <div>
      <p className="text-xs font-bold uppercase tracking-[0.24em] text-stone-500">
        AI Therapists Admin
      </p>
      <h1 className="mt-3 max-w-3xl text-4xl font-semibold tracking-tight sm:text-6xl">
        Ethical Landing Drafts, Ready in Minutes
      </h1>
      <p className="mt-5 max-w-2xl text-base leading-7 text-stone-600 sm:text-lg">
        Use the generate workspace to create compliant therapist page drafts
        from your Spring API.
      </p>
    </div>

    <div className="mt-10 flex flex-wrap gap-3">
      <a
        className="inline-flex items-center rounded-xl bg-stone-900 px-6 py-3 text-sm font-semibold tracking-wide text-white transition hover:bg-stone-700"
        href="/generate"
      >
        Open Generate Workspace
      </a>
      <a
        className="inline-flex items-center rounded-xl border border-stone-300 bg-white/70 px-6 py-3 text-sm font-semibold tracking-wide text-stone-800 transition hover:bg-white"
        href="/dashboard"
      >
        Open Dashboard
      </a>
    </div>
  </div>
</main>;
