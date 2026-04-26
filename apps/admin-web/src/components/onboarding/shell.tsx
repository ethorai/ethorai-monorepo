"use client";

import type { ReactNode } from "react";
import { BackArrow } from "./primitives";

type ShellProps = {
  step: number;
  totalSteps: number;
  onBack?: () => void;
  children: ReactNode;
};

export function Shell({ step, totalSteps, onBack, children }: ShellProps) {
  const isInputStep = step >= 1 && step <= totalSteps;
  const progress = isInputStep
    ? (step / totalSteps) * 100
    : step > totalSteps
      ? 100
      : 0;

  return (
    <main className="flex min-h-screen flex-col bg-[radial-gradient(circle_at_top_left,_#fbe3d3_0%,_#f7f5ee_42%,_#e8f6e8_100%)] text-stone-900">
      <header className="px-6 pt-6 sm:px-10 sm:pt-8">
        <div className="mx-auto flex max-w-2xl items-center gap-6">
          <div className="w-10">
            {onBack ? (
              <button
                type="button"
                onClick={onBack}
                aria-label="Retour"
                className="inline-flex h-10 w-10 items-center justify-center rounded-full text-stone-500 transition hover:bg-white/60 hover:text-stone-900"
              >
                <BackArrow />
              </button>
            ) : null}
          </div>

          <div className="flex flex-1 items-center gap-3">
            {isInputStep ? (
              <>
                <span className="text-xs font-semibold uppercase tracking-[0.2em] text-stone-500">
                  Étape {step} / {totalSteps}
                </span>
                <div className="h-0.5 flex-1 overflow-hidden rounded-full bg-stone-300/60">
                  <div
                    className="h-full bg-stone-900 transition-all duration-500 ease-out"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </>
            ) : null}
          </div>
        </div>
      </header>

      <div className="flex flex-1 items-start px-6 py-10 sm:px-10 sm:py-16">
        <div className="mx-auto w-full max-w-xl">{children}</div>
      </div>
    </main>
  );
}

type QuestionProps = {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
};

export function Question({ title, subtitle, children, footer }: QuestionProps) {
  return (
    <div className="flex flex-col gap-10">
      <div>
        <h1 className="text-3xl font-semibold leading-tight tracking-tight sm:text-4xl">
          {title}
        </h1>
        {subtitle ? (
          <p className="mt-3 text-base leading-relaxed text-stone-600 sm:text-lg">
            {subtitle}
          </p>
        ) : null}
      </div>

      <div>{children}</div>

      {footer ? <div className="flex items-center justify-end gap-3">{footer}</div> : null}
    </div>
  );
}
