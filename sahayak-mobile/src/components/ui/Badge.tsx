// ─────────────────────────────────────────────
// Sahayak Mobile - Badge Component
// Native equivalent of shadcn Badge
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ViewProps } from "react-native";

type BadgeVariant = "default" | "secondary" | "destructive" | "outline" | "success" | "warning";

interface BadgeProps extends ViewProps {
  variant?: BadgeVariant;
  children: React.ReactNode;
  className?: string;
  icon?: React.ReactNode;
}

const variantStyles: Record<BadgeVariant, { container: string; text: string }> = {
  default: {
    container: "bg-sahayak-blue",
    text: "text-white",
  },
  secondary: {
    container: "bg-gray-100",
    text: "text-gray-700",
  },
  destructive: {
    container: "bg-sahayak-red",
    text: "text-white",
  },
  outline: {
    container: "bg-transparent border border-gray-300",
    text: "text-gray-700",
  },
  success: {
    container: "bg-sahayak-green",
    text: "text-white",
  },
  warning: {
    container: "bg-sahayak-orange",
    text: "text-white",
  },
};

export function Badge({
  variant = "default",
  children,
  className = "",
  icon,
  ...props
}: BadgeProps) {
  const styles = variantStyles[variant];

  return (
    <View
      className={`
        flex-row items-center px-2.5 py-1 rounded-full
        ${styles.container}
        ${className}
      `}
      {...props}
    >
      {icon && <View className="mr-1">{icon}</View>}
      {typeof children === "string" ? (
        <Text className={`text-xs font-medium ${styles.text}`}>{children}</Text>
      ) : (
        children
      )}
    </View>
  );
}

// Status badge for urgency levels
type UrgencyLevel = "emergency" | "urgent" | "semi-urgent" | "non-urgent";

const urgencyStyles: Record<UrgencyLevel, BadgeVariant> = {
  emergency: "destructive",
  urgent: "warning",
  "semi-urgent": "default",
  "non-urgent": "success",
};

const urgencyLabels: Record<UrgencyLevel, string> = {
  emergency: "Emergency",
  urgent: "Urgent",
  "semi-urgent": "Semi-Urgent",
  "non-urgent": "Non-Urgent",
};

interface UrgencyBadgeProps extends Omit<BadgeProps, "variant" | "children"> {
  level: UrgencyLevel;
}

export function UrgencyBadge({ level, ...props }: UrgencyBadgeProps) {
  return (
    <Badge variant={urgencyStyles[level]} {...props}>
      {urgencyLabels[level]}
    </Badge>
  );
}

// Specialty badge for medical specialties
interface SpecialtyBadgeProps extends Omit<BadgeProps, "variant" | "children"> {
  specialty: string;
}

export function SpecialtyBadge({ specialty, ...props }: SpecialtyBadgeProps) {
  return (
    <Badge variant="secondary" {...props}>
      {specialty}
    </Badge>
  );
}

// Free/Paid badge for hospitals/services
interface PriceBadgeProps extends Omit<BadgeProps, "variant" | "children"> {
  isFree: boolean;
}

export function PriceBadge({ isFree, ...props }: PriceBadgeProps) {
  return (
    <Badge variant={isFree ? "success" : "secondary"} {...props}>
      {isFree ? "Free" : "Paid"}
    </Badge>
  );
}

// Government badge for govt hospitals
export function GovtBadge(props: Omit<BadgeProps, "variant" | "children">) {
  return (
    <Badge variant="default" {...props}>
      Govt
    </Badge>
  );
}
