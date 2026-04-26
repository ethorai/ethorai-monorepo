import { auth } from "@/auth";
import { OnboardingFlow } from "@/components/onboarding/flow";

export default async function OnboardingPage() {
  const session = await auth();
  const fullName = session?.user?.name ?? "";
  const firstName = fullName.split(" ")[0] || undefined;

  return <OnboardingFlow firstName={firstName} />;
}
