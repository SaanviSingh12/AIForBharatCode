import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { RootStackParamList, Routes } from "./types";

// Import screens (placeholders for now)
import LanguageSelectionScreen from "../screens/LanguageSelectionScreen";
import HomeScreen from "../screens/HomeScreen";
import SymptomEntryScreen from "../screens/SymptomEntryScreen";
import DoctorSearchScreen from "../screens/DoctorSearchScreen";
import DoctorDetailsScreen from "../screens/DoctorDetailsScreen";
import EmergencyModeScreen from "../screens/EmergencyModeScreen";
import PrescriptionSearchScreen from "../screens/PrescriptionSearchScreen";
import PharmacyResultsScreen from "../screens/PharmacyResultsScreen";
import UserProfileScreen from "../screens/UserProfileScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={Routes.LanguageSelection}
        screenOptions={{
          headerShown: false,
          animation: "slide_from_right",
        }}
      >
        <Stack.Screen
          name={Routes.LanguageSelection}
          component={LanguageSelectionScreen}
        />
        <Stack.Screen name={Routes.Home} component={HomeScreen} />
        <Stack.Screen
          name={Routes.SymptomEntry}
          component={SymptomEntryScreen}
        />
        <Stack.Screen
          name={Routes.DoctorSearch}
          component={DoctorSearchScreen}
        />
        <Stack.Screen
          name={Routes.DoctorDetails}
          component={DoctorDetailsScreen}
        />
        <Stack.Screen
          name={Routes.EmergencyMode}
          component={EmergencyModeScreen}
          options={{
            animation: "fade",
          }}
        />
        <Stack.Screen
          name={Routes.PrescriptionSearch}
          component={PrescriptionSearchScreen}
        />
        <Stack.Screen
          name={Routes.PharmacyResults}
          component={PharmacyResultsScreen}
        />
        <Stack.Screen
          name={Routes.UserProfile}
          component={UserProfileScreen}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
