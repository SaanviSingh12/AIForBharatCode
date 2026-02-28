import React, { createContext, useContext, useState, ReactNode } from 'react';
import type { TriageApiResponse, PrescriptionApiResponse } from '../services/api';

interface AppContextType {
  language: string;
  setLanguage: (lang: string) => void;
  userProfile: {
    name: string;
    age: string;
    gender: string;
  };
  setUserProfile: (profile: any) => void;
  symptoms: string;
  setSymptoms: (symptoms: string) => void;
  prescription: string;
  setPrescription: (prescription: string) => void;
  // API results
  triageResult: TriageApiResponse | null;
  setTriageResult: (result: TriageApiResponse | null) => void;
  prescriptionResult: PrescriptionApiResponse | null;
  setPrescriptionResult: (result: PrescriptionApiResponse | null) => void;
  isLoading: boolean;
  setIsLoading: (loading: boolean) => void;
  apiError: string | null;
  setApiError: (error: string | null) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [language, setLanguage] = useState('en');
  const [userProfile, setUserProfile] = useState({
    name: 'Rajesh Kumar',
    age: '35',
    gender: 'Male',
  });
  const [symptoms, setSymptoms] = useState('');
  const [prescription, setPrescription] = useState('');
  const [triageResult, setTriageResult] = useState<TriageApiResponse | null>(null);
  const [prescriptionResult, setPrescriptionResult] = useState<PrescriptionApiResponse | null>(null);
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
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
};
