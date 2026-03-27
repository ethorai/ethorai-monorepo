export type RoleType = "PSYCHOLOGIST" | "THERAPIST" | "COUNSELOR";
export type SessionFormat = "ONLINE" | "IN_PERSON" | "BOTH";
export type ContactMethod = "EMAIL" | "PHONE" | "BOOKING_LINK";

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
  sections: Record<string, string>;
  status: "DRAFT" | "PUBLISHED";
};

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

  if (!response.ok) {
    let message = "Generation failed";
    try {
      const errorPayload = await response.json();
      message = errorPayload?.message ?? message;
    } catch {
      // Keep fallback message when backend doesn't return JSON.
    }
    throw new Error(message);
  }

  return (await response.json()) as GeneratedPageResponse;
}
