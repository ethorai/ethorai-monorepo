import React from "react";
import {
  HeaderData,
  HeroData,
  AreasOfSupportData,
  HowIWorkData,
  WhatYouCanExpectData,
  SessionFormatsData,
  ContactData,
  DisclaimerData,
  FooterData,
} from "@/lib/api";

export function HeaderSection({ data }: { data: HeaderData }) {
  return (
    <header className="bg-gray-50 border-b border-gray-200 px-6 py-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900">{data.name}</h1>
        <p className="text-lg text-gray-600 mt-1">{data.role}</p>
        {data.location && (
          <p className="text-sm text-gray-500 mt-2">{data.location}</p>
        )}
        {(data.phone || data.email) && (
          <div className="mt-4 flex gap-6 text-sm">
            {data.phone && <span className="text-gray-600">{data.phone}</span>}
            {data.email && <span className="text-gray-600">{data.email}</span>}
          </div>
        )}
      </div>
    </header>
  );
}

export function HeroSection({ data }: { data: HeroData }) {
  return (
    <section className="bg-blue-50 px-6 py-16">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-4xl font-bold text-gray-900">{data.heading}</h2>
        <p className="text-xl text-gray-700 mt-4">{data.subheading}</p>
      </div>
    </section>
  );
}

export function AreasOfSupportSection({ data }: { data: AreasOfSupportData }) {
  return (
    <section className="px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold text-gray-900 mb-8">{data.title}</h2>
        <ul className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {data.items.map((item, i) => (
            <li key={i} className="flex items-start gap-3 text-gray-700">
              <span className="text-blue-600 font-bold mt-1">•</span>
              <span>{item}</span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

export function HowIWorkSection({ data }: { data: HowIWorkData }) {
  return (
    <section className="bg-gray-50 px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">{data.title}</h2>
        <p className="text-gray-700 text-lg leading-relaxed">
          {data.description}
        </p>
      </div>
    </section>
  );
}

export function WhatYouCanExpectSection({
  data,
}: {
  data: WhatYouCanExpectData;
}) {
  return (
    <section className="px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold text-gray-900 mb-8">{data.title}</h2>
        <div className="space-y-4">
          {data.statements.map((statement, i) => (
            <div
              key={i}
              className="p-4 bg-blue-50 border-l-4 border-blue-600 rounded"
            >
              <p className="text-gray-800">{statement}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export function SessionFormatsSection({ data }: { data: SessionFormatsData }) {
  return (
    <section className="bg-gray-50 px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold text-gray-900 mb-8">{data.title}</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {data.formats.map((format, i) => (
            <div
              key={i}
              className="bg-white p-6 rounded-lg border border-gray-200"
            >
              <h3 className="font-semibold text-gray-900 mb-2">
                {format.type}
              </h3>
              <p className="text-gray-700">{format.details}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export function ContactSection({ data }: { data: ContactData }) {
  return (
    <section className="bg-blue-600 text-white px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">{data.title}</h2>
        <p className="text-lg mb-8 opacity-90">{data.description}</p>
        <button className="bg-white text-blue-600 font-semibold py-3 px-8 rounded hover:bg-gray-100 transition">
          {data.cta_text}
        </button>
        {(data.phone || data.email) && (
          <div className="mt-8 flex gap-6 text-sm opacity-90">
            {data.phone && <span>{data.phone}</span>}
            {data.email && <span>{data.email}</span>}
          </div>
        )}
      </div>
    </section>
  );
}

export function DisclaimerSection({ data }: { data: DisclaimerData }) {
  return (
    <section className="bg-amber-50 border-t border-amber-200 px-6 py-8">
      <div className="max-w-4xl mx-auto">
        <p className="text-sm text-amber-900 italic">{data.text}</p>
      </div>
    </section>
  );
}

export function FooterSection({ data }: { data: FooterData }) {
  return (
    <footer className="bg-gray-900 text-gray-300 px-6 py-8">
      <div className="max-w-4xl mx-auto text-sm">
        <p className="font-semibold text-white">{data.name}</p>
        <p>{data.role}</p>
        {data.location && <p>{data.location}</p>}
        {(data.phone || data.email) && (
          <div className="mt-4 flex gap-6">
            {data.phone && <span>{data.phone}</span>}
            {data.email && <span>{data.email}</span>}
          </div>
        )}
      </div>
    </footer>
  );
}
