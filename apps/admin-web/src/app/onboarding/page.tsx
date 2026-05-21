import { auth } from "@/auth";
import { OnboardingFlow } from "@/components/onboarding/flow";

export default async function OnboardingPage() {
  // Auth is optional — onboarding is public. We only use the session
  // to pre-fill firstName for the welcome screen if the user is logged in.
  const session = await auth().catch(() => null);
  const fullName = session?.user?.name ?? "";
  const firstName = fullName.split(" ")[0] || undefined;
  const isAuthenticated = !!session?.user?.springToken;

  return <OnboardingFlow firstName={firstName} isAuthenticated={isAuthenticated} />;
}
