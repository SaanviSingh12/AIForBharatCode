// ─────────────────────────────────────────────
// Sahayak Mobile - Audio Recording Hook
// Replaces web navigator.mediaDevices / MediaRecorder
// Uses expo-audio for native audio recording
// ─────────────────────────────────────────────

import { useState, useCallback, useEffect, useRef } from "react";
import {
  useAudioRecorder as useExpoAudioRecorder,
  useAudioRecorderState,
  requestRecordingPermissionsAsync,
  setAudioModeAsync,
  type RecordingOptions,
  AudioQuality,
  IOSOutputFormat,
} from "expo-audio";

export interface UseAudioRecorderResult {
  // State
  isRecording: boolean;
  isPrepared: boolean;
  recordingUri: string | null;
  duration: number;
  error: string | null;

  // Actions
  prepareRecording: () => Promise<boolean>;
  startRecording: () => Promise<void>;
  stopRecording: () => Promise<string | null>;
  cancelRecording: () => Promise<void>;
}

/**
 * Custom recording options optimised for speech recognition:
 * 16 kHz mono AAC in .m4a container.
 */
const SPEECH_RECORDING_OPTIONS: RecordingOptions = {
  extension: ".m4a",
  sampleRate: 16000,
  numberOfChannels: 1,
  bitRate: 128000,
  android: {
    outputFormat: "mpeg4",
    audioEncoder: "aac",
    sampleRate: 16000,
  },
  ios: {
    outputFormat: IOSOutputFormat.MPEG4AAC,
    audioQuality: AudioQuality.HIGH,
    sampleRate: 16000,
    linearPCMBitDepth: 16,
    linearPCMIsBigEndian: false,
    linearPCMIsFloat: false,
  },
  web: {
    mimeType: "audio/webm",
    bitsPerSecond: 128000,
  },
};

/**
 * Hook for audio recording using expo-audio.
 * Handles permissions, recording lifecycle, and cleanup.
 *
 * Usage:
 * ```tsx
 * const { isRecording, startRecording, stopRecording, recordingUri } = useAudioRecorder();
 *
 * const handleRecord = async () => {
 *   if (isRecording) {
 *     const uri = await stopRecording();
 *     // Send uri to API
 *   } else {
 *     await startRecording();
 *   }
 * };
 * ```
 */
export function useAudioRecorder(): UseAudioRecorderResult {
  const [isPrepared, setIsPrepared] = useState(false);
  const [recordingUri, setRecordingUri] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // expo-audio recorder hook — manages lifecycle automatically
  const recorder = useExpoAudioRecorder(SPEECH_RECORDING_OPTIONS);
  const recorderState = useAudioRecorderState(recorder, 500);

  // Derive duration (seconds) from the recorder state
  const duration = Math.floor((recorderState.durationMillis ?? 0) / 1000);
  const isRecording = recorderState.isRecording;

  /**
   * Request permissions and configure audio mode for recording.
   */
  const prepareRecording = useCallback(async (): Promise<boolean> => {
    try {
      setError(null);

      const { granted } = await requestRecordingPermissionsAsync();
      if (!granted) {
        setError("Microphone permission denied");
        return false;
      }

      await setAudioModeAsync({
        allowsRecording: true,
        playsInSilentMode: true,
      });

      setIsPrepared(true);
      return true;
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to prepare recording";
      setError(message);
      console.error("prepareRecording error:", err);
      return false;
    }
  }, []);

  /**
   * Start recording audio.
   * Will request permissions if not already prepared.
   */
  const startRecording = useCallback(async (): Promise<void> => {
    try {
      setError(null);

      if (!isPrepared) {
        const ok = await prepareRecording();
        if (!ok) return;
      }

      // Prepare the recorder and start
      await recorder.prepareToRecordAsync();
      recorder.record();
      setRecordingUri(null);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to start recording";
      setError(message);
      console.error("startRecording error:", err);
    }
  }, [isPrepared, prepareRecording, recorder]);

  /**
   * Stop recording and return the file URI.
   */
  const stopRecording = useCallback(async (): Promise<string | null> => {
    try {
      setError(null);

      await recorder.stop();
      const uri = recorder.uri ?? null;
      setRecordingUri(uri);

      // Reset audio mode for playback
      await setAudioModeAsync({
        allowsRecording: false,
        playsInSilentMode: true,
      });

      return uri;
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to stop recording";
      setError(message);
      console.error("stopRecording error:", err);
      return null;
    }
  }, [recorder]);

  /**
   * Cancel current recording without saving.
   */
  const cancelRecording = useCallback(async (): Promise<void> => {
    try {
      await recorder.stop();
      setRecordingUri(null);
    } catch (err) {
      console.error("cancelRecording error:", err);
    }
  }, [recorder]);

  return {
    isRecording,
    isPrepared,
    recordingUri,
    duration,
    error,
    prepareRecording,
    startRecording,
    stopRecording,
    cancelRecording,
  };
}
