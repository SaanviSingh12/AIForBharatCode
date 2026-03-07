import { ArrowLeft, Building2, Clock, MapPin, Navigation, Phone, TrendingDown, Volume2 } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { BottomNav } from '../components/BottomNav';
import { Badge } from '../components/ui/badge';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { Skeleton } from '../components/ui/skeleton';
import { useApp } from '../context/AppContext';
import type { Pharmacy } from '../data/mockData';
import { getNearbyPharmacies, type MedicineDto, type PharmacyDto, playAudioResponse } from '../services/api';

export const PharmacyResults: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { language, prescription, prescriptionResult, userLocation } = useApp();
    const t = getTranslations(language);

    const fromPrescription = location.state?.fromPrescription || false;

    const [fetchedPharmacies, setFetchedPharmacies] = useState<Pharmacy[]>([]);
    const [isLoadingPharmacies, setIsLoadingPharmacies] = useState(false);

    // Use API results if available
    const apiMedicines = prescriptionResult?.medicines;
    const apiPharmacies = prescriptionResult?.janAushadhiLocations;
    const useApiData = prescriptionResult?.success && apiMedicines && apiMedicines.length > 0;

    // Fetch pharmacies from API on mount
    useEffect(() => {
        let cancelled = false;
        const fetchPharmacies = async () => {
            setIsLoadingPharmacies(true);
            try {
                const loc = userLocation
                    ? { lat: String(userLocation.lat), lng: String(userLocation.lng) }
                    : {};
                const data: PharmacyDto[] = await getNearbyPharmacies(loc);
                if (!cancelled) {
                    setFetchedPharmacies(
                        data.map((p) => ({
                            id: p.id,
                            name: p.name,
                            type: p.type === 'jan-aushadhi' ? 'government' : (p.type as Pharmacy['type']),
                            distance: p.distance,
                            phone: p.phone,
                            address: p.address,
                            timings: p.timings,
                        }))
                    );
                }
            } catch {
                // Silently fall back to mock data
            } finally {
                if (!cancelled) setIsLoadingPharmacies(false);
            }
        };
        fetchPharmacies();
        return () => { cancelled = true; };
    }, [userLocation]);

    // Priority: prescription API pharmacies > fetched API pharmacies
    let displayPharmacies: Pharmacy[];
    if (useApiData && apiPharmacies && apiPharmacies.length > 0) {
        displayPharmacies = apiPharmacies.map((p) => ({
            id: p.id,
            name: p.name,
            type: (p.type === 'jan-aushadhi' ? 'government' : p.type) as Pharmacy['type'],
            distance: p.distance,
            phone: p.phone,
            address: p.address,
            timings: p.timings,
        }));
    } else {
        displayPharmacies = fetchedPharmacies;
    }

    const handleCall = (phone: string) => {
        window.location.href = `tel:${phone}`;
    };

    const handleGetDirections = (name: string, address: string) => {
        const query = encodeURIComponent(`${name}, ${address}`);
        window.open(`https://www.google.com/maps/search/?api=1&query=${query}`, '_blank');
    };

    const getPharmacyTypeColor = (type: Pharmacy['type']) => {
        return type === 'government'
            ? 'bg-green-100 text-green-800 border-green-300'
            : 'bg-blue-100 text-blue-800 border-blue-300';
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
            {/* Header */}
            <div className="bg-white shadow-sm p-4 sticky top-0 z-10">
                <div className="flex items-center gap-3">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate('/prescription-search')}
                    >
                        <ArrowLeft className="w-5 h-5" />
                    </Button>
                    <div>
                        <h1 className="font-semibold text-lg">
                            {t.nearbyPharmacies}
                            {language !== 'en' && (
                                <span className="text-xs text-gray-500 italic ml-2">Nearby Pharmacies</span>
                            )}
                        </h1>
                        {fromPrescription && prescription && (
                            <p className="text-xs text-gray-600">{t.resultsFor} {prescription}</p>
                        )}
                    </div>
                </div>
            </div>

            <div className="p-4 space-y-4">
                {/* AI Audio Response — styled like DoctorSearch response card */}
                {prescriptionResult?.responseText && (
                    <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-xl border border-green-200 p-4 shadow-sm">
                        <div className="flex items-start gap-3">
                            <div className="bg-green-100 rounded-full p-2 mt-0.5">
                                <Volume2 className="w-4 h-4 text-green-600" />
                            </div>
                            <div className="flex-1">
                                <p className="text-xs font-semibold text-green-600 uppercase tracking-wide mb-1">Response for Patient</p>
                                <p className="text-sm text-gray-800 leading-relaxed mb-2">{prescriptionResult.responseText}</p>
                                {prescriptionResult.audioBase64 && (
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => playAudioResponse(prescriptionResult.audioBase64!)}
                                    >
                                        <Volume2 className="w-4 h-4 mr-2" />
                                        Play Audio
                                    </Button>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {/* Savings Summary (API) */}
                {useApiData && (
                    <Card className="p-4 bg-green-50 border-green-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600">Brand cost: <span className="line-through">₹{prescriptionResult!.totalBrandCost}</span></p>
                                <p className="font-bold text-green-700 text-lg">Generic cost: ₹{prescriptionResult!.totalGenericCost}</p>
                            </div>
                            <Badge className="bg-green-600 text-white text-lg px-3 py-1">
                                Save {prescriptionResult!.totalSavingsPercent}%
                            </Badge>
                        </div>
                    </Card>
                )}

                {/* Medicines */}
                {fromPrescription && (
                    <div className="space-y-4 mb-6">
                        <div>
                            <h2 className="font-semibold text-gray-900">
                                {t.genericMedicinePrices}
                                {language !== 'en' && (
                                    <span className="text-xs text-gray-500 italic ml-2">Generic Medicine Prices</span>
                                )}
                            </h2>
                            <div className="flex items-center justify-between">
                                <div></div>
                                <Badge className="bg-green-100 text-green-800">{t.saveUpTo85}</Badge>
                            </div>
                        </div>

                        {(apiMedicines ?? []).map((medicine: MedicineDto, idx: number) => (
                            <Card key={medicine.brandName || idx} className="p-4">
                                <div className="flex items-start justify-between mb-3">
                                    <div>
                                        <h3 className="font-semibold text-gray-900 mb-1">{medicine.genericName}</h3>
                                        <div className="flex items-center gap-2">
                                            <TrendingDown className="w-4 h-4 text-green-600" />
                                            <span className="text-sm text-green-600 font-semibold">
                                                Save {medicine.savingsPercent}%
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <div className="bg-red-50 rounded-lg p-3">
                                        <p className="text-xs text-gray-600 mb-1">{t.branded}</p>
                                        <p className="text-lg font-bold text-red-600">
                                            ₹{medicine.brandPrice}
                                        </p>
                                    </div>
                                    <div className="bg-green-50 rounded-lg p-3">
                                        <p className="text-xs text-gray-600 mb-1">{t.genericJanAushadhi}</p>
                                        <p className="text-lg font-bold text-green-600">₹{medicine.genericPrice}</p>
                                    </div>
                                </div>

                                <div className="mt-3 bg-orange-50 rounded-lg p-2 text-center">
                                    <p className="text-sm text-orange-800">
                                        💰 {t.youSave}{' '}
                                        <span className="font-bold">
                                            ₹{medicine.savingsAmount}
                                        </span>{' '}
                                        {t.perPack}
                                    </p>
                                </div>
                            </Card>
                        ))}

                        <div className="h-px bg-gray-200 my-6"></div>
                    </div>
                )}

                {/* Pharmacy List */}
                <div>
                    <div>
                        <h2 className="font-semibold text-gray-900">
                            {displayPharmacies.length} {t.pharmaciesWithin10km}
                            {language !== 'en' && (
                                <span className="text-xs text-gray-500 italic ml-2">pharmacies within 10 km</span>
                            )}
                        </h2>
                        <div className="flex items-center justify-between mb-4">
                            <div></div>
                            <p className="text-xs text-green-600">
                                ✓ {t.janAushadhiPrioritized}
                            </p>
                        </div>
                    </div>

                    {isLoadingPharmacies ? (
                        <div className="space-y-4">
                            {[1, 2, 3].map((i) => (
                                <Card key={i} className="p-4">
                                    <Skeleton className="h-5 w-3/4 mb-3" />
                                    <Skeleton className="h-4 w-1/2 mb-2" />
                                    <Skeleton className="h-4 w-full mb-2" />
                                    <Skeleton className="h-10 w-full mt-3" />
                                </Card>
                            ))}
                        </div>
                    ) : displayPharmacies.length === 0 ? (
                        <Card className="p-8 text-center">
                            <p className="text-gray-500">{t.noResults}</p>
                            <p className="text-sm text-gray-400 mt-1">{t.tryAgain}</p>
                        </Card>
                    ) : (
                        <div className="space-y-4">
                            {displayPharmacies.map((pharmacy) => (
                                <Card key={pharmacy.id} className="overflow-hidden">
                                    <div className="p-4">
                                        {/* Pharmacy Header */}
                                        <div className="flex items-start justify-between mb-3">
                                            <div className="flex-1">
                                                <div className="flex items-start gap-2 mb-2">
                                                    {pharmacy.type === 'government' && (
                                                        <div className="w-8 h-8 bg-green-500 rounded-lg flex items-center justify-center flex-shrink-0">
                                                            <Building2 className="w-4 h-4 text-white" />
                                                        </div>
                                                    )}
                                                    <div className="flex-1">
                                                        <h3 className="font-semibold text-gray-900 mb-1">{pharmacy.name}</h3>
                                                        <Badge className={`${getPharmacyTypeColor(pharmacy.type)} text-xs`}>
                                                            {pharmacy.type === 'government' ? t.governmentPharmacy : t.commercialPharmacy}
                                                        </Badge>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="text-right">
                                                <div className="flex items-center gap-1 text-gray-600">
                                                    <MapPin className="w-4 h-4" />
                                                    <span className="text-sm font-semibold">~{pharmacy.distance} km</span>
                                                </div>
                                            </div>
                                        </div>

                                        {/* Pharmacy Details */}
                                        <div className="space-y-2 text-sm">
                                            <div className="flex items-start gap-2 text-gray-600">
                                                <MapPin className="w-4 h-4 mt-0.5 flex-shrink-0" />
                                                <span>{pharmacy.address}</span>
                                            </div>
                                            <div className="flex items-center gap-2 text-gray-600">
                                                <Clock className="w-4 h-4 flex-shrink-0" />
                                                <span>{pharmacy.timings}</span>
                                            </div>
                                        </div>

                                        {/* Government Pharmacy Benefits */}
                                        {pharmacy.type === 'government' && (
                                            <div className="mt-3 bg-green-50 rounded-lg p-3">
                                                <p className="text-xs text-green-800">
                                                    ✓ {t.qualityGenericMedicines}<br />
                                                    ✓ {t.upTo85Savings}<br />
                                                    ✓ {t.partOfPMJanAushadhi}
                                                </p>
                                            </div>
                                        )}
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="border-t border-gray-200 p-3 bg-gray-50 flex gap-2">
                                        <Button
                                            onClick={() => handleGetDirections(pharmacy.name, pharmacy.address)}
                                            variant="outline"
                                            className="flex-1 border-blue-300 text-blue-700 hover:bg-blue-50"
                                        >
                                            <Navigation className="w-4 h-4 mr-2" />
                                            Directions
                                        </Button>
                                        <Button
                                            onClick={() => handleCall(pharmacy.phone)}
                                            className="flex-1 bg-green-600 hover:bg-green-700"
                                        >
                                            <Phone className="w-4 h-4 mr-2" />
                                            {t.call}
                                        </Button>
                                    </div>
                                </Card>
                            ))}
                        </div>
                    )}
                </div>

                {/* Info Card */}
                <Card className="p-4 bg-blue-50 border-blue-200">
                    <h3 className="font-semibold text-blue-900 mb-2">ℹ️ {t.aboutJanAushadhi}</h3>
                    <p className="text-sm text-blue-800">
                        {t.janAushadhiDescriptionLong}
                    </p>
                </Card>
            </div>

            {/* Bottom Navigation */}
            <BottomNav />
        </div>
    );
};