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
import { Card } from "../components/ui/Card";
import { Badge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";
import { getDoctorById, type DoctorDto } from "../services/api";
import { makePhoneCall, openMapsDirections } from "../utils/linking";

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

export default function DoctorDetailsScreen({
  navigation,
  route,
}: ScreenProps<"DoctorDetails">) {
  const { doctorId } = route.params;
  const { language } = useApp();
  const t = getTranslations(language);

  const [doctor, setDoctor] = useState<Doctor | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!doctorId) return;
    let cancelled = false;

    const fetchDoctor = async () => {
      setIsLoading(true);
      try {
        const data: DoctorDto = await getDoctorById(doctorId);
        if (!cancelled) {
          setDoctor({
            id: data.id,
            name: data.name,
            specialty: data.specialty,
            type: data.type as Doctor["type"],
            distance: data.distance,
            phone: data.phone,
            address: data.address,
            experience: data.experience,
            languages: data.languages || [],
          });
        }
      } catch {
        // Keep loading state, show error
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };
    fetchDoctor();

    return () => {
      cancelled = true;
    };
  }, [doctorId]);

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

  const getDoctorTypeLabel = (type: Doctor["type"]) => {
    switch (type) {
      case "government":
        return "🏛️ Government Doctor";
      case "independent":
        return "🏥 Independent Practice";
      case "commercial":
        return "🏨 Commercial Hospital";
      default:
        return type;
    }
  };

  const handleCall = () => {
    if (doctor) {
      makePhoneCall(doctor.phone);
    }
  };

  const handleDirections = () => {
    if (doctor) {
      openMapsDirections(doctor.address);
    }
  };

  if (isLoading) {
    return (
      <SafeAreaView className="flex-1 bg-blue-50 items-center justify-center">
        <ActivityIndicator size="large" color="#2563EB" />
        <Text className="text-gray-600 mt-4">
          {t.loading || "Loading..."}
        </Text>
      </SafeAreaView>
    );
  }

  if (!doctor) {
    return (
      <SafeAreaView className="flex-1 bg-blue-50 items-center justify-center px-6">
        <Text className="text-xl mb-2">Doctor not found</Text>
        <Button onPress={() => navigation.goBack()}>
          <Text className="text-white">{t.back || "Back"}</Text>
        </Button>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView className="flex-1 bg-blue-50">
      <ScrollView className="flex-1">
        {/* Header with gradient */}
        <View className="bg-blue-600 px-4 pt-4 pb-6 rounded-b-3xl">
          <TouchableOpacity
            onPress={() => navigation.goBack()}
            className="mb-4"
          >
            <Text className="text-white text-lg">← {t.back || "Back"}</Text>
          </TouchableOpacity>

          <View className="flex-row items-start">
            <View className="w-20 h-20 bg-white/20 rounded-2xl items-center justify-center mr-4">
              <Text className="text-4xl">👨‍⚕️</Text>
            </View>
            <View className="flex-1">
              <Text className="font-bold text-xl text-white mb-1">
                {doctor.name}
              </Text>
              <Text className="text-blue-100 mb-2">{doctor.specialty}</Text>
              <View className="flex-row items-center">
                <Text className="text-white mr-1">📍</Text>
                <Text className="text-white text-sm">
                  {doctor.distance} km away
                </Text>
              </View>
            </View>
          </View>
        </View>

        <View className="px-4 pt-4">
          {/* Type Badge */}
          <View className="items-center mb-4">
            <View
              className={`${getDoctorTypeColor(doctor.type)} px-4 py-2 rounded-full`}
            >
              <Text
                className={`${getDoctorTypeTextColor(doctor.type)} font-medium`}
              >
                {getDoctorTypeLabel(doctor.type)}
              </Text>
            </View>
          </View>

          {/* Details Card */}
          <Card className="p-4 mb-4">
            <View className="space-y-4">
              <View className="flex-row items-start">
                <Text className="text-gray-400 mr-3 text-lg">🏥</Text>
                <View className="flex-1">
                  <Text className="text-sm text-gray-600 mb-1">Address</Text>
                  <Text className="text-gray-900">{doctor.address}</Text>
                </View>
              </View>

              <View className="flex-row items-start">
                <Text className="text-gray-400 mr-3 text-lg">⭐</Text>
                <View className="flex-1">
                  <Text className="text-sm text-gray-600 mb-1">Experience</Text>
                  <Text className="text-gray-900">
                    {doctor.experience} years
                  </Text>
                </View>
              </View>

              {doctor.languages.length > 0 && (
                <View className="flex-row items-start">
                  <Text className="text-gray-400 mr-3 text-lg">🗣️</Text>
                  <View className="flex-1">
                    <Text className="text-sm text-gray-600 mb-2">
                      Languages Spoken
                    </Text>
                    <View className="flex-row flex-wrap gap-2">
                      {doctor.languages.map((lang, idx) => (
                        <View
                          key={idx}
                          className="bg-gray-100 px-3 py-1 rounded-full"
                        >
                          <Text className="text-sm text-gray-700">{lang}</Text>
                        </View>
                      ))}
                    </View>
                  </View>
                </View>
              )}

              <View className="flex-row items-start">
                <Text className="text-gray-400 mr-3 text-lg">📞</Text>
                <View className="flex-1">
                  <Text className="text-sm text-gray-600 mb-1">Contact</Text>
                  <Text className="text-gray-900 font-semibold">
                    {doctor.phone}
                  </Text>
                </View>
              </View>
            </View>
          </Card>

          {/* Timings Card */}
          <Card className="p-4 mb-4">
            <View className="flex-row items-center mb-4">
              <Text className="text-gray-400 mr-2 text-lg">🕐</Text>
              <Text className="font-semibold text-gray-900">
                Consultation Hours
              </Text>
            </View>
            <View className="space-y-2">
              <View className="flex-row justify-between">
                <Text className="text-gray-600">Monday - Friday</Text>
                <Text className="text-gray-900 font-semibold">
                  9:00 AM - 5:00 PM
                </Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-gray-600">Saturday</Text>
                <Text className="text-gray-900 font-semibold">
                  9:00 AM - 1:00 PM
                </Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-gray-600">Sunday</Text>
                <Text className="text-red-600 font-semibold">Closed</Text>
              </View>
            </View>
          </Card>

          {/* PM-JAY Info */}
          {doctor.type === "government" && (
            <Card className="p-4 bg-orange-50 border-orange-200 mb-4">
              <View className="flex-row items-start">
                <View className="w-10 h-10 bg-orange-500 rounded-full items-center justify-center mr-3">
                  <Text className="text-white">🏆</Text>
                </View>
                <View className="flex-1">
                  <Text className="font-semibold text-orange-900 mb-1">
                    PM-JAY Empanelled
                  </Text>
                  <Text className="text-sm text-orange-800">
                    Free consultation and treatment available under Ayushman
                    Bharat scheme
                  </Text>
                </View>
              </View>
            </Card>
          )}

          {/* Action Buttons */}
          <Button onPress={handleCall} className="bg-green-600 mb-3">
            <View className="flex-row items-center justify-center">
              <Text className="text-white mr-2 text-lg">📞</Text>
              <Text className="text-white font-semibold text-lg">
                {t.call || "Call"} Now
              </Text>
            </View>
          </Button>

          <Button onPress={handleDirections} className="bg-blue-600 mb-6">
            <View className="flex-row items-center justify-center">
              <Text className="text-white mr-2 text-lg">🗺️</Text>
              <Text className="text-white font-semibold text-lg">
                Get Directions
              </Text>
            </View>
          </Button>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
