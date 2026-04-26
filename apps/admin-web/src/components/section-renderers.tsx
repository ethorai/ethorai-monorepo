import {
  AreasOfSupportData,
  ContactData,
  DisclaimerData,
  FooterData,
  HeaderData,
  HeroData,
  HowIWorkData,
  SessionFormatsData,
  WhatYouCanExpectData,
} from "@/lib/api";

// Visual rhythm:
//   header — minimal navigation
//   hero   — warm radial, single Fraunces moment
//   sections alternate bg-white ↔ bg-stone-50/60
//   contact — warm gradient anchor before footer
//   disclaimer — light italic aside, no alarm
//   footer  — soft light, calm professional

export function HeaderSection({ data }: { data: HeaderData }) {
  return (
    <header className="sticky top-0 z-10 border-b border-stone-200 bg-white/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-5xl items-baseline justify-between gap-6 px-6 py-6 sm:px-10">
        <div className="flex items-baseline gap-3">
          <span className="font-serif text-xl font-medium text-stone-900">
            {data.name}
          </span>
          <span className="hidden text-sm text-stone-400 sm:inline">·</span>
          <span className="hidden text-sm text-stone-600 sm:inline">
            {data.role}
          </span>
        </div>
        {data.location ? (
          <p className="text-sm text-stone-500">{data.location}</p>
        ) : null}
      </div>
    </header>
  );
}

export function HeroSection({ data }: { data: HeroData }) {
  return (
    <section className="bg-[radial-gradient(ellipse_at_top,#fbe3d3_0%,#f7f5ee_55%,#f4efe4_100%)]">
      <div className="mx-auto max-w-3xl px-6 py-24 sm:px-10 sm:py-32">
        <h1 className="text-4xl font-medium leading-[1.08] tracking-tight text-stone-900 sm:text-6xl">
          {data.heading}
        </h1>
        <p className="mt-8 max-w-2xl text-lg leading-relaxed text-stone-700 sm:text-xl">
          {data.subheading}
        </p>
      </div>
    </section>
  );
}

export function AreasOfSupportSection({
  data,
}: {
  data: AreasOfSupportData;
}) {
  return (
    <section className="bg-white">
      <div className="mx-auto max-w-4xl px-6 py-20 sm:px-10 sm:py-24">
        <h2 className="text-3xl font-medium tracking-tight text-stone-900 sm:text-4xl">
          {data.title}
        </h2>
        <ul className="mt-12 grid grid-cols-1 gap-5 sm:grid-cols-2">
          {data.items.map((item, i) => (
            <li
              key={i}
              className="rounded-2xl border border-stone-200 bg-stone-50/70 px-6 py-6"
            >
              <h3 className="text-lg font-medium text-stone-900">
                {item.title}
              </h3>
              {item.description ? (
                <p className="mt-2 text-base leading-relaxed text-stone-600">
                  {item.description}
                </p>
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

export function HowIWorkSection({ data }: { data: HowIWorkData }) {
  return (
    <section className="bg-stone-50/60">
      <div className="mx-auto max-w-3xl px-6 py-20 sm:px-10 sm:py-24">
        <h2 className="text-3xl font-medium tracking-tight text-stone-900 sm:text-4xl">
          {data.title}
        </h2>
        <p className="mt-8 text-lg leading-relaxed text-stone-700 sm:text-xl">
          {data.description}
        </p>
      </div>
    </section>
  );
}

export function WhatYouCanExpectSection({
  data,
}: {
  data: WhatYouCanExpectData;
}) {
  return (
    <section className="bg-white">
      <div className="mx-auto max-w-3xl px-6 py-20 sm:px-10 sm:py-24">
        <h2 className="text-3xl font-medium tracking-tight text-stone-900 sm:text-4xl">
          {data.title}
        </h2>
        <ul className="mt-10 space-y-5">
          {data.statements.map((statement, i) => (
            <li
              key={i}
              className="rounded-2xl border border-stone-200 bg-stone-50/70 px-6 py-6"
            >
              <h3 className="text-lg font-medium text-stone-900">
                {statement.title}
              </h3>
              {statement.description ? (
                <p className="mt-2 text-base leading-relaxed text-stone-600">
                  {statement.description}
                </p>
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

export function SessionFormatsSection({ data }: { data: SessionFormatsData }) {
  return (
    <section className="bg-stone-50/60">
      <div className="mx-auto max-w-4xl px-6 py-20 sm:px-10 sm:py-24">
        <h2 className="text-3xl font-medium tracking-tight text-stone-900 sm:text-4xl">
          {data.title}
        </h2>
        <div className="mt-12 grid grid-cols-1 gap-5 sm:grid-cols-2">
          {data.formats.map((format, i) => (
            <div
              key={i}
              className="rounded-3xl border border-stone-200 bg-white p-7 shadow-[0_4px_24px_rgba(0,0,0,0.03)]"
            >
              <h3 className="text-xl font-medium text-stone-900">
                {format.type}
              </h3>
              <p className="mt-3 leading-relaxed text-stone-700">
                {format.details}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export function ContactSection({ data }: { data: ContactData }) {
  const ctaHref = data.email
    ? `mailto:${data.email}`
    : data.phone
      ? `tel:${data.phone}`
      : undefined;

  return (
    <section className="bg-[linear-gradient(180deg,#f4efe4_0%,#fbe3d3_100%)]">
      <div className="mx-auto max-w-3xl px-6 py-20 text-center sm:px-10 sm:py-24">
        <h2 className="text-3xl font-medium tracking-tight text-stone-900 sm:text-4xl">
          {data.title}
        </h2>
        <p className="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-stone-700 sm:text-xl">
          {data.description}
        </p>
        {ctaHref ? (
          <a
            href={ctaHref}
            className="mt-10 inline-flex items-center justify-center rounded-2xl bg-stone-900 px-8 py-4 text-base font-medium text-white transition hover:bg-stone-700"
          >
            {data.cta_text}
          </a>
        ) : (
          <span className="mt-10 inline-flex items-center justify-center rounded-2xl bg-stone-900 px-8 py-4 text-base font-medium text-white">
            {data.cta_text}
          </span>
        )}
        {data.phone || data.email ? (
          <div className="mt-8 flex flex-wrap items-center justify-center gap-x-8 gap-y-2 text-sm text-stone-600">
            {data.email ? (
              <a
                href={`mailto:${data.email}`}
                className="transition hover:text-stone-900"
              >
                {data.email}
              </a>
            ) : null}
            {data.phone ? (
              <a
                href={`tel:${data.phone}`}
                className="transition hover:text-stone-900"
              >
                {data.phone}
              </a>
            ) : null}
          </div>
        ) : null}
      </div>
    </section>
  );
}

export function DisclaimerSection({ data }: { data: DisclaimerData }) {
  return (
    <section className="bg-stone-50">
      <div className="mx-auto max-w-3xl px-6 py-10 sm:px-10">
        <p className="text-sm italic leading-relaxed text-stone-500">
          {data.text}
        </p>
      </div>
    </section>
  );
}

export function FooterSection({ data }: { data: FooterData }) {
  const meta = [data.role, data.location].filter(Boolean).join(" · ");
  return (
    <footer className="border-t border-stone-200 bg-stone-100">
      <div className="mx-auto max-w-5xl px-6 py-10 sm:flex sm:items-baseline sm:justify-between sm:px-10 sm:py-12">
        <div>
          <p className="font-serif text-base font-medium text-stone-900">
            {data.name}
          </p>
          {meta ? <p className="mt-1 text-sm text-stone-500">{meta}</p> : null}
        </div>
        {data.phone || data.email ? (
          <div className="mt-4 flex flex-wrap gap-x-6 gap-y-2 text-sm text-stone-500 sm:mt-0">
            {data.email ? (
              <a
                href={`mailto:${data.email}`}
                className="transition hover:text-stone-900"
              >
                {data.email}
              </a>
            ) : null}
            {data.phone ? (
              <a
                href={`tel:${data.phone}`}
                className="transition hover:text-stone-900"
              >
                {data.phone}
              </a>
            ) : null}
          </div>
        ) : null}
      </div>
    </footer>
  );
}
