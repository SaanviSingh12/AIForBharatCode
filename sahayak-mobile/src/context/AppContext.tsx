import React, { createContext, useContext, useState, ReactNode } from "react";
import type {
  TriageApiResponse,
  PrescriptionApiResponse,
  UserProfile,
} from "../types";

interface AppContextType {
  // Language
  language: string;
  setLanguage: (lang: string) => void;

  // User profile
  userProfile: UserProfile;
  setUserProfile: (profile: UserProfile) => void;

  // Symptoms input
  symptoms: string;
  setSymptoms: (symptoms: string) => void;

  // Prescription input
  prescription: string;
  setPrescription: (prescription: string) => void;

  // API results
  triageResult: TriageApiResponse | null;
  setTriageResult: (result: TriageApiResponse | null) => void;
  prescriptionResult: PrescriptionApiResponse | null;
  setPrescriptionResult: (result: PrescriptionApiResponse | null) => void;

  // Loading and error states
  isLoading: boolean;
  setIsLoading: (loading: boolean) => void;
  apiError: string | null;
  setApiError: (error: string | null) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [language, setLanguage] = useState("hi-IN"); // Default to Hindi for rural users
  const [userProfile, setUserProfile] = useState<UserProfile>({
    name: "",
    age: "",
    gender: "",
  });
  const [symptoms, setSymptoms] = useState("");
  const [prescription, setPrescription] = useState("");
  const [triageResult, setTriageResult] = useState<TriageApiResponse | null>(
    null
  );
  const [prescriptionResult, setPrescriptionResult] =
    useState<PrescriptionApiResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  return (
    <AppContext.Provider
      value={{
        language,
        setLanguage,
        userProfile,
        setUserProfile,
        symptoms,
        setSymptoms,
        prescription,
        setPrescription,
        triageResult,
        setTriageResult,
        prescriptionResult,
        setPrescriptionResult,
        isLoading,
        setIsLoading,
        apiError,
        setApiError,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useApp must be used within AppProvider");
  }
  return context;
};
