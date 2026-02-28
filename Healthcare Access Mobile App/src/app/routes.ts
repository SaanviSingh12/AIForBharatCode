import { createBrowserRouter } from "react-router";
import { LanguageSelection } from "./pages/LanguageSelection";
import { Home } from "./pages/Home";
import { SymptomEntry } from "./pages/SymptomEntry";
import { DoctorSearch } from "./pages/DoctorSearch";
import { DoctorDetails } from "./pages/DoctorDetails";
import { PrescriptionSearch } from "./pages/PrescriptionSearch";
import { PharmacyResults } from "./pages/PharmacyResults";
import { UserProfile } from "./pages/UserProfile";
import { EmergencyMode } from "./pages/EmergencyMode";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: LanguageSelection,
  },
  {
    path: "/home",
    Component: Home,
  },
  {
    path: "/symptom-entry",
    Component: SymptomEntry,
  },
  {
    path: "/doctor-search",
    Component: DoctorSearch,
  },
  {
    path: "/doctor/:id",
    Component: DoctorDetails,
  },
  {
    path: "/prescription-search",
    Component: PrescriptionSearch,
  },
  {
    path: "/pharmacy-results",
    Component: PharmacyResults,
  },
  {
    path: "/profile",
    Component: UserProfile,
  },
  {
    path: "/emergency",
    Component: EmergencyMode,
  },
]);
