// ─────────────────────────────────────────────
// Sahayak Mobile - Button Component
// Native equivalent of shadcn Button
// ─────────────────────────────────────────────

import React from "react";
import {
  TouchableOpacity,
  Text,
  ActivityIndicator,
  View,
  TouchableOpacityProps,
} from "react-native";

type ButtonVariant =
  | "default"
  | "destructive"
  | "outline"
  | "secondary"
  | "ghost"
  | "link";
type ButtonSize = "default" | "sm" | "lg" | "icon";

interface ButtonProps extends TouchableOpacityProps {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  children: React.ReactNode;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

const variantStyles: Record<ButtonVariant, { container: string; text: string }> = {
  default: {
    container: "bg-sahayak-blue",
    text: "text-white",
  },
  destructive: {
    container: "bg-sahayak-red",
    text: "text-white",
  },
  outline: {
    container: "bg-transparent border border-gray-300",
    text: "text-gray-900",
  },
  secondary: {
    container: "bg-gray-100",
    text: "text-gray-900",
  },
  ghost: {
    container: "bg-transparent",
    text: "text-gray-700",
  },
  link: {
    container: "bg-transparent",
    text: "text-sahayak-blue underline",
  },
};

const sizeStyles: Record<ButtonSize, { container: string; text: string }> = {
  default: {
    container: "h-11 px-4 py-2",
    text: "text-base",
  },
  sm: {
    container: "h-9 px-3 py-1.5",
    text: "text-sm",
  },
  lg: {
    container: "h-12 px-6 py-3",
    text: "text-lg",
  },
  icon: {
    container: "h-10 w-10 p-0",
    text: "text-base",
  },
};

export function Button({
  variant = "default",
  size = "default",
  loading = false,
  disabled = false,
  children,
  leftIcon,
  rightIcon,
  className,
  ...props
}: ButtonProps & { className?: string }) {
  const variantStyle = variantStyles[variant];
  const sizeStyle = sizeStyles[size];
  const isDisabled = disabled || loading;

  return (
    <TouchableOpacity
      activeOpacity={0.7}
      disabled={isDisabled}
      className={`
        flex-row items-center justify-center rounded-lg
        ${variantStyle.container}
        ${sizeStyle.container}
        ${isDisabled ? "opacity-50" : ""}
        ${className || ""}
      `}
      {...props}
    >
      {loading ? (
        <ActivityIndicator
          size="small"
          color={variant === "default" || variant === "destructive" ? "#fff" : "#374151"}
        />
      ) : (
        <>
          {leftIcon && <View className="mr-2">{leftIcon}</View>}
          {typeof children === "string" ? (
            <Text
              className={`
                font-semibold text-center
                ${variantStyle.text}
                ${sizeStyle.text}
              `}
            >
              {children}
            </Text>
          ) : (
            children
          )}
          {rightIcon && <View className="ml-2">{rightIcon}</View>}
        </>
      )}
    </TouchableOpacity>
  );
}

// Convenience variants
export function PrimaryButton(props: Omit<ButtonProps, "variant">) {
  return <Button variant="default" {...props} />;
}

export function SecondaryButton(props: Omit<ButtonProps, "variant">) {
  return <Button variant="secondary" {...props} />;
}

export function OutlineButton(props: Omit<ButtonProps, "variant">) {
  return <Button variant="outline" {...props} />;
}

export function DestructiveButton(props: Omit<ButtonProps, "variant">) {
  return <Button variant="destructive" {...props} />;
}

export function IconButton(props: Omit<ButtonProps, "size">) {
  return <Button size="icon" {...props} />;
}
