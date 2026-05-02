import { notFound } from "next/navigation";
import type { GeneratedPageResponse } from "@/lib/api";
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

export const revalidate = 3600;

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";

async function fetchPublishedPage(
  id: string,
): Promise<GeneratedPageResponse | null> {
  const response = await fetch(`${API_BASE_URL}/api/public/pages/${id}`, {
    next: { revalidate: 3600 },
  });
  if (!response.ok) return null;
  return response.json() as Promise<GeneratedPageResponse>;
}

type Props = { params: Promise<{ id: string }> };

export async function generateMetadata({ params }: Props) {
  const { id } = await params;
  const page = await fetchPublishedPage(id);
  if (!page) return {};
  return {
    title: `${page.fullName} — Therapist`,
    description: page.sections.HERO?.subheading ?? "",
  };
}

export default async function PublicPage({ params }: Props) {
  const { id } = await params;
  const page = await fetchPublishedPage(id);
  if (!page) notFound();

  const s = page.sections;

  return (
    <main>
      <HeaderSection data={s.HEADER} />
      <HeroSection data={s.HERO} photoUrl={page.photoUrl} />
      <AreasOfSupportSection data={s.AREAS_OF_SUPPORT} />
      <HowIWorkSection data={s.HOW_I_WORK} />
      <WhatYouCanExpectSection data={s.WHAT_YOU_CAN_EXPECT} />
      <SessionFormatsSection data={s.SESSION_FORMATS} />
      <ContactSection data={s.CONTACT} />
      <DisclaimerSection data={s.DISCLAIMER} />
      <FooterSection data={s.FOOTER} />
    </main>
  );
}
