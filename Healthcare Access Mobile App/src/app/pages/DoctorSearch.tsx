import { ArrowLeft, Award, Building, MapPin, Phone, Search } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { BottomNav } from '../components/BottomNav';
import { Badge } from '../components/ui/badge';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Skeleton } from '../components/ui/skeleton';
import { useApp } from '../context/AppContext';
import type { Doctor } from '../data/mockData';
import { getDoctors, type DoctorDto, type HospitalDto } from '../services/api';

export const DoctorSearch: React.FC = () => {
    const navigate = useNavigate();
    const { language, triageResult } = useApp();
    const t = getTranslations(language);

    const [searchQuery, setSearchQuery] = useState('');
    const [fetchedDoctors, setFetchedDoctors] = useState<Doctor[]>([]);
    const [isLoadingDoctors, setIsLoadingDoctors] = useState(false);

    // Fetch doctors from API on mount
    useEffect(() => {
        let cancelled = false;
        const fetchDocs = async () => {
            setIsLoadingDoctors(true);
            try {
                const data: DoctorDto[] = await getDoctors();
                if (!cancelled) {
                    setFetchedDoctors(
                        data.map((d) => ({
                            id: d.id,
                            name: d.name,
                            specialty: d.specialty,
                            type: d.type as Doctor['type'],
                            distance: d.distance,
                            phone: d.phone,
                            address: d.address,
                            experience: d.experience,
                            languages: d.languages || [],
                        }))
                    );
                }
            } catch {
                // Fall back to mock data silently
            } finally {
                if (!cancelled) setIsLoadingDoctors(false);
            }
        };
        fetchDocs();
        return () => { cancelled = true; };
    }, []);

    // Use API results if available, otherwise fall back to mock data
    const apiHospitals = triageResult?.hospitals;
    const useApiData = apiHospitals && apiHospitals.length > 0;

    // Map HospitalDto → Doctor shape for the existing UI
    const triageDoctors: Doctor[] = useApiData
        ? apiHospitals.map((h: HospitalDto) => ({
            id: h.id,
            name: h.name,
            specialty: h.specialist || 'General Physician',
            type: h.type === 'government' ? 'government' as const : 'commercial' as const,
            distance: h.distance,
            phone: h.phone,
            address: h.address,
            experience: 0,
            languages: [],
        }))
        : [];

    // Priority: triage results > fetched API doctors
    const apiDoctors: Doctor[] = triageDoctors.length > 0
        ? triageDoctors
        : fetchedDoctors;

    const filteredDoctors = apiDoctors
        .filter(
            (doctor) =>
                doctor.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                doctor.specialty.toLowerCase().includes(searchQuery.toLowerCase())
        )
        .sort((a, b) => {
            if (a.type === 'government' && b.type !== 'government') return -1;
            if (a.type !== 'government' && b.type === 'government') return 1;
            return a.distance - b.distance;
        });

    const getDoctorTypeLabel = (type: Doctor['type']) => {
        switch (type) {
            case 'government': return t.governmentDoctor;
            case 'independent': return t.independentDoctor;
            case 'commercial': return t.commercialDoctor;
            default: return type;
        }
    };

    const getDoctorTypeColor = (type: Doctor['type']) => {
        switch (type) {
            case 'government': return 'bg-green-100 text-green-800 border-green-300';
            case 'independent': return 'bg-blue-100 text-blue-800 border-blue-300';
            case 'commercial': return 'bg-purple-100 text-purple-800 border-purple-300';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const handleCallDoctor = (phone: string) => {
        window.location.href = `tel:${phone}`;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
            {/* Header */}
            <div className="bg-white shadow-sm p-4 sticky top-0 z-10">
                <div className="flex items-center gap-3 mb-4">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate('/home')}
                    >
                        <ArrowLeft className="w-5 h-5" />
                    </Button>
                    <h1 className="font-semibold text-lg">{t.findDoctor}</h1>
                </div>

                {/* Search Bar */}
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <Input
                        placeholder={t.searchDoctors}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-10"
                    />
                </div>
            </div>

            {/* Doctor List */}
            <div className="p-4 space-y-4">
                <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-600">
                        {filteredDoctors.length} doctors found
                    </p>
                    <p className="text-xs text-green-600">
                        ✓ Government doctors prioritized
                    </p>
                </div>

                {isLoadingDoctors ? (
                    [1, 2, 3].map((i) => (
                        <Card key={i} className="p-4">
                            <Skeleton className="h-5 w-3/4 mb-3" />
                            <Skeleton className="h-4 w-1/2 mb-2" />
                            <Skeleton className="h-4 w-full mb-2" />
                            <Skeleton className="h-4 w-2/3 mb-2" />
                            <Skeleton className="h-10 w-full mt-3" />
                        </Card>
                    ))
                ) : filteredDoctors.length === 0 ? (
                    <Card className="p-8 text-center">
                        <p className="text-gray-500">{t.noResults}</p>
                        <p className="text-sm text-gray-400 mt-1">{t.tryAgain}</p>
                    </Card>
                ) : (
                filteredDoctors.map((doctor) => (
                    <Card key={doctor.id} className="overflow-hidden">
                        <div
                            className="p-4 cursor-pointer"
                            onClick={() => navigate(`/doctor/${doctor.id}`)}
                        >
                            {/* Doctor Header */}
                            <div className="flex items-start justify-between mb-3">
                                <div className="flex-1">
                                    <h3 className="font-semibold text-gray-900 mb-1">{doctor.name}</h3>
                                    <p className="text-sm text-blue-600 mb-2">{doctor.specialty}</p>
                                    <Badge className={`${getDoctorTypeColor(doctor.type)} text-xs`}>
                                        {getDoctorTypeLabel(doctor.type)}
                                    </Badge>
                                </div>
                                <div className="text-right">
                                    <div className="flex items-center gap-1 text-gray-600 mb-1">
                                        <MapPin className="w-4 h-4" />
                                        <span className="text-sm font-semibold">{doctor.distance} km</span>
                                    </div>
                                </div>
                            </div>

                            {/* Doctor Details */}
                            <div className="space-y-2 text-sm">
                                <div className="flex items-start gap-2 text-gray-600">
                                    <Building className="w-4 h-4 mt-0.5 flex-shrink-0" />
                                    <span>{doctor.address}</span>
                                </div>
                                <div className="flex items-center gap-2 text-gray-600">
                                    <Award className="w-4 h-4 flex-shrink-0" />
                                    <span>{doctor.experience} years experience</span>
                                </div>
                            </div>

                            {/* Languages */}
                            <div className="flex flex-wrap gap-2 mt-3">
                                {doctor.languages.map((lang, idx) => (
                                    <span
                                        key={idx}
                                        className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded"
                                    >
                                        {lang}
                                    </span>
                                ))}
                            </div>
                        </div>

                        {/* Call Button */}
                        <div className="border-t border-gray-200 p-3 bg-gray-50">
                            <Button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleCallDoctor(doctor.phone);
                                }}
                                className="w-full bg-green-600 hover:bg-green-700"
                            >
                                <Phone className="w-4 h-4 mr-2" />
                                {t.call} {doctor.phone}
                            </Button>
                        </div>
                    </Card>
                ))
                )}
            </div>

            <BottomNav />
        </div>
    );
};