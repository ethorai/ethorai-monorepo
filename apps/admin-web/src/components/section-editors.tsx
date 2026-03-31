import {
  AreasOfSupportData,
  ContactData,
  DisclaimerData,
  FooterData,
  GeneratedSections,
  HeaderData,
  HeroData,
  HowIWorkData,
  SessionFormatItem,
  SessionFormatsData,
  WhatYouCanExpectData,
} from "@/lib/api";

type EditorProps<T> = {
  value: T;
  onChange: (next: T) => void;
};

function TextInput({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="grid gap-2">
      <span className="text-xs font-semibold uppercase tracking-[0.16em] text-stone-500">
        {label}
      </span>
      <input
        className="rounded-xl border border-stone-300 bg-white px-4 py-2.5 text-sm text-stone-800 outline-none ring-emerald-300 transition focus:ring"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function TextArea({
  label,
  value,
  onChange,
  rows = 4,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  rows?: number;
}) {
  return (
    <label className="grid gap-2">
      <span className="text-xs font-semibold uppercase tracking-[0.16em] text-stone-500">
        {label}
      </span>
      <textarea
        className="rounded-xl border border-stone-300 bg-white px-4 py-3 text-sm leading-6 text-stone-800 outline-none ring-emerald-300 transition focus:ring"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={rows}
      />
    </label>
  );
}

function StringListEditor({
  label,
  values,
  onChange,
}: {
  label: string;
  values: string[];
  onChange: (next: string[]) => void;
}) {
  return (
    <label className="grid gap-2">
      <span className="text-xs font-semibold uppercase tracking-[0.16em] text-stone-500">
        {label}
      </span>
      <textarea
        className="rounded-xl border border-stone-300 bg-white px-4 py-3 text-sm leading-6 text-stone-800 outline-none ring-emerald-300 transition focus:ring"
        value={values.join("\n")}
        onChange={(event) =>
          onChange(
            event.target.value
              .split("\n")
              .map((item) => item.trim())
              .filter(Boolean),
          )
        }
        rows={Math.max(4, values.length || 1)}
      />
      <span className="text-xs text-stone-500">Une ligne par élément.</span>
    </label>
  );
}

export function HeaderEditor({ value, onChange }: EditorProps<HeaderData>) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <TextInput
        label="Name"
        value={value.name}
        onChange={(name) => onChange({ ...value, name })}
      />
      <TextInput
        label="Role"
        value={value.role}
        onChange={(role) => onChange({ ...value, role })}
      />
      <TextInput
        label="Location"
        value={value.location}
        onChange={(location) => onChange({ ...value, location })}
      />
      <TextInput
        label="Phone"
        value={value.phone ?? ""}
        onChange={(phone) => onChange({ ...value, phone: phone || null })}
      />
      <TextInput
        label="Email"
        value={value.email ?? ""}
        onChange={(email) => onChange({ ...value, email: email || null })}
      />
    </div>
  );
}

export function HeroEditor({ value, onChange }: EditorProps<HeroData>) {
  return (
    <div className="grid gap-4">
      <TextInput
        label="Heading"
        value={value.heading}
        onChange={(heading) => onChange({ ...value, heading })}
      />
      <TextArea
        label="Subheading"
        value={value.subheading}
        onChange={(subheading) => onChange({ ...value, subheading })}
        rows={3}
      />
    </div>
  );
}

export function AreasOfSupportEditor({
  value,
  onChange,
}: EditorProps<AreasOfSupportData>) {
  return (
    <div className="grid gap-4">
      <TextInput
        label="Title"
        value={value.title}
        onChange={(title) => onChange({ ...value, title })}
      />
      <StringListEditor
        label="Items"
        values={value.items}
        onChange={(items) => onChange({ ...value, items })}
      />
    </div>
  );
}

export function HowIWorkEditor({ value, onChange }: EditorProps<HowIWorkData>) {
  return (
    <div className="grid gap-4">
      <TextInput
        label="Title"
        value={value.title}
        onChange={(title) => onChange({ ...value, title })}
      />
      <TextArea
        label="Description"
        value={value.description}
        onChange={(description) => onChange({ ...value, description })}
        rows={5}
      />
    </div>
  );
}

export function WhatYouCanExpectEditor({
  value,
  onChange,
}: EditorProps<WhatYouCanExpectData>) {
  return (
    <div className="grid gap-4">
      <TextInput
        label="Title"
        value={value.title}
        onChange={(title) => onChange({ ...value, title })}
      />
      <StringListEditor
        label="Statements"
        values={value.statements}
        onChange={(statements) => onChange({ ...value, statements })}
      />
    </div>
  );
}

function formatListToText(values: SessionFormatItem[]): string {
  return values.map((item) => `${item.type} | ${item.details}`).join("\n");
}

function parseFormats(value: string): SessionFormatItem[] {
  return value
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [type, ...rest] = line.split("|");
      return {
        type: type?.trim() ?? "",
        details: rest.join("|").trim(),
      };
    });
}

export function SessionFormatsEditor({
  value,
  onChange,
}: EditorProps<SessionFormatsData>) {
  return (
    <div className="grid gap-4">
      <TextInput
        label="Title"
        value={value.title}
        onChange={(title) => onChange({ ...value, title })}
      />
      <label className="grid gap-2">
        <span className="text-xs font-semibold uppercase tracking-[0.16em] text-stone-500">
          Formats
        </span>
        <textarea
          className="rounded-xl border border-stone-300 bg-white px-4 py-3 text-sm leading-6 text-stone-800 outline-none ring-emerald-300 transition focus:ring"
          value={formatListToText(value.formats)}
          onChange={(event) =>
            onChange({ ...value, formats: parseFormats(event.target.value) })
          }
          rows={Math.max(4, value.formats.length || 1)}
        />
        <span className="text-xs text-stone-500">
          Une ligne par format: TYPE | details
        </span>
      </label>
    </div>
  );
}

export function ContactEditor({ value, onChange }: EditorProps<ContactData>) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <div className="md:col-span-2">
        <TextInput
          label="Title"
          value={value.title}
          onChange={(title) => onChange({ ...value, title })}
        />
      </div>
      <div className="md:col-span-2">
        <TextArea
          label="Description"
          value={value.description}
          onChange={(description) => onChange({ ...value, description })}
          rows={4}
        />
      </div>
      <TextInput
        label="CTA text"
        value={value.cta_text}
        onChange={(cta_text) => onChange({ ...value, cta_text })}
      />
      <TextInput
        label="Phone"
        value={value.phone ?? ""}
        onChange={(phone) => onChange({ ...value, phone: phone || null })}
      />
      <TextInput
        label="Email"
        value={value.email ?? ""}
        onChange={(email) => onChange({ ...value, email: email || null })}
      />
    </div>
  );
}

export function DisclaimerEditor({
  value,
  onChange,
}: EditorProps<DisclaimerData>) {
  return (
    <TextArea
      label="Text"
      value={value.text}
      onChange={(text) => onChange({ text })}
      rows={4}
    />
  );
}

export function FooterEditor({ value, onChange }: EditorProps<FooterData>) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <TextInput
        label="Name"
        value={value.name}
        onChange={(name) => onChange({ ...value, name })}
      />
      <TextInput
        label="Role"
        value={value.role}
        onChange={(role) => onChange({ ...value, role })}
      />
      <TextInput
        label="Location"
        value={value.location}
        onChange={(location) => onChange({ ...value, location })}
      />
      <TextInput
        label="Phone"
        value={value.phone ?? ""}
        onChange={(phone) => onChange({ ...value, phone: phone || null })}
      />
      <TextInput
        label="Email"
        value={value.email ?? ""}
        onChange={(email) => onChange({ ...value, email: email || null })}
      />
    </div>
  );
}

export function renderSectionEditor(
  sectionKey: keyof GeneratedSections,
  value: GeneratedSections[keyof GeneratedSections],
  onChange: (next: GeneratedSections[keyof GeneratedSections]) => void,
) {
  switch (sectionKey) {
    case "HEADER":
      return (
        <HeaderEditor
          value={value as HeaderData}
          onChange={(next) => onChange(next)}
        />
      );
    case "HERO":
      return (
        <HeroEditor
          value={value as HeroData}
          onChange={(next) => onChange(next)}
        />
      );
    case "AREAS_OF_SUPPORT":
      return (
        <AreasOfSupportEditor
          value={value as AreasOfSupportData}
          onChange={(next) => onChange(next)}
        />
      );
    case "HOW_I_WORK":
      return (
        <HowIWorkEditor
          value={value as HowIWorkData}
          onChange={(next) => onChange(next)}
        />
      );
    case "WHAT_YOU_CAN_EXPECT":
      return (
        <WhatYouCanExpectEditor
          value={value as WhatYouCanExpectData}
          onChange={(next) => onChange(next)}
        />
      );
    case "SESSION_FORMATS":
      return (
        <SessionFormatsEditor
          value={value as SessionFormatsData}
          onChange={(next) => onChange(next)}
        />
      );
    case "CONTACT":
      return (
        <ContactEditor
          value={value as ContactData}
          onChange={(next) => onChange(next)}
        />
      );
    case "DISCLAIMER":
      return (
        <DisclaimerEditor
          value={value as DisclaimerData}
          onChange={(next) => onChange(next)}
        />
      );
    case "FOOTER":
      return (
        <FooterEditor
          value={value as FooterData}
          onChange={(next) => onChange(next)}
        />
      );
    default:
      return null;
  }
}
