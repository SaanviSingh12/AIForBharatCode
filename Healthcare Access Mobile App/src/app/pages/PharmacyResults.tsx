import React from 'react';
import { useNavigate, useLocation } from 'react-router';
import { ArrowLeft, MapPin, Phone, Clock, TrendingDown, Building2, Volume2 } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { translations, mockPharmacies, mockMedicines, Pharmacy } from '../data/mockData';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { BottomNav } from '../components/BottomNav';
import { playAudioResponse } from '../services/api';

export const PharmacyResults: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { language, prescription, prescriptionResult } = useApp();
  const t = translations[language] || translations.en;

  const fromPrescription = location.state?.fromPrescription || false;

  // Use API results if available
  const apiMedicines = prescriptionResult?.medicines;
  const apiPharmacies = prescriptionResult?.janAushadhiLocations;
  const useApiData = prescriptionResult?.success && apiMedicines && apiMedicines.length > 0;

  // Map API pharmacy → Pharmacy shape for the existing UI
  const displayPharmacies = useApiData && apiPharmacies
    ? apiPharmacies.map((p) => ({
        id: p.id,
        name: p.name,
        type: p.type === 'jan-aushadhi' ? 'government' : 'private',
        distance: p.distance,
        phone: p.phone,
        address: p.address,
        timings: p.timings,
        hasDelivery: false,
      } as Pharmacy))
    : [...mockPharmacies].sort((a, b) => {
        if (a.type === 'government' && b.type !== 'government') return -1;
        if (a.type !== 'government' && b.type === 'government') return 1;
        return a.distance - b.distance;
      });

  const handleCall = (phone: string) => {
    window.location.href = `tel:${phone}`;
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
            <h1 className="font-semibold text-lg">Nearby Pharmacies</h1>
            {fromPrescription && prescription && (
              <p className="text-xs text-gray-600">Results for: {prescription}</p>
            )}
          </div>
        </div>
      </div>

      <div className="p-4 space-y-4">
        {/* AI Audio Response */}
        {prescriptionResult?.audioBase64 && (
          <Card className="p-3 bg-blue-50 border-blue-200">
            <div className="flex items-center justify-between">
              <p className="text-sm text-blue-800">{prescriptionResult.responseText}</p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => playAudioResponse(prescriptionResult.audioBase64!)}
              >
                <Volume2 className="w-4 h-4" />
              </Button>
            </div>
          </Card>
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
            <div className="flex items-center justify-between">
              <h2 className="font-semibold text-gray-900">Generic Medicine Prices</h2>
              <Badge className="bg-green-100 text-green-800">Save up to 85%</Badge>
            </div>

            {(useApiData ? apiMedicines! : mockMedicines).map((medicine, idx) => (
              <Card key={'id' in medicine ? medicine.id : idx} className="p-4">
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <h3 className="font-semibold text-gray-900 mb-1">{medicine.genericName}</h3>
                    <div className="flex items-center gap-2">
                      <TrendingDown className="w-4 h-4 text-green-600" />
                      <span className="text-sm text-green-600 font-semibold">
                        Save {medicine.savings}%
                      </span>
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="bg-red-50 rounded-lg p-3">
                    <p className="text-xs text-gray-600 mb-1">Branded</p>
                    <p className="text-lg font-bold text-red-600">
                      ₹{'brandPrice' in medicine ? medicine.brandPrice : medicine.brandedPrice}
                    </p>
                  </div>
                  <div className="bg-green-50 rounded-lg p-3">
                    <p className="text-xs text-gray-600 mb-1">Generic (Jan Aushadhi)</p>
                    <p className="text-lg font-bold text-green-600">₹{medicine.genericPrice}</p>
                  </div>
                </div>

                <div className="mt-3 bg-orange-50 rounded-lg p-2 text-center">
                  <p className="text-sm text-orange-800">
                    💰 You save{' '}
                    <span className="font-bold">
                      ₹{'savingsAmount' in medicine
                        ? medicine.savingsAmount
                        : (('brandPrice' in medicine ? medicine.brandPrice : medicine.brandedPrice) - medicine.genericPrice)}
                    </span>{' '}
                    per pack
                  </p>
                </div>
              </Card>
            ))}

            <div className="h-px bg-gray-200 my-6"></div>
          </div>
        )}

        {/* Pharmacy List */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-gray-900">
              {displayPharmacies.length} pharmacies within 10 km
            </h2>
            <p className="text-xs text-green-600">
              ✓ Jan Aushadhi prioritized
            </p>
          </div>

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
                        <span className="text-sm font-semibold">{pharmacy.distance} km</span>
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
                        ✓ Quality generic medicines available<br />
                        ✓ Up to 85% savings on medicines<br />
                        ✓ Part of PM Jan Aushadhi Scheme
                      </p>
                    </div>
                  )}
                </div>

                {/* Call Button */}
                <div className="border-t border-gray-200 p-3 bg-gray-50">
                  <Button
                    onClick={() => handleCall(pharmacy.phone)}
                    className="w-full bg-green-600 hover:bg-green-700"
                  >
                    <Phone className="w-4 h-4 mr-2" />
                    {t.call} {pharmacy.phone}
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </div>

        {/* Info Card */}
        <Card className="p-4 bg-blue-50 border-blue-200">
          <h3 className="font-semibold text-blue-900 mb-2">ℹ️ About Jan Aushadhi Kendras</h3>
          <p className="text-sm text-blue-800">
            Jan Aushadhi Kendras are government pharmacies that provide quality generic medicines at affordable prices. 
            These medicines are manufactured in government-approved facilities and meet all quality standards.
          </p>
        </Card>
      </div>

      {/* Bottom Navigation */}
      <BottomNav />
    </div>
  );
};