import React, { createContext, ReactNode, useCallback, useContext, useEffect, useState } from 'react';
import type { PrescriptionApiResponse, TriageApiResponse } from '../services/api';

export interface UserLocation {
    lat: string;
    lng: string;
}

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
    // Pending audio blob for cross-page transfer (Blobs can't go through router state)
    pendingAudioBlob: Blob | null;
    setPendingAudioBlob: (blob: Blob | null) => void;
    // User location from browser geolocation
    userLocation: UserLocation | null;
    locationError: string | null;
    requestLocation: () => void;
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
    const [language, setLanguage] = useState(() => {
        const saved = localStorage.getItem('sahayak-language');
        return saved || 'en';
    });
    const [userProfile, setUserProfile] = useState({
        name: 'Rajesh Kumar',
        age: '35',
        gender: 'Male',
    });
    const [symptoms, setSymptoms] = useState('');
    const [prescription, setPrescription] = useState('');
    const [pendingAudioBlob, setPendingAudioBlob] = useState<Blob | null>(null);
    const [triageResult, setTriageResult] = useState<TriageApiResponse | null>(null);
    const [prescriptionResult, setPrescriptionResult] = useState<PrescriptionApiResponse | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState<string | null>(null);

    // ── Geolocation ────────────────────────────
    const [userLocation, setUserLocation] = useState<UserLocation | null>(null);
    const [locationError, setLocationError] = useState<string | null>(null);

    const requestLocation = useCallback(() => {
        if (!navigator.geolocation) {
            setLocationError('Geolocation is not supported by your browser');
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (position) => {
                setUserLocation({
                    lat: position.coords.latitude.toString(),
                    lng: position.coords.longitude.toString(),
                });
                setLocationError(null);
            },
            (error) => {
                console.warn('Geolocation error:', error.message);
                setLocationError(error.message);
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
        );
    }, []);

    // Request location on app start
    useEffect(() => {
        requestLocation();
    }, [requestLocation]);

    // Save language to localStorage
    useEffect(() => {
        localStorage.setItem('sahayak-language', language);
    }, [language]);

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
                pendingAudioBlob,
                setPendingAudioBlob,
                userLocation,
                locationError,
                requestLocation,
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
