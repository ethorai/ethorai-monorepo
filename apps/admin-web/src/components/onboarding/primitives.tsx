"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";

type ChipProps = {
  selected: boolean;
  onClick: () => void;
  children: ReactNode;
};

export function Chip({ selected, onClick, children }: ChipProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full border px-4 py-2 text-sm font-medium transition ${
        selected
          ? "border-stone-900 bg-stone-900 text-white"
          : "border-stone-300 bg-white/70 text-stone-700 hover:border-stone-500 hover:bg-white"
      }`}
    >
      {children}
    </button>
  );
}

type RadioCardProps = {
  selected: boolean;
  onSelect: () => void;
  title: string;
  description?: string;
};

export function RadioCard({
  selected,
  onSelect,
  title,
  description,
}: RadioCardProps) {
  return (
    <button
      type="button"
      onClick={onSelect}
      className={`w-full rounded-2xl border-2 p-5 text-left transition ${
        selected
          ? "border-stone-900 bg-white shadow-sm"
          : "border-stone-200 bg-white/60 hover:border-stone-400 hover:bg-white"
      }`}
    >
      <div className="flex items-start gap-4">
        <div
          className={`mt-1 flex h-5 w-5 shrink-0 items-center justify-center rounded-full border-2 transition ${
            selected ? "border-stone-900 bg-stone-900" : "border-stone-300"
          }`}
        >
          {selected ? <div className="h-2 w-2 rounded-full bg-white" /> : null}
        </div>
        <div className="flex-1">
          <p className="font-semibold text-stone-900">{title}</p>
          {description ? (
            <p className="mt-1 text-sm leading-relaxed text-stone-600">
              {description}
            </p>
          ) : null}
        </div>
      </div>
    </button>
  );
}

type PrimaryButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
};

export function PrimaryButton({
  children,
  className = "",
  ...rest
}: PrimaryButtonProps) {
  return (
    <button
      {...rest}
      className={`inline-flex items-center justify-center gap-2 rounded-2xl bg-stone-900 px-6 py-3 text-base font-semibold tracking-wide text-white transition hover:bg-stone-700 disabled:cursor-not-allowed disabled:opacity-40 ${className}`}
    >
      {children}
    </button>
  );
}

export function BackArrow() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      <path d="M19 12H5M12 19l-7-7 7-7" />
    </svg>
  );
}

export function ArrowRight() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      <path d="M5 12h14M12 5l7 7-7 7" />
    </svg>
  );
}
