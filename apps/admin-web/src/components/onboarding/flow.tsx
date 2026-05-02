"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import {
  generateLandingPage,
  getGenerationStatus,
  type TherapistInput,
} from "@/lib/api";
import {
  clearOnboarding,
  INITIAL_ONBOARDING_STATE,
  loadOnboarding,
  saveOnboarding,
  type OnboardingSnapshot,
  type OnboardingState,
} from "@/lib/onboarding-storage";
import { Shell } from "./shell";
import {
  ApproachScreen,
  AreasScreen,
  AudienceScreen,
  ContactScreen,
  ExpectationsScreen,
  FormatScreen,
  IdentityScreen,
  PhotoScreen,
  RoleScreen,
  SummaryScreen,
  WelcomeScreen,
} from "./screens";

const TOTAL_INPUT_STEPS = 9;
const SUMMARY_STEP = 10;
const POLL_INTERVAL_MS = 2000;

type FlowProps = {
  firstName?: string;
};

// Two-stage component: outer reads localStorage on mount, inner takes
// the resolved snapshot as initial state. This avoids setState-in-effect
// while keeping hydration SSR-safe.
export function OnboardingFlow({ firstName }: FlowProps) {
  const [snapshot, setSnapshot] = useState<OnboardingSnapshot | null>(null);

  useEffect(() => {
    // One-shot hydration from localStorage post-mount. Required because
    // localStorage is unavailable during SSR; this runs exactly once.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setSnapshot(
      loadOnboarding() ?? { state: INITIAL_ONBOARDING_STATE, step: 0 },
    );
  }, []);

  if (!snapshot) return null;

  return (
    <FlowInner
      firstName={firstName}
      initialState={snapshot.state}
      initialStep={snapshot.step}
    />
  );
}

type InnerProps = {
  firstName?: string;
  initialState: OnboardingState;
  initialStep: number;
};

function FlowInner({ firstName, initialState, initialStep }: InnerProps) {
  const router = useRouter();
  const [state, setState] = useState<OnboardingState>(initialState);
  const [step, setStep] = useState(initialStep);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    saveOnboarding({ state, step });
  }, [state, step]);

  useEffect(() => {
    return () => {
      if (pollingRef.current) clearInterval(pollingRef.current);
    };
  }, []);

  function update(patch: Partial<OnboardingState>) {
    setState((prev) => ({ ...prev, ...patch }));
  }

  function next() {
    setStep((s) => Math.min(s + 1, SUMMARY_STEP));
  }

  function back() {
    setStep((s) => Math.max(s - 1, 0));
    setError("");
  }

  function goTo(target: number) {
    setStep(target);
    setError("");
  }

  async function generate() {
    if (!state.role || !state.sessionFormat) {
      setError("Certaines réponses sont manquantes.");
      return;
    }
    if (!state.contactPhone && !state.contactEmail && !state.contactBookingLink) {
      setError("Veuillez renseigner au moins un moyen de contact.");
      return;
    }

    setError("");
    setGenerating(true);

    const payload: TherapistInput = {
      fullName: state.fullName.trim(),
      role: state.role,
      location: state.location.trim(),
      audiences: state.audiences,
      areasOfSupport: state.areasOfSupport,
      approach: state.approach.trim(),
      sessionFormat: state.sessionFormat,
      expectations: state.expectations,
      phone: state.contactPhone.trim() || null,
      email: state.contactEmail.trim() || null,
      bookingLink: state.contactBookingLink.trim() || null,
      photoUrl: state.photoUrl.trim() || null,
    };

    try {
      const job = await generateLandingPage(payload);

      pollingRef.current = setInterval(async () => {
        try {
          const status = await getGenerationStatus(job.jobId);
          if (status.status === "COMPLETED" && status.pageId) {
            if (pollingRef.current) clearInterval(pollingRef.current);
            clearOnboarding();
            router.push("/page");
          } else if (status.status === "FAILED") {
            if (pollingRef.current) clearInterval(pollingRef.current);
            setError(status.error ?? "La génération a échoué.");
            setGenerating(false);
          }
        } catch {
          if (pollingRef.current) clearInterval(pollingRef.current);
          setError("Impossible de vérifier le statut de la génération.");
          setGenerating(false);
        }
      }, POLL_INTERVAL_MS);
    } catch (submitError) {
      const message =
        submitError instanceof Error
          ? submitError.message
          : "La génération a échoué.";
      setError(message);
      setGenerating(false);
    }
  }

  const showBack = step > 0 && !generating;

  return (
    <Shell
      step={step}
      totalSteps={TOTAL_INPUT_STEPS}
      onBack={showBack ? back : undefined}
    >
      {step === 0 ? (
        <WelcomeScreen firstName={firstName} onNext={next} />
      ) : null}
      {step === 1 ? (
        <IdentityScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 2 ? (
        <RoleScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 3 ? (
        <AudienceScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 4 ? (
        <AreasScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 5 ? (
        <ApproachScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 6 ? (
        <FormatScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 7 ? (
        <ExpectationsScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 8 ? (
        <ContactScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === 9 ? (
        <PhotoScreen state={state} update={update} onNext={next} />
      ) : null}
      {step === SUMMARY_STEP ? (
        <SummaryScreen
          state={state}
          onEdit={goTo}
          onGenerate={generate}
          generating={generating}
          error={error}
        />
      ) : null}
    </Shell>
  );
}
