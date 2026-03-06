// ─────────────────────────────────────────────
// Sahayak Mobile - Symptom Entry Screen
// Voice recording + text input for symptom triage
// ─────────────────────────────────────────────

import React, { useState, useEffect } from "react";
import { View, Text, ScrollView, TouchableOpacity, TextInput, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScreenProps } from "../navigation/types";
import { useApp } from "../context/AppContext";
import { getTranslations, isVoiceInputSupported } from "../i18n";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Alert, WarningAlert } from "../components/ui/Alert";
import { ScreenWithNav } from "../components/navigation/BottomNav";
import { useAudioRecorder } from "../hooks/useAudioRecorder";
import { analyzeSymptoms, playAudioResponse, stopAudio } from "../services/api";

export default function SymptomEntryScreen({
  navigation,
}: ScreenProps<"SymptomEntry">) {
  const { language, setSymptoms, setTriageResult, setIsLoading, setApiError } = useApp();
  const t = getTranslations(language);

  const [symptomText, setSymptomText] = useState("");
  const [aiResponse, setAiResponse] = useState("");
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [audioBase64, setAudioBase64] = useState<string | null>(null);
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);

  // Audio recording hook
  const {
    isRecording,
    startRecording,
    stopRecording,
    recordingUri,
    duration,
    error: recordingError,
  } = useAudioRecorder();

  const voiceSupported = isVoiceInputSupported(language);

  // Handle recording completion
  useEffect(() => {
    if (recordingUri && !isRecording) {
      // Recording just finished, run analysis
      runAnalysisWithAudio(recordingUri);
    }
  }, [recordingUri, isRecording]);

  const handleRecordToggle = async () => {
    if (isRecording) {
      await stopRecording();
    } else {
      await startRecording();
      // Auto-stop after 10 seconds
      setTimeout(async () => {
        await stopRecording();
      }, 10000);
    }
  };

  const runAnalysisWithAudio = async (audioUri: string) => {
    setIsAnalyzing(true);
    setIsLoading(true);
    setApiError(null);

    try {
      const result = await analyzeSymptoms(audioUri, language, {}, undefined);
      setTriageResult(result);

      if (result.success) {
        setAiResponse(result.responseText || result.summary || "");
        if (result.audioBase64) {
          setAudioBase64(result.audioBase64);
        }
        
        if (result.isEmergency) {
          navigation.navigate("EmergencyMode");
        }
      } else {
        setApiError(result.error || "Analysis failed");
        setAiResponse("Unable to analyze. Please try again.");
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Network error";
      setApiError(msg);
      setAiResponse("Unable to connect to AI service. Please check your internet connection.");
    } finally {
      setIsAnalyzing(false);
      setIsLoading(false);
    }
  };

  const runAnalysisWithText = async () => {
    if (!symptomText.trim()) return;

    setIsAnalyzing(true);
    setIsLoading(true);
    setApiError(null);
    setSymptoms(symptomText);

    try {
      const result = await analyzeSymptoms(null, language, {}, symptomText);
      setTriageResult(result);

      if (result.success) {
        setAiResponse(result.responseText || result.summary || "");
        if (result.audioBase64) {
          setAudioBase64(result.audioBase64);
        }

        if (result.isEmergency) {
          navigation.navigate("EmergencyMode");
        }
      } else {
        setApiError(result.error || "Analysis failed");
        setAiResponse("Unable to analyze. Please try again.");
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Network error";
      setApiError(msg);
      
      // Client-side emergency keyword check
      const emergencyKeywords = ["chest pain", "difficulty breathing", "unconscious", "severe bleeding", "heart attack", "stroke"];
      const isEmergency = emergencyKeywords.some(kw => symptomText.toLowerCase().includes(kw));
      
      if (isEmergency) {
        navigation.navigate("EmergencyMode");
      } else {
        setAiResponse("Unable to connect. Please consult a doctor if symptoms persist.");
      }
    } finally {
      setIsAnalyzing(false);
      setIsLoading(false);
    }
  };

  const handlePlayAudio = async () => {
    if (!audioBase64) return;
    
    if (isPlayingAudio) {
      await stopAudio();
      setIsPlayingAudio(false);
    } else {
      setIsPlayingAudio(true);
      await playAudioResponse(audioBase64);
      setIsPlayingAudio(false);
    }
  };

  const handleFindDoctors = () => {
    navigation.navigate("DoctorSearch");
  };

  return (
    <ScreenWithNav activeScreen="SymptomEntry">
      <SafeAreaView className="flex-1 bg-gray-50" edges={["top"]}>
        {/* Header */}
        <View className="bg-white shadow-sm px-4 py-3 flex-row items-center">
          <TouchableOpacity 
            onPress={() => navigation.goBack()}
            className="p-2 -ml-2"
          >
            <Text className="text-2xl">←</Text>
          </TouchableOpacity>
          <Text className="font-semibold text-lg ml-2">
            {t.symptomEntry || "Symptom Checker"}
          </Text>
        </View>

        <ScrollView className="flex-1 p-4" showsVerticalScrollIndicator={false}>
          {/* Voice Input Section */}
          {voiceSupported && (
            <Card className="p-6 mb-4">
              <View className="items-center">
                <Text className="text-gray-700 mb-4 text-center">
                  {t.speakSymptoms || "Speak Your Symptoms"}
                </Text>
                
                <TouchableOpacity
                  onPress={handleRecordToggle}
                  disabled={isAnalyzing}
                  className={`w-20 h-20 rounded-full items-center justify-center ${
                    isRecording ? "bg-sahayak-red" : "bg-sahayak-blue"
                  }`}
                  activeOpacity={0.7}
                >
                  <Text className="text-4xl">{isRecording ? "⬛" : "🎤"}</Text>
                </TouchableOpacity>

                {isRecording && (
                  <View className="mt-3 items-center">
                    <Text className="text-sahayak-red animate-pulse">
                      Recording... ({duration}s)
                    </Text>
                    <Text className="text-sm text-gray-500 mt-1">
                      Tap to stop
                    </Text>
                  </View>
                )}

                {recordingError && (
                  <Text className="text-sahayak-red text-sm mt-2 text-center">
                    {recordingError}
                  </Text>
                )}
              </View>
            </Card>
          )}

          {/* Text Input Section */}
          <Card className="p-6 mb-4">
            <Text className="text-gray-700 mb-3">
              {voiceSupported ? "Or type your symptoms:" : "Describe your symptoms:"}
            </Text>
            
            <TextInput
              placeholder={t.typeSymptoms || "E.g., I have a headache and fever..."}
              value={symptomText}
              onChangeText={setSymptomText}
              multiline
              numberOfLines={4}
              textAlignVertical="top"
              className="bg-gray-50 border border-gray-200 rounded-lg p-3 min-h-[100px] text-base text-gray-900"
              placeholderTextColor="#9ca3af"
            />

            <Button
              onPress={runAnalysisWithText}
              disabled={!symptomText.trim() || isAnalyzing}
              loading={isAnalyzing}
              className="mt-4"
            >
              {isAnalyzing ? "Analyzing..." : "🔍 Analyze Symptoms"}
            </Button>
          </Card>

          {/* AI Response */}
          {aiResponse && (
            <Card className="p-6 mb-4 bg-blue-50 border-blue-200">
              <View className="flex-row items-start mb-4">
                <View className="w-10 h-10 bg-sahayak-blue rounded-full items-center justify-center mr-3">
                  <Text className="text-xl">🤖</Text>
                </View>
                <View className="flex-1">
                  <Text className="font-semibold text-blue-900 mb-2">
                    AI Analysis
                  </Text>
                  <Text className="text-blue-800 text-sm leading-5">
                    {aiResponse}
                  </Text>
                </View>
              </View>

              {audioBase64 && (
                <Button
                  variant="outline"
                  size="sm"
                  onPress={handlePlayAudio}
                  className="mb-3"
                >
                  {isPlayingAudio ? "⏸️ Stop Audio" : "🔊 Play Audio Response"}
                </Button>
              )}

              <Button onPress={handleFindDoctors}>
                🏥 {t.findDoctors || "Find Doctors"}
              </Button>
            </Card>
          )}

          {/* Emergency Warning */}
          <Card className="p-4 bg-yellow-50 border-yellow-200 mb-6">
            <View className="flex-row items-start">
              <Text className="text-xl mr-3">⚠️</Text>
              <Text className="flex-1 text-sm text-yellow-800">
                If you are experiencing severe or life-threatening symptoms, please call emergency services immediately at{" "}
                <Text className="font-bold">108</Text>
              </Text>
            </View>
          </Card>
        </ScrollView>
      </SafeAreaView>
    </ScreenWithNav>
  );
}
