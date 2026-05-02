import type { RoleType, SessionFormat } from "@/lib/api";

export type OnboardingState = {
  fullName: string;
  location: string;
  role: RoleType | null;
  audiences: string[];
  areasOfSupport: string[];
  approach: string;
  sessionFormat: SessionFormat | null;
  expectations: string[];
  contactPhone: string;
  contactEmail: string;
  contactBookingLink: string;
  photoUrl: string;
};

export const INITIAL_ONBOARDING_STATE: OnboardingState = {
  fullName: "",
  location: "",
  role: null,
  audiences: [],
  areasOfSupport: [],
  approach: "",
  sessionFormat: null,
  expectations: [],
  contactPhone: "",
  contactEmail: "",
  contactBookingLink: "",
  photoUrl: "",
};

export type OnboardingSnapshot = {
  state: OnboardingState;
  step: number;
};

const KEY = "ethorai:onboarding";

export function loadOnboarding(): OnboardingSnapshot | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(KEY);
    if (!raw) return null;
    return JSON.parse(raw) as OnboardingSnapshot;
  } catch {
    return null;
  }
}

export function saveOnboarding(snapshot: OnboardingSnapshot): void {
  if (typeof window === "undefined") return;
  try {
    window.localStorage.setItem(KEY, JSON.stringify(snapshot));
  } catch {
    // Ignore quota errors — onboarding still works in-memory.
  }
}

export function clearOnboarding(): void {
  if (typeof window === "undefined") return;
  try {
    window.localStorage.removeItem(KEY);
  } catch {
    // Ignore.
  }
}
