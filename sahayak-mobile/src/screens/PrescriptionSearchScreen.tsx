import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  TextInput,
  ScrollView,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { ScreenWithNav } from "../components/navigation/BottomNav";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { ErrorAlert } from "../components/ui/Alert";
import { useApp } from "../context/AppContext";
import { getTranslations } from "../i18n";
import {
  analyzePrescription,
  analyzePrescriptionText,
} from "../services/api";
import { takePhoto, pickImage, type ImagePickerResult } from "../utils/imagePicker";

export default function PrescriptionSearchScreen({
  navigation,
}: ScreenProps<"PrescriptionSearch">) {
  const {
    language,
    setPrescription,
    setPrescriptionResult,
    setIsLoading,
    setApiError,
  } = useApp();
  const t = getTranslations(language);

  const [textInput, setTextInput] = useState("");
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleTakePhoto = async () => {
    const result = await takePhoto();
    if (result) {
      setImageUri(result.uri);
      processImage(result.uri);
    }
  };

  const handlePickImage = async () => {
    const result = await pickImage();
    if (result) {
      setImageUri(result.uri);
      processImage(result.uri);
    }
  };

  const processImage = async (uri: string) => {
    setIsProcessing(true);
    setIsLoading(true);
    setErrorMsg(null);

    try {
      const result = await analyzePrescription(uri, language);
      setPrescriptionResult(result);

      if (result.success) {
        setPrescription(result.extractedText || "");
        navigation.navigate("PharmacyResults");
      } else {
        setErrorMsg(result.error || "Failed to process prescription");
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Network error";
      setErrorMsg(msg);
      setApiError(msg);
    } finally {
      setIsProcessing(false);
      setIsLoading(false);
    }
  };

  const handleTextSearch = async () => {
    if (!textInput.trim()) return;

    setIsProcessing(true);
    setIsLoading(true);
    setErrorMsg(null);

    try {
      const result = await analyzePrescriptionText(textInput, language);
      setPrescriptionResult(result);

      if (result.success) {
        setPrescription(textInput);
        navigation.navigate("PharmacyResults");
      } else {
        setErrorMsg(result.error || "Failed to analyze medicines");
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Network error";
      setErrorMsg(msg);
      setApiError(msg);
    } finally {
      setIsProcessing(false);
      setIsLoading(false);
    }
  };

  const handleBrowsePharmacies = () => {
    navigation.navigate("PharmacyResults");
  };

  return (
    <ScreenWithNav activeScreen="PrescriptionSearch">
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
            <Text className="text-lg font-semibold text-gray-900">
              {t.prescription || "Prescription"}
            </Text>
          </View>
        </View>

        <ScrollView className="flex-1 px-4 pt-4">
          {/* Error Display */}
          {errorMsg && (
            <ErrorAlert
              title={t.error || "Error"}
              message={errorMsg}
              className="mb-4"
            />
          )}

          {/* Upload Prescription Card */}
          <Card className="p-4 mb-4">
            <View className="flex-row items-center mb-4">
              <View className="w-12 h-12 bg-blue-100 rounded-xl items-center justify-center mr-3">
                <Text className="text-2xl">📤</Text>
              </View>
              <View className="flex-1">
                <Text className="font-semibold text-gray-900">
                  {t.uploadPrescription || "Upload Prescription"}
                </Text>
                <Text className="text-sm text-gray-600">
                  Get generic medicine suggestions
                </Text>
              </View>
            </View>

            {imageUri ? (
              <View className="mb-4">
                <Image
                  source={{ uri: imageUri }}
                  className="w-full h-48 rounded-lg"
                  resizeMode="contain"
                />
                {isProcessing && (
                  <View className="mt-4 bg-blue-50 rounded-lg p-4 items-center">
                    <ActivityIndicator size="small" color="#2563EB" />
                    <Text className="text-blue-600 font-semibold mt-2">
                      Processing prescription...
                    </Text>
                    <Text className="text-sm text-blue-600 mt-1">
                      Extracting medicines with AI
                    </Text>
                  </View>
                )}
              </View>
            ) : (
              <TouchableOpacity
                onPress={handleTakePhoto}
                activeOpacity={0.8}
                className="border-2 border-dashed border-gray-300 rounded-xl p-8 items-center"
              >
                <Text className="text-4xl mb-3">📷</Text>
                <Text className="text-gray-700 font-semibold mb-1">
                  {t.takePicture || "Take Photo or Upload"}
                </Text>
                <Text className="text-sm text-gray-500">
                  Tap to open camera
                </Text>
              </TouchableOpacity>
            )}

            {!imageUri && (
              <Button
                variant="outline"
                onPress={handlePickImage}
                className="mt-3"
              >
                <Text className="text-blue-600 font-medium">
                  Choose from Gallery
                </Text>
              </Button>
            )}
          </Card>

          {/* Text Search Card */}
          <Card className="p-4 mb-4">
            <View className="flex-row items-center mb-4">
              <View className="w-12 h-12 bg-green-100 rounded-xl items-center justify-center mr-3">
                <Text className="text-2xl">📝</Text>
              </View>
              <View className="flex-1">
                <Text className="font-semibold text-gray-900">
                  Type Medicine Names
                </Text>
                <Text className="text-sm text-gray-600">
                  Search for specific medicines
                </Text>
              </View>
            </View>

            <TextInput
              placeholder="Enter medicine names (e.g., Paracetamol, Amoxicillin)"
              placeholderTextColor="#9CA3AF"
              value={textInput}
              onChangeText={setTextInput}
              multiline
              numberOfLines={3}
              className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900 mb-3"
              textAlignVertical="top"
            />

            <Button
              onPress={handleTextSearch}
              disabled={!textInput.trim() || isProcessing}
              className="bg-green-600"
            >
              {isProcessing ? (
                <View className="flex-row items-center justify-center">
                  <ActivityIndicator size="small" color="#FFFFFF" />
                  <Text className="text-white ml-2">
                    {t.analyzing || "Analyzing..."}
                  </Text>
                </View>
              ) : (
                <View className="flex-row items-center justify-center">
                  <Text className="text-white mr-2">🔍</Text>
                  <Text className="text-white font-medium">
                    Search Medicines
                  </Text>
                </View>
              )}
            </Button>
          </Card>

          {/* Browse Pharmacies Card */}
          <Card className="p-4 mb-4">
            <View className="flex-row items-center mb-4">
              <View className="w-12 h-12 bg-purple-100 rounded-xl items-center justify-center mr-3">
                <Text className="text-2xl">🏪</Text>
              </View>
              <View className="flex-1">
                <Text className="font-semibold text-gray-900">
                  Browse Pharmacies
                </Text>
                <Text className="text-sm text-gray-600">
                  Find nearby Jan Aushadhi stores
                </Text>
              </View>
            </View>

            <Button variant="outline" onPress={handleBrowsePharmacies}>
              <Text className="text-purple-600 font-medium">
                View All Pharmacies
              </Text>
            </Button>
          </Card>

          {/* Info Card */}
          <Card className="p-4 bg-green-50 border-green-200 mb-6">
            <Text className="text-green-800 font-semibold mb-2">
              💡 Did you know?
            </Text>
            <Text className="text-sm text-green-700">
              Generic medicines at Jan Aushadhi Kendras save you up to 80%
              compared to branded medicines with the same quality!
            </Text>
          </Card>
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
