// ─────────────────────────────────────────────
// Sahayak Mobile - Image Picker Utility
// Replaces web <input type="file"> and FileReader
// Uses expo-image-picker for camera/gallery access
// ─────────────────────────────────────────────

import * as ImagePicker from "expo-image-picker";
import { Alert, Platform } from "react-native";

export interface ImagePickerResult {
  uri: string;
  base64?: string;
  width: number;
  height: number;
  type: "image";
}

export interface ImagePickerOptions {
  /** Allow editing/cropping before selection */
  allowsEditing?: boolean;
  /** Image quality 0-1 (lower = smaller file) */
  quality?: number;
  /** Include base64 data (larger memory footprint) */
  includeBase64?: boolean;
  /** Aspect ratio for cropping [width, height] */
  aspect?: [number, number];
}

const defaultOptions: ImagePickerOptions = {
  allowsEditing: false,
  quality: 0.8,
  includeBase64: false,
  aspect: [4, 3],
};

/**
 * Request camera permissions.
 * @returns true if permission granted
 */
export async function requestCameraPermission(): Promise<boolean> {
  const { status } = await ImagePicker.requestCameraPermissionsAsync();
  if (status !== "granted") {
    Alert.alert(
      "Camera Permission Needed",
      "Please allow camera access to take prescription photos.",
      [{ text: "OK" }]
    );
    return false;
  }
  return true;
}

/**
 * Request photo library permissions.
 * @returns true if permission granted
 */
export async function requestMediaLibraryPermission(): Promise<boolean> {
  const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
  if (status !== "granted") {
    Alert.alert(
      "Gallery Permission Needed",
      "Please allow gallery access to select prescription photos.",
      [{ text: "OK" }]
    );
    return false;
  }
  return true;
}

/**
 * Take a photo using the device camera.
 * Ideal for capturing prescriptions.
 *
 * @param options - ImagePicker options
 * @returns Image result with URI, or null if cancelled
 */
export async function takePhoto(
  options: ImagePickerOptions = {}
): Promise<ImagePickerResult | null> {
  const hasPermission = await requestCameraPermission();
  if (!hasPermission) return null;

  const opts = { ...defaultOptions, ...options };

  try {
    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: "images",
      allowsEditing: opts.allowsEditing,
      quality: opts.quality,
      base64: opts.includeBase64,
      aspect: opts.aspect,
      exif: false, // Don't need EXIF data
    });

    if (result.canceled || !result.assets?.[0]) {
      return null;
    }

    const asset = result.assets[0];
    return {
      uri: asset.uri,
      base64: asset.base64 ?? undefined,
      width: asset.width,
      height: asset.height,
      type: "image",
    };
  } catch (error) {
    console.error("takePhoto error:", error);
    Alert.alert("Error", "Failed to take photo. Please try again.");
    return null;
  }
}

/**
 * Pick an image from the device gallery.
 *
 * @param options - ImagePicker options
 * @returns Image result with URI, or null if cancelled
 */
export async function pickImage(
  options: ImagePickerOptions = {}
): Promise<ImagePickerResult | null> {
  const hasPermission = await requestMediaLibraryPermission();
  if (!hasPermission) return null;

  const opts = { ...defaultOptions, ...options };

  try {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: "images",
      allowsEditing: opts.allowsEditing,
      quality: opts.quality,
      base64: opts.includeBase64,
      aspect: opts.aspect,
      exif: false,
    });

    if (result.canceled || !result.assets?.[0]) {
      return null;
    }

    const asset = result.assets[0];
    return {
      uri: asset.uri,
      base64: asset.base64 ?? undefined,
      width: asset.width,
      height: asset.height,
      type: "image",
    };
  } catch (error) {
    console.error("pickImage error:", error);
    Alert.alert("Error", "Failed to select image. Please try again.");
    return null;
  }
}

/**
 * Show action sheet to choose between camera and gallery.
 * Returns selected image or null if cancelled.
 *
 * @param options - ImagePicker options applied to both methods
 */
export async function pickImageWithOptions(
  options: ImagePickerOptions = {}
): Promise<ImagePickerResult | null> {
  // For now, we'll let the calling component handle the choice
  // This function can be extended with ActionSheet if needed
  return pickImage(options);
}
