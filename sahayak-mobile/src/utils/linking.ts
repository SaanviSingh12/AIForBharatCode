// ─────────────────────────────────────────────
// Sahayak Mobile - Linking Utilities
// Replaces web window.location.href for tel:, maps, etc.
// Uses expo-linking for native deep linking
// ─────────────────────────────────────────────

import * as Linking from "expo-linking";
import { Alert, Platform } from "react-native";

/**
 * Make a phone call.
 * Replaces: window.location.href = `tel:${number}`
 *
 * @param phoneNumber - Phone number to dial (can include + prefix)
 */
export async function makePhoneCall(phoneNumber: string): Promise<void> {
  // Clean the phone number (remove spaces, dashes, etc.)
  const cleanNumber = phoneNumber.replace(/[\s\-\(\)]/g, "");
  const url = `tel:${cleanNumber}`;

  try {
    const canOpen = await Linking.canOpenURL(url);
    if (canOpen) {
      await Linking.openURL(url);
    } else {
      Alert.alert(
        "Cannot Make Call",
        `Unable to dial ${phoneNumber}. Please ensure you have a phone app installed.`,
        [{ text: "OK" }]
      );
    }
  } catch (error) {
    console.error("makePhoneCall error:", error);
    Alert.alert("Error", "Failed to make phone call. Please try again.");
  }
}

/**
 * Send an SMS message.
 *
 * @param phoneNumber - Phone number to send SMS to
 * @param message - Optional pre-filled message
 */
export async function sendSMS(
  phoneNumber: string,
  message?: string
): Promise<void> {
  const cleanNumber = phoneNumber.replace(/[\s\-\(\)]/g, "");

  // SMS URL format differs by platform
  let url: string;
  if (Platform.OS === "ios") {
    url = message
      ? `sms:${cleanNumber}&body=${encodeURIComponent(message)}`
      : `sms:${cleanNumber}`;
  } else {
    url = message
      ? `sms:${cleanNumber}?body=${encodeURIComponent(message)}`
      : `sms:${cleanNumber}`;
  }

  try {
    const canOpen = await Linking.canOpenURL(url);
    if (canOpen) {
      await Linking.openURL(url);
    } else {
      Alert.alert("Cannot Send SMS", "Unable to open SMS app.");
    }
  } catch (error) {
    console.error("sendSMS error:", error);
    Alert.alert("Error", "Failed to open SMS. Please try again.");
  }
}

/**
 * Open Google Maps or Apple Maps with directions to a location.
 *
 * @param address - Address or place name to navigate to
 * @param coords - Optional coordinates { lat, lng }
 */
export async function openMapsDirections(
  address: string,
  coords?: { lat: number; lng: number }
): Promise<void> {
  let url: string;

  if (coords) {
    // Use coordinates if available (more accurate)
    if (Platform.OS === "ios") {
      url = `maps:?daddr=${coords.lat},${coords.lng}`;
    } else {
      url = `geo:${coords.lat},${coords.lng}?q=${coords.lat},${coords.lng}`;
    }
  } else {
    // Fall back to address search
    const encodedAddress = encodeURIComponent(address);
    if (Platform.OS === "ios") {
      url = `maps:?q=${encodedAddress}`;
    } else {
      url = `geo:0,0?q=${encodedAddress}`;
    }
  }

  try {
    const canOpen = await Linking.canOpenURL(url);
    if (canOpen) {
      await Linking.openURL(url);
    } else {
      // Fall back to Google Maps web URL
      const webUrl = coords
        ? `https://www.google.com/maps/dir/?api=1&destination=${coords.lat},${coords.lng}`
        : `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
      await Linking.openURL(webUrl);
    }
  } catch (error) {
    console.error("openMapsDirections error:", error);
    Alert.alert("Error", "Failed to open maps. Please try again.");
  }
}

/**
 * Open a URL in the default browser.
 *
 * @param url - URL to open
 */
export async function openURL(url: string): Promise<void> {
  try {
    const canOpen = await Linking.canOpenURL(url);
    if (canOpen) {
      await Linking.openURL(url);
    } else {
      Alert.alert("Cannot Open Link", `Unable to open ${url}`);
    }
  } catch (error) {
    console.error("openURL error:", error);
    Alert.alert("Error", "Failed to open link. Please try again.");
  }
}

/**
 * Open email client with pre-filled recipient.
 *
 * @param email - Email address
 * @param subject - Optional email subject
 * @param body - Optional email body
 */
export async function sendEmail(
  email: string,
  subject?: string,
  body?: string
): Promise<void> {
  let url = `mailto:${email}`;
  const params: string[] = [];

  if (subject) params.push(`subject=${encodeURIComponent(subject)}`);
  if (body) params.push(`body=${encodeURIComponent(body)}`);

  if (params.length > 0) {
    url += `?${params.join("&")}`;
  }

  try {
    await Linking.openURL(url);
  } catch (error) {
    console.error("sendEmail error:", error);
    Alert.alert("Error", "Failed to open email app. Please try again.");
  }
}
