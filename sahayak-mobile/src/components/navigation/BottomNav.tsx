// ─────────────────────────────────────────────
// Sahayak Mobile - Bottom Navigation Component
// Standalone component that works with Stack Navigator
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, TouchableOpacity, StyleSheet, Platform } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import type { RootStackParamList } from "../../navigation/types";

// ─────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────

type NavigableScreen = "Home" | "SymptomEntry" | "PrescriptionSearch" | "DoctorSearch" | "UserProfile";

interface NavItem {
  screen: NavigableScreen;
  icon: string;
  label: string;
}

const navItems: NavItem[] = [
  { screen: "Home", icon: "🏠", label: "Home" },
  { screen: "SymptomEntry", icon: "🩺", label: "Symptoms" },
  { screen: "PrescriptionSearch", icon: "💊", label: "Rx" },
  { screen: "DoctorSearch", icon: "👨‍⚕️", label: "Doctors" },
  { screen: "UserProfile", icon: "👤", label: "Profile" },
];

// ─────────────────────────────────────────────
// BottomNavBar Component
// ─────────────────────────────────────────────

interface BottomNavBarProps {
  activeScreen?: NavigableScreen;
}

export function BottomNavBar({ activeScreen }: BottomNavBarProps) {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute();

  // Determine active screen from route if not provided
  const currentScreen = activeScreen || (route.name as NavigableScreen);

  return (
    <View style={styles.navBar}>
      {navItems.map((item) => {
        const isActive = currentScreen === item.screen;
        return (
          <TouchableOpacity
            key={item.screen}
            onPress={() => navigation.navigate(item.screen)}
            style={styles.navItem}
            activeOpacity={0.7}
          >
            <Text style={[styles.navIcon, isActive && styles.navIconActive]}>
              {item.icon}
            </Text>
            <Text style={[styles.navLabel, isActive && styles.navLabelActive]}>
              {item.label}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

// ─────────────────────────────────────────────
// Screen Wrapper with Bottom Nav
// ─────────────────────────────────────────────

interface ScreenWithNavProps {
  children: React.ReactNode;
  showNav?: boolean;
  activeScreen?: NavigableScreen;
}

/**
 * Wrapper component that includes bottom navigation.
 * Use this to wrap screen content when you want the nav bar visible.
 */
export function ScreenWithNav({
  children,
  showNav = true,
  activeScreen,
}: ScreenWithNavProps) {
  return (
    <View style={styles.screenContainer}>
      <View style={styles.content}>{children}</View>
      {showNav && <BottomNavBar activeScreen={activeScreen} />}
    </View>
  );
}

// ─────────────────────────────────────────────
// Styles
// ─────────────────────────────────────────────

const styles = StyleSheet.create({
  screenContainer: {
    flex: 1,
  },
  content: {
    flex: 1,
    paddingBottom: Platform.OS === "ios" ? 85 : 70, // Space for nav bar
  },
  navBar: {
    flexDirection: "row",
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    height: Platform.OS === "ios" ? 85 : 70,
    paddingBottom: Platform.OS === "ios" ? 25 : 10,
    paddingTop: 10,
    backgroundColor: "#ffffff",
    borderTopWidth: 1,
    borderTopColor: "#e5e7eb",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 10,
  },
  navItem: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  navIcon: {
    fontSize: 24,
    marginBottom: 2,
    opacity: 0.6,
  },
  navIconActive: {
    opacity: 1,
  },
  navLabel: {
    fontSize: 10,
    color: "#9ca3af",
    fontWeight: "500",
  },
  navLabelActive: {
    color: "#2563eb",
    fontWeight: "600",
  },
});
