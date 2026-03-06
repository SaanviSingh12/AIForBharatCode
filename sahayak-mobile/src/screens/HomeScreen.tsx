// ─────────────────────────────────────────────
// Sahayak Mobile - Home Screen
// Main entry point with two main flows:
// 1. Symptom Entry (Voice triage)
// 2. Prescription Search (Generic medicines)
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ScrollView, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";
import { Card, PressableCard } from "../components/ui/Card";
import { ScreenWithNav } from "../components/navigation/BottomNav";

interface MenuItem {
  title: string;
  subtitle: string;
  icon: string;
  color: string;
  screen: "SymptomEntry" | "DoctorSearch" | "PrescriptionSearch" | "UserProfile";
}

export default function HomeScreen({ navigation }: ScreenProps<"Home">) {
  const { language, userProfile } = useApp();
  const t = getTranslations(language);

  const menuItems: MenuItem[] = [
    {
      title: t.symptomEntry || "Symptom Checker",
      subtitle: "AI-Powered Analysis",
      icon: "❤️",
      color: "bg-red-500",
      screen: "SymptomEntry",
    },
    {
      title: t.findDoctor || "Find Doctor",
      subtitle: "Govt & Private Doctors",
      icon: "🩺",
      color: "bg-blue-500",
      screen: "DoctorSearch",
    },
    {
      title: t.prescription || "Prescription",
      subtitle: "Generic Medicines",
      icon: "💊",
      color: "bg-green-500",
      screen: "PrescriptionSearch",
    },
    {
      title: t.profile || "Profile",
      subtitle: "Your Details",
      icon: "👤",
      color: "bg-purple-500",
      screen: "UserProfile",
    },
  ];

  const userName = userProfile?.name || "User";

  return (
    <ScreenWithNav activeScreen="Home">
      <SafeAreaView className="flex-1 bg-gray-50" edges={["top"]}>
        <ScrollView 
          className="flex-1"
          showsVerticalScrollIndicator={false}
        >
          {/* Header with gradient effect */}
          <View className="bg-sahayak-blue px-6 pt-6 pb-8 rounded-b-3xl">
            <View className="flex-row items-center justify-between mb-4">
              <View>
                <Text className="font-bold text-2xl text-white">Sahayak</Text>
                <Text className="text-blue-100 text-sm">
                  {t.healthcareForAll || "Healthcare for All Indians"}
                </Text>
              </View>
              <TouchableOpacity
                onPress={() => navigation.navigate("UserProfile")}
                className="w-12 h-12 bg-white/20 rounded-full items-center justify-center"
              >
                <Text className="text-2xl">👤</Text>
              </TouchableOpacity>
            </View>

            {/* Welcome Card */}
            <View className="bg-white/10 rounded-xl p-4">
              <Text className="text-blue-100 text-sm">
                {t.welcome || "Welcome"},
              </Text>
              <Text className="font-semibold text-lg text-white">
                {userName}
              </Text>
            </View>
          </View>

          {/* Emergency Alert */}
          <View className="px-6 mt-6">
            <TouchableOpacity
              onPress={() => navigation.navigate("EmergencyMode")}
              className="bg-red-50 border-2 border-red-200 rounded-xl p-4 flex-row items-center"
              activeOpacity={0.7}
            >
              <View className="w-10 h-10 bg-sahayak-red rounded-full items-center justify-center">
                <Text className="text-xl">🚨</Text>
              </View>
              <View className="flex-1 ml-3">
                <Text className="font-semibold text-red-900">
                  {t.emergency || "Emergency"}
                </Text>
                <Text className="text-sm text-red-700">
                  Tap for emergency numbers
                </Text>
              </View>
              <Text className="text-red-400 text-xl">›</Text>
            </TouchableOpacity>
          </View>

          {/* Services Menu */}
          <View className="px-6 mt-6">
            <Text className="font-semibold text-gray-900 mb-1">Services</Text>
            {language !== "en" && language !== "en-IN" && (
              <Text className="text-xs text-gray-500 italic mb-2">Services</Text>
            )}

            <View className="flex-row flex-wrap justify-between mt-2">
              {menuItems.map((item, index) => (
                <PressableCard
                  key={index}
                  onPress={() => navigation.navigate(item.screen)}
                  className="w-[48%] p-5 mb-4"
                >
                  <View
                    className={`w-14 h-14 ${item.color} rounded-2xl items-center justify-center mb-4`}
                  >
                    <Text className="text-2xl">{item.icon}</Text>
                  </View>
                  <Text className="font-semibold text-gray-900 mb-1">
                    {item.title}
                  </Text>
                  {language !== "en" && language !== "en-IN" && (
                    <Text className="text-xs text-gray-500 italic">
                      {["Symptom Entry", "Find Doctor", "Prescription", "Profile"][index]}
                    </Text>
                  )}
                  <Text className="text-xs text-gray-600 mt-1">
                    {item.subtitle}
                  </Text>
                </PressableCard>
              ))}
            </View>
          </View>

          {/* Government Schemes */}
          <View className="px-6 mt-2 mb-6">
            <Text className="font-semibold text-gray-900 mb-1">
              Government Schemes
            </Text>
            {language !== "en" && language !== "en-IN" && (
              <Text className="text-xs text-gray-500 italic mb-2">
                Government Schemes
              </Text>
            )}

            <Card className="p-4 bg-orange-50 border-orange-200 mt-2">
              <Text className="font-semibold text-orange-900 mb-1">PM-JAY</Text>
              {language !== "en" && language !== "en-IN" && (
                <Text className="text-xs text-orange-600 italic">PM-JAY</Text>
              )}
              <Text className="text-sm text-orange-800 mt-1">
                Ayushman Bharat - Free healthcare up to ₹5 lakhs
              </Text>
            </Card>

            <Card className="p-4 bg-green-50 border-green-200 mt-3">
              <Text className="font-semibold text-green-900 mb-1">
                Jan Aushadhi
              </Text>
              {language !== "en" && language !== "en-IN" && (
                <Text className="text-xs text-green-600 italic">Jan Aushadhi</Text>
              )}
              <Text className="text-sm text-green-800 mt-1">
                Generic medicines at affordable prices - Save up to 85%
              </Text>
            </Card>
          </View>
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
