// ─────────────────────────────────────────────
// Sahayak Mobile - Emergency Mode Screen
// Red-themed emergency display with call actions
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ScrollView, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { makePhoneCall } from "../utils/linking";

const emergencyNumbers = [
  { name: "Ambulance (National)", number: "108", icon: "🚑" },
  { name: "Medical Emergency", number: "102", icon: "❤️" },
  { name: "Police Emergency", number: "100", icon: "🛡️" },
  { name: "Fire Emergency", number: "101", icon: "🔥" },
  { name: "Women Helpline", number: "1091", icon: "👩" },
  { name: "Child Helpline", number: "1098", icon: "👶" },
];

export default function EmergencyModeScreen({
  navigation,
}: ScreenProps<"EmergencyMode">) {
  const handleCall = (number: string) => {
    makePhoneCall(number);
  };

  return (
    <View className="flex-1 bg-sahayak-red">
      <SafeAreaView className="flex-1">
        {/* Header */}
        <View className="px-4 pt-4">
          <TouchableOpacity
            onPress={() => navigation.goBack()}
            className="w-10 h-10 bg-white/20 rounded-full items-center justify-center mb-4"
          >
            <Text className="text-white text-xl">←</Text>
          </TouchableOpacity>

          {/* Emergency Title */}
          <View className="items-center mb-6">
            <View className="w-20 h-20 bg-white/20 rounded-full items-center justify-center mb-4">
              <Text className="text-4xl">🚨</Text>
            </View>
            <Text className="font-bold text-3xl text-white mb-2">
              EMERGENCY MODE
            </Text>
            <Text className="text-red-100">आपातकालीन मोड</Text>
          </View>
        </View>

        {/* Emergency Alert */}
        <View className="px-4 mb-4">
          <Card className="bg-white/10 border-white/20 p-4">
            <Text className="text-white text-center text-sm">
              ⚠️ If you are experiencing a life-threatening emergency, call an
              ambulance immediately
            </Text>
          </Card>
        </View>

        {/* Emergency Contacts */}
        <ScrollView className="flex-1 px-4" showsVerticalScrollIndicator={false}>
          <Text className="font-semibold text-xl text-center text-white mb-4">
            Emergency Numbers
          </Text>

          {emergencyNumbers.map((emergency, index) => (
            <Card key={index} className="bg-white mb-3 overflow-hidden">
              <View className="flex-row items-center p-4">
                <View className="w-14 h-14 bg-red-100 rounded-xl items-center justify-center mr-4">
                  <Text className="text-2xl">{emergency.icon}</Text>
                </View>
                <View className="flex-1">
                  <Text className="font-semibold text-gray-900 mb-1">
                    {emergency.name}
                  </Text>
                  <Text className="text-2xl font-bold text-sahayak-red">
                    {emergency.number}
                  </Text>
                </View>
              </View>

              <View className="border-t border-gray-200 p-3 bg-red-50">
                <TouchableOpacity
                  onPress={() => handleCall(emergency.number)}
                  className="bg-sahayak-red py-3 rounded-lg flex-row items-center justify-center"
                  activeOpacity={0.8}
                >
                  <Text className="text-xl mr-2">📞</Text>
                  <Text className="text-white font-semibold text-base">
                    CALL {emergency.number}
                  </Text>
                </TouchableOpacity>
              </View>
            </Card>
          ))}

          {/* Guidelines */}
          <Card className="bg-white/10 border-white/20 p-4 mb-4">
            <Text className="font-semibold text-white mb-3">
              Important Guidelines
            </Text>
            <View className="space-y-2">
              {[
                "Stay calm and speak clearly when calling",
                "Provide your exact location",
                "Describe the emergency situation",
                "Follow the operator's instructions",
                "Don't hang up until told to do so",
              ].map((guideline, index) => (
                <Text key={index} className="text-red-100 text-sm">
                  • {guideline}
                </Text>
              ))}
            </View>
          </Card>

          {/* Safety Notice */}
          <Card className="bg-yellow-500 p-4 mb-6">
            <View className="flex-row items-start">
              <Text className="text-xl mr-3">⚠️</Text>
              <View className="flex-1">
                <Text className="font-semibold text-yellow-900 mb-1">
                  Medical Emergency Symptoms
                </Text>
                <Text className="text-sm text-yellow-900">
                  Chest pain, difficulty breathing, severe bleeding,
                  unconsciousness, stroke symptoms, severe burns, or suspected
                  heart attack - Call 108 immediately!
                </Text>
              </View>
            </View>
          </Card>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}
