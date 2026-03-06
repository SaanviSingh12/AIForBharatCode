// ─────────────────────────────────────────────
// Sahayak Mobile - Alert Component
// Native equivalent of shadcn Alert
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ViewProps, TextProps } from "react-native";

type AlertVariant = "default" | "destructive" | "success" | "warning" | "info";

interface AlertProps extends ViewProps {
  variant?: AlertVariant;
  children: React.ReactNode;
  icon?: React.ReactNode;
  className?: string;
}

const variantStyles: Record<
  AlertVariant,
  { container: string; icon: string; title: string; description: string }
> = {
  default: {
    container: "bg-gray-50 border-gray-200",
    icon: "text-gray-600",
    title: "text-gray-900",
    description: "text-gray-600",
  },
  destructive: {
    container: "bg-red-50 border-sahayak-red",
    icon: "text-sahayak-red",
    title: "text-sahayak-red",
    description: "text-red-700",
  },
  success: {
    container: "bg-green-50 border-sahayak-green",
    icon: "text-sahayak-green",
    title: "text-green-800",
    description: "text-green-700",
  },
  warning: {
    container: "bg-orange-50 border-sahayak-orange",
    icon: "text-sahayak-orange",
    title: "text-orange-800",
    description: "text-orange-700",
  },
  info: {
    container: "bg-blue-50 border-sahayak-blue",
    icon: "text-sahayak-blue",
    title: "text-blue-800",
    description: "text-blue-700",
  },
};

export function Alert({
  variant = "default",
  children,
  icon,
  className = "",
  ...props
}: AlertProps) {
  const styles = variantStyles[variant];

  return (
    <View
      className={`
        flex-row items-start p-4 rounded-lg border
        ${styles.container}
        ${className}
      `}
      {...props}
    >
      {icon && <View className={`mr-3 ${styles.icon}`}>{icon}</View>}
      <View className="flex-1">{children}</View>
    </View>
  );
}

interface AlertTitleProps extends TextProps {
  children: React.ReactNode;
  variant?: AlertVariant;
  className?: string;
}

export function AlertTitle({
  children,
  variant = "default",
  className = "",
  ...props
}: AlertTitleProps) {
  const styles = variantStyles[variant];

  return (
    <Text
      className={`
        text-base font-semibold mb-1
        ${styles.title}
        ${className}
      `}
      {...props}
    >
      {children}
    </Text>
  );
}

interface AlertDescriptionProps extends TextProps {
  children: React.ReactNode;
  variant?: AlertVariant;
  className?: string;
}

export function AlertDescription({
  children,
  variant = "default",
  className = "",
  ...props
}: AlertDescriptionProps) {
  const styles = variantStyles[variant];

  return (
    <Text
      className={`
        text-sm
        ${styles.description}
        ${className}
      `}
      {...props}
    >
      {children}
    </Text>
  );
}

// Pre-composed alert variants for common use cases

interface SimpleAlertProps {
  title?: string;
  message: string;
  className?: string;
}

export function ErrorAlert({ title = "Error", message, className }: SimpleAlertProps) {
  return (
    <Alert variant="destructive" className={className}>
      <AlertTitle variant="destructive">{title}</AlertTitle>
      <AlertDescription variant="destructive">{message}</AlertDescription>
    </Alert>
  );
}

export function SuccessAlert({ title = "Success", message, className }: SimpleAlertProps) {
  return (
    <Alert variant="success" className={className}>
      <AlertTitle variant="success">{title}</AlertTitle>
      <AlertDescription variant="success">{message}</AlertDescription>
    </Alert>
  );
}

export function WarningAlert({ title = "Warning", message, className }: SimpleAlertProps) {
  return (
    <Alert variant="warning" className={className}>
      <AlertTitle variant="warning">{title}</AlertTitle>
      <AlertDescription variant="warning">{message}</AlertDescription>
    </Alert>
  );
}

export function InfoAlert({ title = "Info", message, className }: SimpleAlertProps) {
  return (
    <Alert variant="info" className={className}>
      <AlertTitle variant="info">{title}</AlertTitle>
      <AlertDescription variant="info">{message}</AlertDescription>
    </Alert>
  );
}

// Emergency alert for critical situations
interface EmergencyAlertProps {
  message: string;
  onCallEmergency?: () => void;
}

import { TouchableOpacity } from "react-native";

export function EmergencyAlert({ message, onCallEmergency }: EmergencyAlertProps) {
  return (
    <View className="bg-sahayak-red p-4 rounded-lg">
      <Text className="text-white text-lg font-bold mb-2">🚨 Emergency</Text>
      <Text className="text-white text-base mb-3">{message}</Text>
      {onCallEmergency && (
        <TouchableOpacity
          onPress={onCallEmergency}
          className="bg-white py-2 px-4 rounded-lg self-start"
        >
          <Text className="text-sahayak-red font-semibold">Call 112</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}
