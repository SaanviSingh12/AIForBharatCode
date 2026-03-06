import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { ScreenWithNav } from "../components/navigation/BottomNav";
import { Card } from "../components/ui/Card";
import { Badge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";
import {
  getNearbyPharmacies,
  playAudioResponse,
  type PharmacyDto,
  type MedicineDto,
} from "../services/api";
import { makePhoneCall } from "../utils/linking";

interface Pharmacy {
  id: string;
  name: string;
  type: "government" | "private";
  distance: number;
  phone: string;
  address: string;
  timings: string;
}

export default function PharmacyResultsScreen({
  navigation,
}: ScreenProps<"PharmacyResults">) {
  const { language, prescription, prescriptionResult } = useApp();
  const t = getTranslations(language);

  const [fetchedPharmacies, setFetchedPharmacies] = useState<Pharmacy[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Use API results if available
  const apiMedicines = prescriptionResult?.medicines;
  const apiPharmacies = prescriptionResult?.janAushadhiLocations;
  const useApiData =
    prescriptionResult?.success && apiMedicines && apiMedicines.length > 0;

  // Fetch pharmacies from API on mount
  useEffect(() => {
    let cancelled = false;
    const fetchPharmacies = async () => {
      setIsLoading(true);
      try {
        const data: PharmacyDto[] = await getNearbyPharmacies();
        if (!cancelled) {
          setFetchedPharmacies(
            data.map((p) => ({
              id: p.id,
              name: p.name,
              type:
                p.type === "jan-aushadhi"
                  ? "government"
                  : (p.type as Pharmacy["type"]),
              distance: p.distance,
              phone: p.phone,
              address: p.address,
              timings: p.timings,
            }))
          );
        }
      } catch {
        // Fall back silently
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };
    fetchPharmacies();
    return () => {
      cancelled = true;
    };
  }, []);

  // Priority: prescription API pharmacies > fetched pharmacies
  let displayPharmacies: Pharmacy[];
  if (useApiData && apiPharmacies && apiPharmacies.length > 0) {
    displayPharmacies = apiPharmacies.map((p) => ({
      id: p.id,
      name: p.name,
      type:
        p.type === "jan-aushadhi" ? "government" : (p.type as Pharmacy["type"]),
      distance: p.distance,
      phone: p.phone,
      address: p.address,
      timings: p.timings,
    }));
  } else {
    displayPharmacies = fetchedPharmacies;
  }

  const handleCall = (phone: string) => {
    makePhoneCall(phone);
  };

  const handlePlayAudio = () => {
    if (prescriptionResult?.audioBase64) {
      playAudioResponse(prescriptionResult.audioBase64);
    }
  };

  return (
    <ScreenWithNav>
      <SafeAreaView className="flex-1 bg-blue-50" edges={["top"]}>
        {/* Header */}
        <View className="bg-white px-4 py-3 border-b border-gray-200">
          <View className="flex-row items-center">
            <TouchableOpacity
              onPress={() => navigation.goBack()}
              className="mr-3"
            >
              <Text className="text-blue-600 text-lg">←</Text>
            </TouchableOpacity>
            <View className="flex-1">
              <Text className="text-lg font-semibold text-gray-900">
                Nearby Pharmacies
              </Text>
              {prescription && (
                <Text className="text-xs text-gray-600">
                  Results for: {prescription.substring(0, 30)}...
                </Text>
              )}
            </View>
          </View>
        </View>

        <ScrollView className="flex-1 px-4 pt-4">
          {/* Audio Response */}
          {prescriptionResult?.audioBase64 && (
            <Card className="p-3 bg-blue-50 border-blue-200 mb-4">
              <View className="flex-row items-center justify-between">
                <Text
                  className="text-sm text-blue-800 flex-1 mr-2"
                  numberOfLines={2}
                >
                  {prescriptionResult.responseText}
                </Text>
                <Button variant="outline" size="sm" onPress={handlePlayAudio}>
                  <Text className="text-blue-600">🔊</Text>
                </Button>
              </View>
            </Card>
          )}

          {/* Savings Summary */}
          {useApiData && (
            <Card className="p-4 bg-green-50 border-green-200 mb-4">
              <View className="flex-row justify-between items-center">
                <View>
                  <Text className="text-sm text-gray-600">
                    Brand cost:{" "}
                    <Text className="line-through">
                      ₹{prescriptionResult!.totalBrandCost}
                    </Text>
                  </Text>
                  <Text className="font-bold text-green-700 text-lg">
                    Generic cost: ₹{prescriptionResult!.totalGenericCost}
                  </Text>
                </View>
                <View className="bg-green-600 px-3 py-2 rounded-lg">
                  <Text className="text-white font-bold text-lg">
                    Save {prescriptionResult!.totalSavingsPercent}%
                  </Text>
                </View>
              </View>
            </Card>
          )}

          {/* Medicines Section */}
          {useApiData && (
            <>
              <View className="flex-row justify-between items-center mb-3">
                <Text className="font-semibold text-gray-900">
                  Generic Medicine Prices
                </Text>
                <Badge variant="success">Save up to 85%</Badge>
              </View>

              {(apiMedicines ?? []).map(
                (medicine: MedicineDto, idx: number) => (
                  <Card key={medicine.brandName || idx} className="p-4 mb-3">
                    <View className="flex-row justify-between items-start mb-3">
                      <View className="flex-1">
                        <Text className="font-semibold text-gray-900 mb-1">
                          {medicine.genericName}
                        </Text>
                        <View className="flex-row items-center">
                          <Text className="text-green-600 mr-1">📉</Text>
                          <Text className="text-sm text-green-600 font-semibold">
                            Save {medicine.savingsPercent}%
                          </Text>
                        </View>
                      </View>
                    </View>

                    <View className="flex-row gap-3">
                      <View className="flex-1 bg-red-50 rounded-lg p-3">
                        <Text className="text-xs text-gray-600 mb-1">
                          Branded
                        </Text>
                        <Text className="text-lg font-bold text-red-600">
                          ₹{medicine.brandPrice}
                        </Text>
                      </View>
                      <View className="flex-1 bg-green-50 rounded-lg p-3">
                        <Text className="text-xs text-gray-600 mb-1">
                          Generic (Jan Aushadhi)
                        </Text>
                        <Text className="text-lg font-bold text-green-600">
                          ₹{medicine.genericPrice}
                        </Text>
                      </View>
                    </View>

                    <View className="mt-3 bg-orange-50 rounded-lg p-2">
                      <Text className="text-sm text-orange-800 text-center">
                        💰 You save{" "}
                        <Text className="font-bold">
                          ₹{medicine.savingsAmount}
                        </Text>{" "}
                        per pack
                      </Text>
                    </View>
                  </Card>
                )
              )}

              <View className="h-px bg-gray-200 my-4" />
            </>
          )}

          {/* Pharmacy List */}
          <View className="mb-2">
            <View className="flex-row justify-between items-center mb-3">
              <Text className="font-semibold text-gray-900">
                {displayPharmacies.length} Pharmacies Nearby
              </Text>
              <Text className="text-xs text-green-600">
                ✓ Jan Aushadhi prioritized
              </Text>
            </View>

            {isLoading ? (
              <View className="items-center py-8">
                <ActivityIndicator size="large" color="#2563EB" />
                <Text className="text-gray-600 mt-3">
                  {t.loading || "Loading..."}
                </Text>
              </View>
            ) : displayPharmacies.length === 0 ? (
              <Card className="p-8 items-center">
                <Text className="text-gray-500">No pharmacies found</Text>
                <Text className="text-sm text-gray-400 mt-1">
                  Try again later
                </Text>
              </Card>
            ) : (
              displayPharmacies.map((pharmacy) => (
                <Card key={pharmacy.id} className="mb-3 overflow-hidden">
                  <View className="p-4">
                    {/* Pharmacy Header */}
                    <View className="flex-row justify-between items-start mb-2">
                      <View className="flex-row items-start flex-1">
                        {pharmacy.type === "government" && (
                          <View className="w-8 h-8 bg-green-500 rounded-lg items-center justify-center mr-2">
                            <Text className="text-white">🏥</Text>
                          </View>
                        )}
                        <View className="flex-1">
                          <Text className="font-semibold text-gray-900 mb-1">
                            {pharmacy.name}
                          </Text>
                          <Badge
                            variant={
                              pharmacy.type === "government"
                                ? "success"
                                : "secondary"
                            }
                          >
                            {pharmacy.type === "government"
                              ? "Government"
                              : "Commercial"}
                          </Badge>
                        </View>
                      </View>
                      <View className="flex-row items-center">
                        <Text className="text-gray-400 mr-1">📍</Text>
                        <Text className="text-sm font-semibold text-gray-600">
                          {pharmacy.distance} km
                        </Text>
                      </View>
                    </View>

                    {/* Details */}
                    <View className="space-y-1 mb-2">
                      <View className="flex-row items-start">
                        <Text className="text-gray-400 mr-2">📍</Text>
                        <Text className="text-sm text-gray-600 flex-1">
                          {pharmacy.address}
                        </Text>
                      </View>
                      <View className="flex-row items-center">
                        <Text className="text-gray-400 mr-2">🕐</Text>
                        <Text className="text-sm text-gray-600">
                          {pharmacy.timings}
                        </Text>
                      </View>
                    </View>

                    {/* Government Benefits */}
                    {pharmacy.type === "government" && (
                      <View className="mt-2 bg-green-50 rounded-lg p-2">
                        <Text className="text-xs text-green-800">
                          ✓ Quality generic medicines{"\n"}✓ Up to 85% savings
                          {"\n"}✓ Part of PM Jan Aushadhi Yojana
                        </Text>
                      </View>
                    )}
                  </View>

                  {/* Call Button */}
                  <View className="border-t border-gray-200 p-3 bg-gray-50">
                    <Button
                      onPress={() => handleCall(pharmacy.phone)}
                      className="bg-green-600"
                    >
                      <View className="flex-row items-center justify-center">
                        <Text className="text-white mr-2">📞</Text>
                        <Text className="text-white font-medium">
                          {t.call || "Call"} {pharmacy.phone}
                        </Text>
                      </View>
                    </Button>
                  </View>
                </Card>
              ))
            )}
          </View>

          {/* Info Card */}
          <Card className="p-4 bg-blue-50 border-blue-200 mb-6">
            <Text className="font-semibold text-blue-900 mb-2">
              ℹ️ About Jan Aushadhi Kendras
            </Text>
            <Text className="text-sm text-blue-800">
              Jan Aushadhi Kendras are government-supported pharmacies that sell
              quality generic medicines at affordable prices. They are part of
              the Pradhan Mantri Bhartiya Jan Aushadhi Pariyojana (PMBJP).
            </Text>
          </Card>
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
