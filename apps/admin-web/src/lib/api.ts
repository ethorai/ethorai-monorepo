export type RoleType = "PSYCHOLOGIST" | "THERAPIST" | "COUNSELOR";
export type SessionFormat = "ONLINE" | "IN_PERSON" | "BOTH";
export type ContactMethod = "EMAIL" | "PHONE" | "BOOKING_LINK";
export type PageStatus = "DRAFT" | "PUBLISHED";
export type SectionType =
  | "HEADER"
  | "HERO"
  | "AREAS_OF_SUPPORT"
  | "HOW_I_WORK"
  | "WHAT_YOU_CAN_EXPECT"
  | "SESSION_FORMATS"
  | "CONTACT"
  | "DISCLAIMER"
  | "FOOTER";

// Structured section types
export type HeaderData = {
  name: string;
  role: string;
  location: string;
  phone: string | null;
  email: string | null;
};

export type HeroData = {
  heading: string;
  subheading: string;
};

export type AreasOfSupportData = {
  title: string;
  items: string[];
};

export type HowIWorkData = {
  title: string;
  description: string;
};

export type WhatYouCanExpectData = {
  title: string;
  statements: string[];
};

export type SessionFormatItem = {
  type: string;
  details: string;
};

export type SessionFormatsData = {
  title: string;
  formats: SessionFormatItem[];
};

export type ContactData = {
  title: string;
  description: string;
  cta_text: string;
  phone: string | null;
  email: string | null;
};

export type DisclaimerData = {
  text: string;
};

export type FooterData = {
  name: string;
  role: string;
  location: string;
  phone: string | null;
  email: string | null;
};

// Parsed sections
export type GeneratedSections = {
  HEADER: HeaderData;
  HERO: HeroData;
  AREAS_OF_SUPPORT: AreasOfSupportData;
  HOW_I_WORK: HowIWorkData;
  WHAT_YOU_CAN_EXPECT: WhatYouCanExpectData;
  SESSION_FORMATS: SessionFormatsData;
  CONTACT: ContactData;
  DISCLAIMER: DisclaimerData;
  FOOTER: FooterData;
};

export type ApiErrorResponse = {
  code: string;
  message: string;
  timestamp?: string;
};

export type TherapistInput = {
  fullName: string;
  role: RoleType;
  location: string;
  audiences: string[];
  areasOfSupport: string[];
  approach: string;
  sessionFormat: SessionFormat;
  expectations: string[];
  contactMethod: ContactMethod;
  contactValue: string;
};

export type GeneratedPageResponse = {
  pageId: string;
  profileId: string;
  fullName: string;
  role: RoleType;
  sections: GeneratedSections;
  status: PageStatus;
};

async function parseApiResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = "Request failed";
    try {
      const errorPayload = (await response.json()) as Partial<ApiErrorResponse>;
      message = errorPayload.message ?? message;
    } catch {
      // Preserve generic fallback if backend does not return structured JSON.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function generateLandingPage(
  payload: TherapistInput,
): Promise<GeneratedPageResponse> {
  const response = await fetch("/api/generate", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  return parseApiResponse<GeneratedPageResponse>(response);
}

export async function getPage(pageId: string): Promise<GeneratedPageResponse> {
  const response = await fetch(`/api/pages/${pageId}`, {
    method: "GET",
    cache: "no-store",
  });

  return parseApiResponse<GeneratedPageResponse>(response);
}

export async function getPagesByProfile(
  profileId: string,
): Promise<GeneratedPageResponse[]> {
  const params = new URLSearchParams({ profileId });
  const response = await fetch(`/api/pages?${params.toString()}`, {
    method: "GET",
    cache: "no-store",
  });

  return parseApiResponse<GeneratedPageResponse[]>(response);
}

export async function updatePageSection(
  pageId: string,
  sectionType: SectionType,
  content: GeneratedSections[SectionType],
): Promise<void> {
  const response = await fetch(`/api/pages/${pageId}/sections/${sectionType}`, {
    method: "PUT",
    headers: {
      "Content-Type": "text/plain",
    },
    body: JSON.stringify(content),
  });

  return parseApiResponse<void>(response);
}

export async function publishPage(pageId: string): Promise<void> {
  const response = await fetch(`/api/pages/${pageId}/publish`, {
    method: "POST",
  });

  return parseApiResponse<void>(response);
}

export async function regenerateSection(
  pageId: string,
  sectionType: SectionType,
): Promise<GeneratedPageResponse> {
  const response = await fetch(
    `/api/pages/${pageId}/sections/${sectionType}/regenerate`,
    { method: "POST" },
  );

  return parseApiResponse<GeneratedPageResponse>(response);
}
