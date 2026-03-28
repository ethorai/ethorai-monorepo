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

// Sections are stored as JSON strings, need to be parsed
export type RawGeneratedSections = Record<SectionType, string>;

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
  sections: RawGeneratedSections;
  status: PageStatus;
};

function stripHtml(value: string): string {
  return value
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function splitTextItems(value: string): string[] {
  const plain = stripHtml(value);
  return plain
    .split(/,|\n|•|·/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function tryParseObject<T>(value: string): T | null {
  try {
    return JSON.parse(value) as T;
  } catch {
    return null;
  }
}

// Helper to parse raw JSON string sections into typed sections
export function parseSections(raw: RawGeneratedSections): GeneratedSections {
  const header = tryParseObject<HeaderData>(raw.HEADER);
  const hero = tryParseObject<HeroData>(raw.HERO);
  const areas = tryParseObject<AreasOfSupportData>(raw.AREAS_OF_SUPPORT);
  const howIWork = tryParseObject<HowIWorkData>(raw.HOW_I_WORK);
  const whatYouCanExpect = tryParseObject<WhatYouCanExpectData>(
    raw.WHAT_YOU_CAN_EXPECT,
  );
  const sessionFormats = tryParseObject<SessionFormatsData>(
    raw.SESSION_FORMATS,
  );
  const contact = tryParseObject<ContactData>(raw.CONTACT);
  const disclaimer = tryParseObject<DisclaimerData>(raw.DISCLAIMER);
  const footer = tryParseObject<FooterData>(raw.FOOTER);

  return {
    HEADER: header ?? {
      name: stripHtml(raw.HEADER),
      role: "",
      location: "",
      phone: null,
      email: null,
    },
    HERO: hero ?? {
      heading: stripHtml(raw.HERO),
      subheading: "",
    },
    AREAS_OF_SUPPORT: areas ?? {
      title: "Areas of support",
      items: splitTextItems(raw.AREAS_OF_SUPPORT),
    },
    HOW_I_WORK: howIWork ?? {
      title: "How I work",
      description: stripHtml(raw.HOW_I_WORK),
    },
    WHAT_YOU_CAN_EXPECT: whatYouCanExpect ?? {
      title: "What you can expect",
      statements: splitTextItems(raw.WHAT_YOU_CAN_EXPECT),
    },
    SESSION_FORMATS: sessionFormats ?? {
      title: "Session formats",
      formats: [
        {
          type: stripHtml(raw.SESSION_FORMATS),
          details: "",
        },
      ],
    },
    CONTACT: contact ?? {
      title: "Contact",
      description: stripHtml(raw.CONTACT),
      cta_text: "Contact me",
      phone: raw.CONTACT.includes("@") ? null : stripHtml(raw.CONTACT),
      email: raw.CONTACT.includes("@") ? stripHtml(raw.CONTACT) : null,
    },
    DISCLAIMER: disclaimer ?? {
      text: stripHtml(raw.DISCLAIMER),
    },
    FOOTER: footer ?? {
      name: stripHtml(raw.FOOTER),
      role: "",
      location: "",
      phone: null,
      email: null,
    },
  };
}

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
  content: string,
): Promise<void> {
  const response = await fetch(`/api/pages/${pageId}/sections/${sectionType}`, {
    method: "PUT",
    headers: {
      "Content-Type": "text/plain",
    },
    body: content,
  });

  return parseApiResponse<void>(response);
}

export async function publishPage(pageId: string): Promise<void> {
  const response = await fetch(`/api/pages/${pageId}/publish`, {
    method: "POST",
  });

  return parseApiResponse<void>(response);
}
