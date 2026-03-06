import { NativeStackScreenProps } from "@react-navigation/native-stack";

// Define the navigation param types for type-safe navigation
export type RootStackParamList = {
  LanguageSelection: undefined;
  Home: undefined;
  SymptomEntry: undefined;
  DoctorSearch: undefined;
  DoctorDetails: { doctorId: string };
  EmergencyMode: undefined;
  PrescriptionSearch: undefined;
  PharmacyResults: undefined;
  UserProfile: undefined;
};

// Type helpers for screens
export type ScreenProps<T extends keyof RootStackParamList> =
  NativeStackScreenProps<RootStackParamList, T>;

// Route names constant for easy reference
export const Routes = {
  LanguageSelection: "LanguageSelection",
  Home: "Home",
  SymptomEntry: "SymptomEntry",
  DoctorSearch: "DoctorSearch",
  DoctorDetails: "DoctorDetails",
  EmergencyMode: "EmergencyMode",
  PrescriptionSearch: "PrescriptionSearch",
  PharmacyResults: "PharmacyResults",
  UserProfile: "UserProfile",
} as const;
