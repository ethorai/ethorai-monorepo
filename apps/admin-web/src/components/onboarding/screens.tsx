"use client";

import {
  type ChangeEvent,
  type FormEvent,
  type KeyboardEvent,
  useState,
} from "react";
import type { ContactMethod, RoleType, SessionFormat } from "@/lib/api";
import type { OnboardingState } from "@/lib/onboarding-storage";
import {
  ArrowRight,
  Chip,
  PrimaryButton,
  RadioCard,
} from "./primitives";
import { Question } from "./shell";

type ScreenProps = {
  state: OnboardingState;
  update: (patch: Partial<OnboardingState>) => void;
  onNext: () => void;
};

const INPUT_CLASSES =
  "w-full rounded-2xl border border-stone-300 bg-white/80 px-5 py-3.5 text-base outline-none transition placeholder:text-stone-400 focus:border-stone-900 focus:bg-white";

const ROLE_OPTIONS: {
  value: RoleType;
  label: string;
  description: string;
}[] = [
  {
    value: "PSYCHOLOGIST",
    label: "Psychologue",
    description:
      "Diplômé(e) d'un Master en psychologie, inscrit(e) au registre ADELI.",
  },
  {
    value: "THERAPIST",
    label: "Thérapeute",
    description:
      "Praticien(ne) en psychothérapie, sophrologie, hypnothérapie ou approche similaire.",
  },
  {
    value: "COUNSELOR",
    label: "Conseiller(ère)",
    description:
      "Coach ou accompagnant(e) sur des problématiques spécifiques (couple, transition, bien-être).",
  },
];

const AUDIENCE_OPTIONS = [
  "Adultes",
  "Adolescents",
  "Couples",
  "Familles",
  "Enfants",
];

const AREA_SUGGESTIONS = [
  "Stress",
  "Anxiété",
  "Transitions de vie",
  "Relations difficiles",
  "Estime de soi",
  "Burn-out",
  "Deuil",
  "Sommeil",
  "Émotions difficiles",
];

const APPROACH_SUGGESTIONS = [
  "Thérapie cognitivo-comportementale",
  "Approche intégrative",
  "Centrée sur la personne",
  "EMDR",
  "Psychodynamique",
];

const FORMAT_OPTIONS: {
  value: SessionFormat;
  label: string;
  description: string;
}[] = [
  {
    value: "IN_PERSON",
    label: "En cabinet",
    description: "Vos patients viennent à votre cabinet.",
  },
  {
    value: "ONLINE",
    label: "En visio",
    description: "Vous recevez à distance, par vidéo.",
  },
  {
    value: "BOTH",
    label: "Les deux",
    description: "Cabinet et visio, selon les besoins.",
  },
];

const EXPECTATION_OPTIONS = [
  "Confidentialité",
  "Bienveillance",
  "Respect du rythme",
  "Non-jugement",
  "Collaboration",
  "Écoute attentive",
];

const CONTACT_OPTIONS: {
  value: ContactMethod;
  label: string;
  description: string;
}[] = [
  {
    value: "EMAIL",
    label: "Par email",
    description: "Une adresse mail vers laquelle vos visiteurs vous écrivent.",
  },
  {
    value: "PHONE",
    label: "Par téléphone",
    description: "Un numéro affiché en clair sur votre page.",
  },
  {
    value: "BOOKING_LINK",
    label: "Lien de prise de rendez-vous",
    description: "Un lien Calendly, Doctolib ou équivalent.",
  },
];

function toggleInList(list: string[], item: string): string[] {
  return list.includes(item) ? list.filter((i) => i !== item) : [...list, item];
}

function submitOnEnter(handler: () => void) {
  return (event: KeyboardEvent<HTMLElement>) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      handler();
    }
  };
}

// ─── 0. Welcome ────────────────────────────────────────────────

type WelcomeProps = {
  firstName?: string;
  onNext: () => void;
};

export function WelcomeScreen({ firstName, onNext }: WelcomeProps) {
  const greeting = firstName ? `Bonjour ${firstName}.` : "Bienvenue.";
  return (
    <Question
      title={`${greeting} Créons votre page ensemble.`}
      subtitle="8 questions courtes, environ 3 minutes. Vous pourrez tout modifier après la génération."
      footer={
        <PrimaryButton onClick={onNext}>
          Commencer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <p className="text-sm leading-relaxed text-stone-500">
        Aucune réponse n&rsquo;est définitive. Votre progression est
        sauvegardée automatiquement, vous pouvez fermer cette page et reprendre
        plus tard.
      </p>
    </Question>
  );
}

// ─── 1. Identity ────────────────────────────────────────────────

export function IdentityScreen({ state, update, onNext }: ScreenProps) {
  const nameFilled = state.fullName.trim().length >= 2;
  const canContinue = nameFilled && state.location.trim().length >= 2;

  return (
    <Question
      title="Comment souhaitez-vous apparaître sur votre page ?"
      subtitle="Votre nom complet tel que vos patients le verront."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-5">
        <input
          autoFocus
          className={INPUT_CLASSES}
          placeholder="Marie Dupont"
          value={state.fullName}
          onChange={(e: ChangeEvent<HTMLInputElement>) =>
            update({ fullName: e.target.value })
          }
          onKeyDown={submitOnEnter(() => {
            if (canContinue) onNext();
          })}
        />

        <div
          className={`overflow-hidden transition-all duration-500 ${
            nameFilled ? "max-h-40 opacity-100" : "max-h-0 opacity-0"
          }`}
        >
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-stone-600">
              Et dans quelle ville exercez-vous ?
            </span>
            <input
              className={INPUT_CLASSES}
              placeholder="Paris, Lyon, Bordeaux..."
              value={state.location}
              onChange={(e: ChangeEvent<HTMLInputElement>) =>
                update({ location: e.target.value })
              }
              onKeyDown={submitOnEnter(() => {
                if (canContinue) onNext();
              })}
            />
          </label>
        </div>
      </div>
    </Question>
  );
}

// ─── 2. Role ────────────────────────────────────────────────────

export function RoleScreen({ state, update, onNext }: ScreenProps) {
  return (
    <Question
      title="Quel est votre métier ?"
      subtitle="Cela nous aidera à adapter le ton et les mentions légales de votre page."
      footer={
        <PrimaryButton disabled={!state.role} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-3">
        {ROLE_OPTIONS.map((option) => (
          <RadioCard
            key={option.value}
            selected={state.role === option.value}
            onSelect={() => update({ role: option.value })}
            title={option.label}
            description={option.description}
          />
        ))}
      </div>
    </Question>
  );
}

// ─── 3. Audience ────────────────────────────────────────────────

export function AudienceScreen({ state, update, onNext }: ScreenProps) {
  const canContinue = state.audiences.length > 0;
  return (
    <Question
      title="Qui consultez-vous ?"
      subtitle="Sélectionnez tous les profils qui correspondent à votre pratique."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="flex flex-wrap gap-2.5">
        {AUDIENCE_OPTIONS.map((option) => (
          <Chip
            key={option}
            selected={state.audiences.includes(option)}
            onClick={() =>
              update({ audiences: toggleInList(state.audiences, option) })
            }
          >
            {option}
          </Chip>
        ))}
      </div>
    </Question>
  );
}

// ─── 4. Areas of Support ────────────────────────────────────────

export function AreasScreen({ state, update, onNext }: ScreenProps) {
  const [custom, setCustom] = useState("");
  const canContinue = state.areasOfSupport.length > 0;

  function addCustom(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = custom.trim();
    if (!trimmed || state.areasOfSupport.includes(trimmed)) {
      setCustom("");
      return;
    }
    update({ areasOfSupport: [...state.areasOfSupport, trimmed] });
    setCustom("");
  }

  const customSelections = state.areasOfSupport.filter(
    (item) => !AREA_SUGGESTIONS.includes(item),
  );

  return (
    <Question
      title="Sur quels sujets vos patients viennent-ils vous voir ?"
      subtitle="Choisissez les plus fréquents. Vous pouvez en ajouter qui ne sont pas dans la liste."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-6">
        <div className="flex flex-wrap gap-2.5">
          {AREA_SUGGESTIONS.map((option) => (
            <Chip
              key={option}
              selected={state.areasOfSupport.includes(option)}
              onClick={() =>
                update({
                  areasOfSupport: toggleInList(state.areasOfSupport, option),
                })
              }
            >
              {option}
            </Chip>
          ))}
          {customSelections.map((option) => (
            <Chip
              key={option}
              selected
              onClick={() =>
                update({
                  areasOfSupport: state.areasOfSupport.filter(
                    (i) => i !== option,
                  ),
                })
              }
            >
              {option} ×
            </Chip>
          ))}
        </div>

        <form onSubmit={addCustom} className="flex gap-2">
          <input
            className={INPUT_CLASSES}
            placeholder="Ajouter un sujet..."
            value={custom}
            onChange={(e) => setCustom(e.target.value)}
          />
          <button
            type="submit"
            disabled={!custom.trim()}
            className="shrink-0 rounded-2xl border border-stone-300 bg-white/80 px-5 text-sm font-semibold text-stone-700 transition hover:bg-white disabled:cursor-not-allowed disabled:opacity-40"
          >
            Ajouter
          </button>
        </form>
      </div>
    </Question>
  );
}

// ─── 5. Approach ────────────────────────────────────────────────

export function ApproachScreen({ state, update, onNext }: ScreenProps) {
  const length = state.approach.trim().length;
  const canContinue = length >= 30;

  function appendSuggestion(text: string) {
    const current = state.approach.trim();
    const next = current ? `${current}. ${text}` : text;
    update({ approach: next });
  }

  return (
    <Question
      title="Comment travaillez-vous avec vos patients ?"
      subtitle="Quelques mots sur votre approche thérapeutique. Aucun jargon obligatoire."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-4">
        <textarea
          autoFocus
          rows={5}
          className={`${INPUT_CLASSES} resize-none`}
          placeholder="Je travaille en approche intégrative, en mêlant écoute active et techniques cognitives..."
          value={state.approach}
          onChange={(e) => update({ approach: e.target.value })}
        />

        <div className="flex items-center justify-between text-xs text-stone-500">
          <span>Inspirations rapides :</span>
          <span>{length} / 300</span>
        </div>

        <div className="flex flex-wrap gap-2">
          {APPROACH_SUGGESTIONS.map((suggestion) => (
            <button
              key={suggestion}
              type="button"
              onClick={() => appendSuggestion(suggestion)}
              className="rounded-full border border-dashed border-stone-400 bg-white/40 px-3 py-1.5 text-xs text-stone-600 transition hover:border-stone-600 hover:bg-white"
            >
              + {suggestion}
            </button>
          ))}
        </div>
      </div>
    </Question>
  );
}

// ─── 6. Format ──────────────────────────────────────────────────

export function FormatScreen({ state, update, onNext }: ScreenProps) {
  return (
    <Question
      title="Comment recevez-vous vos patients ?"
      subtitle="Vous pourrez préciser plus tard si certains formats demandent des conditions particulières."
      footer={
        <PrimaryButton disabled={!state.sessionFormat} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-3">
        {FORMAT_OPTIONS.map((option) => (
          <RadioCard
            key={option.value}
            selected={state.sessionFormat === option.value}
            onSelect={() => update({ sessionFormat: option.value })}
            title={option.label}
            description={option.description}
          />
        ))}
      </div>
    </Question>
  );
}

// ─── 7. Expectations ────────────────────────────────────────────

export function ExpectationsScreen({ state, update, onNext }: ScreenProps) {
  const canContinue = state.expectations.length >= 2;
  return (
    <Question
      title="Qu'est-ce qui compte pour vous dans votre pratique ?"
      subtitle="Ces valeurs apparaîtront sur votre page pour rassurer vos visiteurs. Choisissez-en au moins deux."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="flex flex-wrap gap-2.5">
        {EXPECTATION_OPTIONS.map((option) => (
          <Chip
            key={option}
            selected={state.expectations.includes(option)}
            onClick={() =>
              update({
                expectations: toggleInList(state.expectations, option),
              })
            }
          >
            {option}
          </Chip>
        ))}
      </div>
    </Question>
  );
}

// ─── 8. Contact ─────────────────────────────────────────────────

export function ContactScreen({ state, update, onNext }: ScreenProps) {
  const placeholder =
    state.contactMethod === "EMAIL"
      ? "marie.dupont@exemple.fr"
      : state.contactMethod === "PHONE"
        ? "06 12 34 56 78"
        : "https://calendly.com/votre-cabinet";
  const inputType =
    state.contactMethod === "EMAIL"
      ? "email"
      : state.contactMethod === "PHONE"
        ? "tel"
        : "url";

  const canContinue =
    !!state.contactMethod && state.contactValue.trim().length > 3;

  return (
    <Question
      title="Comment souhaitez-vous que les visiteurs vous contactent ?"
      subtitle="Une seule méthode pour ne pas disperser l'attention de vos visiteurs."
      footer={
        <PrimaryButton disabled={!canContinue} onClick={onNext}>
          Continuer
          <ArrowRight />
        </PrimaryButton>
      }
    >
      <div className="space-y-4">
        <div className="space-y-3">
          {CONTACT_OPTIONS.map((option) => (
            <RadioCard
              key={option.value}
              selected={state.contactMethod === option.value}
              onSelect={() =>
                update({ contactMethod: option.value, contactValue: "" })
              }
              title={option.label}
              description={option.description}
            />
          ))}
        </div>

        <div
          className={`overflow-hidden transition-all duration-500 ${
            state.contactMethod ? "max-h-40 opacity-100" : "max-h-0 opacity-0"
          }`}
        >
          <input
            type={inputType}
            className={INPUT_CLASSES}
            placeholder={placeholder}
            value={state.contactValue}
            onChange={(e) => update({ contactValue: e.target.value })}
            onKeyDown={submitOnEnter(() => {
              if (canContinue) onNext();
            })}
          />
        </div>
      </div>
    </Question>
  );
}

// ─── 9. Summary ─────────────────────────────────────────────────

type SummaryProps = {
  state: OnboardingState;
  onEdit: (step: number) => void;
  onGenerate: () => void;
  generating: boolean;
  error: string;
};

const SUMMARY_ROWS: {
  label: string;
  step: number;
  pick: (s: OnboardingState) => string;
}[] = [
  {
    label: "Identité",
    step: 1,
    pick: (s) =>
      [s.fullName, s.location].filter(Boolean).join(" · ") || "—",
  },
  {
    label: "Métier",
    step: 2,
    pick: (s) =>
      s.role === "PSYCHOLOGIST"
        ? "Psychologue"
        : s.role === "THERAPIST"
          ? "Thérapeute"
          : s.role === "COUNSELOR"
            ? "Conseiller(ère)"
            : "—",
  },
  {
    label: "Public",
    step: 3,
    pick: (s) => s.audiences.join(", ") || "—",
  },
  {
    label: "Sujets",
    step: 4,
    pick: (s) => s.areasOfSupport.join(", ") || "—",
  },
  {
    label: "Approche",
    step: 5,
    pick: (s) => s.approach || "—",
  },
  {
    label: "Format",
    step: 6,
    pick: (s) =>
      s.sessionFormat === "IN_PERSON"
        ? "En cabinet"
        : s.sessionFormat === "ONLINE"
          ? "En visio"
          : s.sessionFormat === "BOTH"
            ? "Les deux"
            : "—",
  },
  {
    label: "Valeurs",
    step: 7,
    pick: (s) => s.expectations.join(", ") || "—",
  },
  {
    label: "Contact",
    step: 8,
    pick: (s) =>
      s.contactMethod && s.contactValue
        ? `${
            s.contactMethod === "EMAIL"
              ? "Email"
              : s.contactMethod === "PHONE"
                ? "Téléphone"
                : "Lien"
          } — ${s.contactValue}`
        : "—",
  },
];

export function SummaryScreen({
  state,
  onEdit,
  onGenerate,
  generating,
  error,
}: SummaryProps) {
  return (
    <Question
      title={generating ? "Génération en cours..." : "Tout est prêt."}
      subtitle={
        generating
          ? "Cela prend environ 30 secondes. Merci de patienter."
          : "Relisez vos réponses, modifiez si besoin, puis lancez la génération."
      }
      footer={
        !generating ? (
          <PrimaryButton onClick={onGenerate}>
            Générer ma page
            <ArrowRight />
          </PrimaryButton>
        ) : undefined
      }
    >
      {generating ? (
        <div className="flex items-center justify-center py-12">
          <div className="h-10 w-10 animate-spin rounded-full border-2 border-stone-300 border-t-stone-900" />
        </div>
      ) : (
        <div className="space-y-3">
          {SUMMARY_ROWS.map((row) => (
            <button
              key={row.label}
              type="button"
              onClick={() => onEdit(row.step)}
              className="group flex w-full items-start gap-4 rounded-2xl border border-stone-200 bg-white/70 p-4 text-left transition hover:border-stone-400 hover:bg-white"
            >
              <span className="w-24 shrink-0 text-xs font-semibold uppercase tracking-[0.18em] text-stone-500">
                {row.label}
              </span>
              <span className="flex-1 text-sm text-stone-800">
                {row.pick(state)}
              </span>
              <span className="shrink-0 text-xs text-stone-400 opacity-0 transition group-hover:opacity-100">
                Modifier
              </span>
            </button>
          ))}

          {error ? (
            <p className="rounded-2xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {error}
            </p>
          ) : null}
        </div>
      )}
    </Question>
  );
}
