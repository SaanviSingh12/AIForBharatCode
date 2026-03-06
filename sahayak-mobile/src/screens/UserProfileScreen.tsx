import React, { useState } from "react";
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  TextInput,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { ScreenWithNav } from "../components/navigation/BottomNav";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";

export default function UserProfileScreen({
  navigation,
}: ScreenProps<"UserProfile">) {
  const { language, userProfile, setUserProfile } = useApp();
  const t = getTranslations(language);

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState(userProfile);

  const handleSave = () => {
    setUserProfile(formData);
    setIsEditing(false);
  };

  const handleChange = (field: string, value: string) => {
    setFormData({ ...formData, [field]: value });
  };

  return (
    <ScreenWithNav activeScreen="UserProfile">
      <SafeAreaView className="flex-1 bg-blue-50" edges={["top"]}>
        <ScrollView className="flex-1">
          {/* Header with gradient */}
          <View className="bg-blue-600 px-4 pt-4 pb-6 rounded-b-3xl">
            <View className="flex-row items-center justify-between mb-6">
              <TouchableOpacity onPress={() => navigation.goBack()}>
                <Text className="text-white text-lg">
                  ← {t.back || "Back"}
                </Text>
              </TouchableOpacity>
              <Text className="font-semibold text-lg text-white">
                {t.profile || "Profile"}
              </Text>
              <TouchableOpacity onPress={() => setIsEditing(!isEditing)}>
                <Text className="text-white text-lg">✏️</Text>
              </TouchableOpacity>
            </View>

            <View className="items-center">
              <View className="w-24 h-24 bg-white/20 rounded-full items-center justify-center mb-3">
                <Text className="text-5xl">👤</Text>
              </View>
              <Text className="font-bold text-xl text-white">
                {userProfile.name || "Guest User"}
              </Text>
              <Text className="text-blue-100">
                {userProfile.age ? `${userProfile.age} years` : ""}{" "}
                {userProfile.age && userProfile.gender ? "•" : ""}{" "}
                {userProfile.gender}
              </Text>
            </View>
          </View>

          <View className="px-4 pt-4">
            {/* Personal Information */}
            <Card className="p-4 mb-4">
              <Text className="font-semibold text-gray-900 mb-4">
                Personal Information
              </Text>

              {isEditing ? (
                <View className="space-y-4">
                  <View>
                    <Text className="text-sm text-gray-600 mb-1">
                      Full Name
                    </Text>
                    <TextInput
                      value={formData.name}
                      onChangeText={(val) => handleChange("name", val)}
                      placeholder="Enter your name"
                      placeholderTextColor="#9CA3AF"
                      className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                    />
                  </View>
                  <View>
                    <Text className="text-sm text-gray-600 mb-1">Age</Text>
                    <TextInput
                      value={formData.age}
                      onChangeText={(val) => handleChange("age", val)}
                      placeholder="Enter your age"
                      placeholderTextColor="#9CA3AF"
                      keyboardType="numeric"
                      className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                    />
                  </View>
                  <View>
                    <Text className="text-sm text-gray-600 mb-1">Gender</Text>
                    <TextInput
                      value={formData.gender}
                      onChangeText={(val) => handleChange("gender", val)}
                      placeholder="Enter your gender"
                      placeholderTextColor="#9CA3AF"
                      className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                    />
                  </View>
                  <Button onPress={handleSave} className="bg-green-600">
                    <View className="flex-row items-center justify-center">
                      <Text className="text-white mr-2">💾</Text>
                      <Text className="text-white font-medium">
                        Save Changes
                      </Text>
                    </View>
                  </Button>
                </View>
              ) : (
                <View className="space-y-3">
                  <View className="flex-row items-center">
                    <Text className="text-gray-400 mr-3 text-xl">👤</Text>
                    <View>
                      <Text className="text-sm text-gray-600">Name</Text>
                      <Text className="text-gray-900 font-semibold">
                        {userProfile.name || "Not set"}
                      </Text>
                    </View>
                  </View>
                  <View className="flex-row items-center">
                    <Text className="text-gray-400 mr-3 text-xl">📅</Text>
                    <View>
                      <Text className="text-sm text-gray-600">Age</Text>
                      <Text className="text-gray-900 font-semibold">
                        {userProfile.age
                          ? `${userProfile.age} years`
                          : "Not set"}
                      </Text>
                    </View>
                  </View>
                  <View className="flex-row items-center">
                    <Text className="text-gray-400 mr-3 text-xl">👥</Text>
                    <View>
                      <Text className="text-sm text-gray-600">Gender</Text>
                      <Text className="text-gray-900 font-semibold">
                        {userProfile.gender || "Not set"}
                      </Text>
                    </View>
                  </View>
                </View>
              )}
            </Card>

            {/* Contact Information */}
            <Card className="p-4 mb-4">
              <Text className="font-semibold text-gray-900 mb-4">
                Contact Information
              </Text>
              <View className="space-y-3">
                <View className="flex-row items-center">
                  <Text className="text-gray-400 mr-3 text-xl">📞</Text>
                  <View>
                    <Text className="text-sm text-gray-600">Phone Number</Text>
                    <Text className="text-gray-900 font-semibold">
                      +91 98765 43210
                    </Text>
                  </View>
                </View>
                <View className="flex-row items-center">
                  <Text className="text-gray-400 mr-3 text-xl">📍</Text>
                  <View>
                    <Text className="text-sm text-gray-600">Location</Text>
                    <Text className="text-gray-900 font-semibold">
                      New Delhi, India
                    </Text>
                  </View>
                </View>
              </View>
            </Card>

            {/* PM-JAY Status */}
            <Card className="p-4 bg-orange-50 border-orange-200 mb-4">
              <View className="flex-row items-start">
                <View className="w-12 h-12 bg-orange-500 rounded-xl items-center justify-center mr-3">
                  <Text className="text-white text-2xl">🏆</Text>
                </View>
                <View className="flex-1">
                  <Text className="font-semibold text-orange-900 mb-1">
                    PM-JAY Status
                  </Text>
                  <Text className="text-sm text-orange-800 mb-3">
                    Ayushman Bharat Health Card
                  </Text>
                  <View className="bg-white rounded-lg p-3">
                    <View className="flex-row justify-between mb-2">
                      <Text className="text-sm text-gray-600">Card Number</Text>
                      <Text className="text-sm font-semibold text-gray-900">
                        1234-5678-9012
                      </Text>
                    </View>
                    <View className="flex-row justify-between">
                      <Text className="text-sm text-gray-600">Coverage</Text>
                      <Text className="text-sm font-semibold text-green-600">
                        ₹5,00,000
                      </Text>
                    </View>
                  </View>
                </View>
              </View>
            </Card>

            {/* Medical History */}
            <Card className="p-4 mb-4">
              <Text className="font-semibold text-gray-900 mb-4">
                Medical History
              </Text>
              <View className="space-y-3">
                <View className="flex-row items-start bg-blue-50 rounded-lg p-3">
                  <Text className="text-blue-600 mr-2">❤️</Text>
                  <View>
                    <Text className="font-semibold text-gray-900">
                      Diabetes Type 2
                    </Text>
                    <Text className="text-xs text-gray-600">
                      Diagnosed Jan 2023
                    </Text>
                  </View>
                </View>
              </View>
            </Card>

            {/* Settings */}
            <Text className="font-semibold text-gray-900 mb-3">Settings</Text>

            <TouchableOpacity
              className="bg-white border border-gray-200 rounded-xl p-4 mb-3 flex-row justify-between items-center"
              onPress={() => navigation.navigate("LanguageSelection")}
            >
              <View className="flex-row items-center">
                <Text className="text-xl mr-3">🌐</Text>
                <View>
                  <Text className="text-gray-900 font-medium">Language</Text>
                  <Text className="text-gray-500 text-sm">
                    Change app language
                  </Text>
                </View>
              </View>
              <Text className="text-gray-400">→</Text>
            </TouchableOpacity>

            <TouchableOpacity className="bg-white border border-gray-200 rounded-xl p-4 mb-3 flex-row justify-between items-center">
              <View className="flex-row items-center">
                <Text className="text-xl mr-3">📍</Text>
                <View>
                  <Text className="text-gray-900 font-medium">Location</Text>
                  <Text className="text-gray-500 text-sm">
                    Update your location
                  </Text>
                </View>
              </View>
              <Text className="text-gray-400">→</Text>
            </TouchableOpacity>

            <TouchableOpacity className="bg-white border border-gray-200 rounded-xl p-4 mb-3 flex-row justify-between items-center">
              <View className="flex-row items-center">
                <Text className="text-xl mr-3">ℹ️</Text>
                <View>
                  <Text className="text-gray-900 font-medium">
                    About Sahayak
                  </Text>
                  <Text className="text-gray-500 text-sm">Version 1.0.0</Text>
                </View>
              </View>
              <Text className="text-gray-400">→</Text>
            </TouchableOpacity>

            {/* Government Info */}
            <Card className="p-4 bg-blue-50 border-blue-200 mt-4 mb-8">
              <Text className="text-blue-800 font-semibold mb-2">
                🏛️ Government Healthcare Initiative
              </Text>
              <Text className="text-sm text-blue-700">
                Sahayak helps connect rural India with government healthcare
                services including PM-JAY and Jan Aushadhi Kendras, ensuring
                affordable and accessible healthcare for all.
              </Text>
            </Card>
          </View>
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
