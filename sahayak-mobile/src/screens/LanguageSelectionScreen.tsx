// ─────────────────────────────────────────────
// Sahayak Mobile - Language Selection Screen
// First screen users see to choose their language
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ScrollView, Pressable } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { useApp } from "../context/AppContext";
import { languages, getTranslations } from "../i18n";
import { Card } from "../components/ui/Card";

export default function LanguageSelectionScreen({
  navigation,
}: ScreenProps<"LanguageSelection">) {
  const { language, setLanguage } = useApp();
  const t = getTranslations(language);

  const handleLanguageSelect = (langCode: string) => {
    // Convert to AWS format (e.g., "hi" -> "hi-IN")
    const awsCode = `${langCode}-IN`;
    setLanguage(langCode === "en" ? "en-IN" : awsCode);
    navigation.navigate("Home");
  };

  return (
    <SafeAreaView className="flex-1 bg-gray-50">
      {/* Header */}
      <View className="bg-white shadow-sm px-6 py-5">
        <View className="flex-row items-center justify-center">
          <View className="w-12 h-12 bg-sahayak-blue rounded-2xl items-center justify-center mr-3">
            <Text className="text-2xl">🌐</Text>
          </View>
          <View>
            <Text className="font-bold text-2xl text-gray-900">Sahayak</Text>
            <Text className="text-sm text-gray-600">सहायक • Healthcare for All</Text>
          </View>
        </View>
      </View>

      {/* Language Selection */}
      <ScrollView 
        className="flex-1 px-6 pt-6"
        showsVerticalScrollIndicator={false}
      >
        <View className="items-center mb-6">
          <Text className="text-xl font-semibold text-gray-900 text-center">
            Choose Your Language
          </Text>
          <Text className="text-xl text-gray-700 text-center mt-1">
            अपनी भाषा चुनें
          </Text>
          <Text className="text-gray-500 text-center mt-2">
            {t.selectLanguage}
          </Text>
        </View>

        <View className="pb-6">
          {languages.map((lang) => (
            <Pressable
              key={lang.code}
              onPress={() => handleLanguageSelect(lang.code)}
              className="mb-3 active:opacity-70"
            >
              <Card className="p-4 flex-row items-center justify-between">
                <View className="flex-1">
                  <Text className="text-lg font-semibold text-gray-900">
                    {lang.nativeName}
                  </Text>
                  <Text className="text-sm text-gray-500">{lang.name}</Text>
                </View>
                <View className="flex-row items-center">
                  {lang.voiceInput && (
                    <View className="bg-green-100 px-2 py-1 rounded-full mr-2">
                      <Text className="text-xs text-green-700">🎤 Voice</Text>
                    </View>
                  )}
                  <Text className="text-gray-400 text-xl">›</Text>
                </View>
              </Card>
            </Pressable>
          ))}
        </View>

        {/* Info Box */}
        <View className="bg-blue-50 rounded-xl p-4 mb-8">
          <Text className="text-sm text-gray-700 text-center">
            Powered by PM-JAY & Jan Aushadhi Scheme
          </Text>
          <Text className="text-xs text-gray-600 text-center mt-1">
            Ayushman Bharat Digital Mission
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
