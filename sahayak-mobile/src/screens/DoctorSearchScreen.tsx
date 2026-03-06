import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  TextInput,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { ScreenWithNav } from "../components/navigation/BottomNav";
import { Card, PressableCard } from "../components/ui/Card";
import { Badge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";
import { getDoctors, type DoctorDto, type HospitalDto } from "../services/api";
import { makePhoneCall } from "../utils/linking";

interface Doctor {
  id: string;
  name: string;
  specialty: string;
  type: "government" | "independent" | "commercial";
  distance: number;
  phone: string;
  address: string;
  experience: number;
  languages: string[];
}

export default function DoctorSearchScreen({
  navigation,
}: ScreenProps<"DoctorSearch">) {
  const { language, triageResult } = useApp();
  const t = getTranslations(language);

  const [searchQuery, setSearchQuery] = useState("");
  const [fetchedDoctors, setFetchedDoctors] = useState<Doctor[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Fetch doctors from API on mount
  useEffect(() => {
    let cancelled = false;
    const fetchDocs = async () => {
      setIsLoading(true);
      try {
        const data: DoctorDto[] = await getDoctors();
        if (!cancelled) {
          setFetchedDoctors(
            data.map((d) => ({
              id: d.id,
              name: d.name,
              specialty: d.specialty,
              type: d.type as Doctor["type"],
              distance: d.distance,
              phone: d.phone,
              address: d.address,
              experience: d.experience,
              languages: d.languages || [],
            }))
          );
        }
      } catch {
        // Fall back silently
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };
    fetchDocs();
    return () => {
      cancelled = true;
    };
  }, []);

  // Use triage results if available, otherwise fetched doctors
  const apiHospitals = triageResult?.hospitals;
  const useApiData = apiHospitals && apiHospitals.length > 0;

  const triageDoctors: Doctor[] = useApiData
    ? apiHospitals.map((h: HospitalDto) => ({
        id: h.id,
        name: h.name,
        specialty: h.specialist || "General Physician",
        type: h.type === "government" ? ("government" as const) : ("commercial" as const),
        distance: h.distance,
        phone: h.phone,
        address: h.address,
        experience: 0,
        languages: [],
      }))
    : [];

  const apiDoctors: Doctor[] =
    triageDoctors.length > 0 ? triageDoctors : fetchedDoctors;

  const filteredDoctors = apiDoctors
    .filter(
      (doctor) =>
        doctor.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        doctor.specialty.toLowerCase().includes(searchQuery.toLowerCase())
    )
    .sort((a, b) => {
      // Government doctors first
      if (a.type === "government" && b.type !== "government") return -1;
      if (a.type !== "government" && b.type === "government") return 1;
      return a.distance - b.distance;
    });

  const getDoctorTypeLabel = (type: Doctor["type"]) => {
    switch (type) {
      case "government":
        return t.governmentDoctor || "Government";
      case "independent":
        return "Independent";
      case "commercial":
        return "Commercial";
      default:
        return type;
    }
  };

  const getDoctorTypeColor = (type: Doctor["type"]) => {
    switch (type) {
      case "government":
        return "bg-green-100";
      case "independent":
        return "bg-blue-100";
      case "commercial":
        return "bg-purple-100";
      default:
        return "bg-gray-100";
    }
  };

  const getDoctorTypeTextColor = (type: Doctor["type"]) => {
    switch (type) {
      case "government":
        return "text-green-800";
      case "independent":
        return "text-blue-800";
      case "commercial":
        return "text-purple-800";
      default:
        return "text-gray-800";
    }
  };

  const handleCallDoctor = (phone: string) => {
    makePhoneCall(phone);
  };

  return (
    <ScreenWithNav activeScreen="DoctorSearch">
      <SafeAreaView className="flex-1 bg-blue-50" edges={["top"]}>
        {/* Header */}
        <View className="bg-white px-4 py-3 border-b border-gray-200">
          <View className="flex-row items-center mb-3">
            <TouchableOpacity
              onPress={() => navigation.goBack()}
              className="mr-3"
            >
              <Text className="text-blue-600 text-lg">←</Text>
            </TouchableOpacity>
            <Text className="text-lg font-semibold text-gray-900">
              {t.findDoctor || "Find Doctor"}
            </Text>
          </View>

          {/* Search Bar */}
          <View className="bg-gray-100 rounded-xl px-4 py-3 flex-row items-center">
            <Text className="text-gray-400 mr-2">🔍</Text>
            <TextInput
              placeholder={t.searchDoctors || "Search doctors..."}
              placeholderTextColor="#9CA3AF"
              value={searchQuery}
              onChangeText={setSearchQuery}
              className="flex-1 text-gray-900"
            />
          </View>
        </View>

        <ScrollView className="flex-1 px-4 pt-4">
          {/* Results info */}
          <View className="flex-row justify-between items-center mb-3">
            <Text className="text-sm text-gray-600">
              {filteredDoctors.length} doctors found
            </Text>
            <Text className="text-xs text-green-600">
              ✓ Government doctors prioritized
            </Text>
          </View>

          {isLoading ? (
            <View className="items-center py-8">
              <ActivityIndicator size="large" color="#2563EB" />
              <Text className="text-gray-600 mt-3">
                {t.loading || "Loading..."}
              </Text>
            </View>
          ) : filteredDoctors.length === 0 ? (
            <Card className="p-8 items-center">
              <Text className="text-gray-500">No doctors found</Text>
              <Text className="text-sm text-gray-400 mt-1">
                Try a different search
              </Text>
            </Card>
          ) : (
            filteredDoctors.map((doctor) => (
              <PressableCard
                key={doctor.id}
                className="mb-3 overflow-hidden"
                onPress={() =>
                  navigation.navigate("DoctorDetails", { doctorId: doctor.id })
                }
              >
                <View className="p-4">
                  {/* Doctor Header */}
                  <View className="flex-row justify-between items-start mb-3">
                    <View className="flex-1">
                      <Text className="font-semibold text-gray-900 mb-1">
                        {doctor.name}
                      </Text>
                      <Text className="text-sm text-blue-600 mb-2">
                        {doctor.specialty}
                      </Text>
                      <View
                        className={`${getDoctorTypeColor(doctor.type)} self-start px-2 py-1 rounded`}
                      >
                        <Text
                          className={`text-xs font-medium ${getDoctorTypeTextColor(doctor.type)}`}
                        >
                          {getDoctorTypeLabel(doctor.type)}
                        </Text>
                      </View>
                    </View>
                    <View className="items-end">
                      <View className="flex-row items-center">
                        <Text className="text-gray-400 mr-1">📍</Text>
                        <Text className="text-sm font-semibold text-gray-600">
                          {doctor.distance} km
                        </Text>
                      </View>
                    </View>
                  </View>

                  {/* Doctor Details */}
                  <View className="space-y-2 mb-3">
                    <View className="flex-row items-start">
                      <Text className="text-gray-400 mr-2">🏥</Text>
                      <Text className="text-sm text-gray-600 flex-1">
                        {doctor.address}
                      </Text>
                    </View>
                    {doctor.experience > 0 && (
                      <View className="flex-row items-center">
                        <Text className="text-gray-400 mr-2">⭐</Text>
                        <Text className="text-sm text-gray-600">
                          {doctor.experience} years experience
                        </Text>
                      </View>
                    )}
                  </View>

                  {/* Languages */}
                  {doctor.languages.length > 0 && (
                    <View className="flex-row flex-wrap gap-1 mt-2">
                      {doctor.languages.map((lang, idx) => (
                        <View
                          key={idx}
                          className="bg-gray-100 px-2 py-1 rounded"
                        >
                          <Text className="text-xs text-gray-700">{lang}</Text>
                        </View>
                      ))}
                    </View>
                  )}
                </View>

                {/* Call Button */}
                <View className="border-t border-gray-200 p-3 bg-gray-50">
                  <Button
                    onPress={() => handleCallDoctor(doctor.phone)}
                    className="bg-green-600"
                  >
                    <View className="flex-row items-center justify-center">
                      <Text className="text-white mr-2">📞</Text>
                      <Text className="text-white font-medium">
                        {t.call || "Call"} {doctor.phone}
                      </Text>
                    </View>
                  </Button>
                </View>
              </PressableCard>
            ))
          )}
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
