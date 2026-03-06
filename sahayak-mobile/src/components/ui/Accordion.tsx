// ─────────────────────────────────────────────
// Sahayak Mobile - Accordion Component
// Native equivalent of shadcn Accordion
// For doctor details, medicine info, etc.
// ─────────────────────────────────────────────

import React, { useState, useCallback, useRef, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Animated,
  LayoutAnimation,
  Platform,
  UIManager,
  ViewProps,
} from "react-native";

// Enable LayoutAnimation on Android
if (Platform.OS === "android" && UIManager.setLayoutAnimationEnabledExperimental) {
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

// ─────────────────────────────────────────────
// Accordion Context
// ─────────────────────────────────────────────

interface AccordionContextValue {
  expandedItems: Set<string>;
  toggleItem: (id: string) => void;
  type: "single" | "multiple";
}

const AccordionContext = React.createContext<AccordionContextValue | null>(null);

function useAccordionContext() {
  const context = React.useContext(AccordionContext);
  if (!context) {
    throw new Error("Accordion components must be used within an Accordion");
  }
  return context;
}

// ─────────────────────────────────────────────
// Accordion Root
// ─────────────────────────────────────────────

interface AccordionProps extends ViewProps {
  type?: "single" | "multiple";
  defaultValue?: string | string[];
  children: React.ReactNode;
  className?: string;
}

export function Accordion({
  type = "single",
  defaultValue,
  children,
  className = "",
  ...props
}: AccordionProps) {
  const [expandedItems, setExpandedItems] = useState<Set<string>>(() => {
    if (!defaultValue) return new Set();
    if (Array.isArray(defaultValue)) return new Set(defaultValue);
    return new Set([defaultValue]);
  });

  const toggleItem = useCallback(
    (id: string) => {
      LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);

      setExpandedItems((prev) => {
        const next = new Set(prev);
        if (next.has(id)) {
          next.delete(id);
        } else {
          if (type === "single") {
            next.clear();
          }
          next.add(id);
        }
        return next;
      });
    },
    [type]
  );

  return (
    <AccordionContext.Provider value={{ expandedItems, toggleItem, type }}>
      <View className={`${className}`} {...props}>
        {children}
      </View>
    </AccordionContext.Provider>
  );
}

// ─────────────────────────────────────────────
// Accordion Item
// ─────────────────────────────────────────────

interface AccordionItemContextValue {
  id: string;
  isExpanded: boolean;
}

const AccordionItemContext = React.createContext<AccordionItemContextValue | null>(null);

function useAccordionItemContext() {
  const context = React.useContext(AccordionItemContext);
  if (!context) {
    throw new Error("AccordionItem components must be used within an AccordionItem");
  }
  return context;
}

interface AccordionItemProps extends ViewProps {
  value: string;
  children: React.ReactNode;
  className?: string;
}

export function AccordionItem({
  value,
  children,
  className = "",
  ...props
}: AccordionItemProps) {
  const { expandedItems } = useAccordionContext();
  const isExpanded = expandedItems.has(value);

  return (
    <AccordionItemContext.Provider value={{ id: value, isExpanded }}>
      <View
        className={`
          border-b border-gray-200 last:border-b-0
          ${className}
        `}
        {...props}
      >
        {children}
      </View>
    </AccordionItemContext.Provider>
  );
}

// ─────────────────────────────────────────────
// Accordion Trigger
// ─────────────────────────────────────────────

interface AccordionTriggerProps {
  children: React.ReactNode;
  className?: string;
}

export function AccordionTrigger({
  children,
  className = "",
}: AccordionTriggerProps) {
  const { toggleItem } = useAccordionContext();
  const { id, isExpanded } = useAccordionItemContext();
  const rotateAnim = useRef(new Animated.Value(isExpanded ? 1 : 0)).current;

  useEffect(() => {
    Animated.timing(rotateAnim, {
      toValue: isExpanded ? 1 : 0,
      duration: 200,
      useNativeDriver: true,
    }).start();
  }, [isExpanded, rotateAnim]);

  const rotation = rotateAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ["0deg", "180deg"],
  });

  return (
    <TouchableOpacity
      onPress={() => toggleItem(id)}
      activeOpacity={0.7}
      className={`
        flex-row items-center justify-between py-4 pr-1
        ${className}
      `}
    >
      <View className="flex-1 pr-4">
        {typeof children === "string" ? (
          <Text className="text-base font-medium text-gray-900">{children}</Text>
        ) : (
          children
        )}
      </View>
      <Animated.Text
        style={{ transform: [{ rotate: rotation }] }}
        className="text-gray-500 text-lg"
      >
        ▼
      </Animated.Text>
    </TouchableOpacity>
  );
}

// ─────────────────────────────────────────────
// Accordion Content
// ─────────────────────────────────────────────

interface AccordionContentProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function AccordionContent({
  children,
  className = "",
  ...props
}: AccordionContentProps) {
  const { isExpanded } = useAccordionItemContext();

  if (!isExpanded) {
    return null;
  }

  return (
    <View
      className={`
        pb-4
        ${className}
      `}
      {...props}
    >
      {children}
    </View>
  );
}

// ─────────────────────────────────────────────
// Pre-composed Doctor Details Accordion
// ─────────────────────────────────────────────

interface DoctorDetailsAccordionProps {
  doctor: {
    name: string;
    specialty: string;
    hospital: string;
    address?: string;
    phone?: string;
    availability?: string;
    experience?: string;
    qualifications?: string[];
  };
  className?: string;
}

export function DoctorDetailsAccordion({
  doctor,
  className = "",
}: DoctorDetailsAccordionProps) {
  return (
    <Accordion type="single" className={className}>
      <AccordionItem value="contact">
        <AccordionTrigger>
          <Text className="text-base font-medium text-gray-900">Contact Information</Text>
        </AccordionTrigger>
        <AccordionContent>
          {doctor.address && (
            <View className="flex-row mb-2">
              <Text className="text-gray-500 w-20">Address:</Text>
              <Text className="text-gray-700 flex-1">{doctor.address}</Text>
            </View>
          )}
          {doctor.phone && (
            <View className="flex-row mb-2">
              <Text className="text-gray-500 w-20">Phone:</Text>
              <Text className="text-sahayak-blue flex-1">{doctor.phone}</Text>
            </View>
          )}
        </AccordionContent>
      </AccordionItem>

      <AccordionItem value="availability">
        <AccordionTrigger>
          <Text className="text-base font-medium text-gray-900">Availability</Text>
        </AccordionTrigger>
        <AccordionContent>
          <Text className="text-gray-700">
            {doctor.availability || "Contact hospital for availability"}
          </Text>
        </AccordionContent>
      </AccordionItem>

      {doctor.qualifications && doctor.qualifications.length > 0 && (
        <AccordionItem value="qualifications">
          <AccordionTrigger>
            <Text className="text-base font-medium text-gray-900">Qualifications</Text>
          </AccordionTrigger>
          <AccordionContent>
            {doctor.qualifications.map((qual, index) => (
              <Text key={index} className="text-gray-700 mb-1">
                • {qual}
              </Text>
            ))}
          </AccordionContent>
        </AccordionItem>
      )}
    </Accordion>
  );
}

// ─────────────────────────────────────────────
// Pre-composed Medicine Details Accordion
// ─────────────────────────────────────────────

interface MedicineDetailsAccordionProps {
  medicine: {
    brandName: string;
    genericName: string;
    dosage?: string;
    sideEffects?: string[];
    instructions?: string;
    warnings?: string[];
  };
  className?: string;
}

export function MedicineDetailsAccordion({
  medicine,
  className = "",
}: MedicineDetailsAccordionProps) {
  return (
    <Accordion type="multiple" className={className}>
      <AccordionItem value="dosage">
        <AccordionTrigger>
          <Text className="text-base font-medium text-gray-900">Dosage & Instructions</Text>
        </AccordionTrigger>
        <AccordionContent>
          {medicine.dosage && (
            <Text className="text-gray-700 mb-2">Dosage: {medicine.dosage}</Text>
          )}
          {medicine.instructions && (
            <Text className="text-gray-700">{medicine.instructions}</Text>
          )}
        </AccordionContent>
      </AccordionItem>

      {medicine.sideEffects && medicine.sideEffects.length > 0 && (
        <AccordionItem value="sideEffects">
          <AccordionTrigger>
            <Text className="text-base font-medium text-gray-900">Side Effects</Text>
          </AccordionTrigger>
          <AccordionContent>
            {medicine.sideEffects.map((effect, index) => (
              <Text key={index} className="text-gray-700 mb-1">
                • {effect}
              </Text>
            ))}
          </AccordionContent>
        </AccordionItem>
      )}

      {medicine.warnings && medicine.warnings.length > 0 && (
        <AccordionItem value="warnings">
          <AccordionTrigger>
            <Text className="text-base font-medium text-sahayak-orange">⚠️ Warnings</Text>
          </AccordionTrigger>
          <AccordionContent>
            {medicine.warnings.map((warning, index) => (
              <Text key={index} className="text-orange-700 mb-1">
                • {warning}
              </Text>
            ))}
          </AccordionContent>
        </AccordionItem>
      )}
    </Accordion>
  );
}
