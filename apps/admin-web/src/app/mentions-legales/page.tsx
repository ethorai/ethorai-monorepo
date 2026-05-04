export const metadata = {
  title: "Mentions légales — Ethorai",
};

export default function MentionsLegalesPage() {
  return (
    <div className="min-h-screen bg-stone-50 px-6 py-16 sm:px-10">
      <div className="mx-auto max-w-2xl space-y-10">
        <div>
          <h1 className="font-serif text-3xl font-medium text-stone-900">
            Mentions légales
          </h1>
          <p className="mt-2 text-sm text-stone-500">
            Conformément à la loi n° 2004-575 du 21 juin 2004 pour la confiance
            dans l&apos;économie numérique (LCEN).
          </p>
        </div>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-800">Éditeur</h2>
          <p className="text-stone-600">
            Ce site est édité à titre personnel par :
          </p>
          <ul className="space-y-1 text-stone-600">
            <li>
              <span className="font-medium">Nom :</span> Mohamed Najib Slassi
            </li>
            <li>
              <span className="font-medium">Email :</span>{" "}
              <a
                href="mailto:mednajib.slassi@gmail.com"
                className="underline hover:text-stone-900"
              >
                mednajib.slassi@gmail.com
              </a>
            </li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-800">Hébergement</h2>
          <ul className="space-y-3 text-stone-600">
            <li>
              <p className="font-medium">Interface web (Vercel Inc.)</p>
              <p>440 N Barranca Ave #4133, Covina, CA 91723, États-Unis</p>
              <p>
                <a
                  href="https://vercel.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline hover:text-stone-900"
                >
                  vercel.com
                </a>
              </p>
            </li>
            <li>
              <p className="font-medium">API et base de données (Railway Corp.)</p>
              <p>
                <a
                  href="https://railway.app"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline hover:text-stone-900"
                >
                  railway.app
                </a>
              </p>
            </li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-800">
            Données personnelles
          </h2>
          <p className="text-stone-600">
            Ethorai collecte uniquement les données nécessaires au fonctionnement
            du service : adresse email, nom, et les informations professionnelles
            que vous saisissez pour générer votre page (ville, téléphone, etc.).
          </p>
          <p className="text-stone-600">
            Ces données ne sont pas revendues ni transmises à des tiers, à
            l&apos;exception des prestataires techniques indispensables au
            service (Vercel, Railway, OpenAI pour la génération de contenu).
          </p>
          <p className="text-stone-600">
            Conformément au Règlement Général sur la Protection des Données
            (RGPD), vous disposez d&apos;un droit d&apos;accès, de
            rectification et de suppression de vos données. Pour exercer ces
            droits, contactez-nous à{" "}
            <a
              href="mailto:mednajib.slassi@gmail.com"
              className="underline hover:text-stone-900"
            >
              mednajib.slassi@gmail.com
            </a>
            .
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-800">Cookies</h2>
          <p className="text-stone-600">
            Ce site utilise uniquement des cookies strictement nécessaires au
            maintien de votre session de connexion. Aucun cookie publicitaire
            ou de traçage n&apos;est utilisé.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-800">
            Génération de contenu
          </h2>
          <p className="text-stone-600">
            Le contenu des pages est généré via l&apos;API OpenAI (GPT-4o).
            Les informations que vous saisissez sont transmises à OpenAI dans
            le seul but de produire le contenu de votre page. Elles ne sont
            pas utilisées pour entraîner des modèles.
          </p>
        </section>
      </div>
    </div>
  );
}
