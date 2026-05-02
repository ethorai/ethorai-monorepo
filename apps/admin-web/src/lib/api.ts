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

export type AreaOfSupportItem = {
  title: string;
  description: string;
};

export type AreasOfSupportData = {
  title: string;
  items: AreaOfSupportItem[];
};

export type HowIWorkData = {
  title: string;
  description: string;
};

export type ExpectationStatement = {
  title: string;
  description: string;
};

export type WhatYouCanExpectData = {
  title: string;
  statements: ExpectationStatement[];
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
  booking_link: string | null;
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
  phone: string | null;
  email: string | null;
  bookingLink: string | null;
};

export type GeneratedPageResponse = {
  pageId: string;
  profileId: string;
  fullName: string;
  role: RoleType;
  sections: GeneratedSections;
  status: PageStatus;
};

export type GenerationJobStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "FAILED";

export type GenerationJobResponse = {
  jobId: string;
  profileId: string;
  pageId: string | null;
  status: GenerationJobStatus;
  error: string | null;
  createdAt: string;
  updatedAt: string;
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
): Promise<GenerationJobResponse> {
  const response = await fetch("/api/generate", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  return parseApiResponse<GenerationJobResponse>(response);
}

export async function getGenerationStatus(
  jobId: string,
): Promise<GenerationJobResponse> {
  const response = await fetch(`/api/generate/status/${jobId}`, {
    method: "GET",
    cache: "no-store",
  });

  return parseApiResponse<GenerationJobResponse>(response);
}

export async function getPage(pageId: string): Promise<GeneratedPageResponse> {
  const response = await fetch(`/api/pages/${pageId}`, {
    method: "GET",
    cache: "no-store",
  });

  return parseApiResponse<GeneratedPageResponse>(response);
}

export async function getMyPage(): Promise<GeneratedPageResponse | null> {
  const response = await fetch(`/api/me/page`, {
    method: "GET",
    cache: "no-store",
  });
  if (response.status === 204) return null;
  return parseApiResponse<GeneratedPageResponse>(response);
}

export async function getMyProfile(): Promise<TherapistInput | null> {
  const response = await fetch(`/api/me/profile`, {
    method: "GET",
    cache: "no-store",
  });
  if (response.status === 204) return null;
  return parseApiResponse<TherapistInput>(response);
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
