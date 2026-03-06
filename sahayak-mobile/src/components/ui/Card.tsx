// ─────────────────────────────────────────────
// Sahayak Mobile - Card Component
// Native equivalent of shadcn Card
// ─────────────────────────────────────────────

import React from "react";
import { View, Text, ViewProps, TextProps, Pressable, PressableProps } from "react-native";

interface CardProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function Card({ children, className = "", ...props }: CardProps) {
  return (
    <View
      className={`
        bg-white rounded-xl border border-gray-200 shadow-sm
        ${className}
      `}
      {...props}
    >
      {children}
    </View>
  );
}

interface CardHeaderProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function CardHeader({ children, className = "", ...props }: CardHeaderProps) {
  return (
    <View
      className={`
        px-4 pt-4 pb-2
        ${className}
      `}
      {...props}
    >
      {children}
    </View>
  );
}

interface CardTitleProps extends TextProps {
  children: React.ReactNode;
  className?: string;
}

export function CardTitle({ children, className = "", ...props }: CardTitleProps) {
  return (
    <Text
      className={`
        text-lg font-semibold text-gray-900
        ${className}
      `}
      {...props}
    >
      {children}
    </Text>
  );
}

interface CardDescriptionProps extends TextProps {
  children: React.ReactNode;
  className?: string;
}

export function CardDescription({
  children,
  className = "",
  ...props
}: CardDescriptionProps) {
  return (
    <Text
      className={`
        text-sm text-gray-500 mt-1
        ${className}
      `}
      {...props}
    >
      {children}
    </Text>
  );
}

interface CardContentProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function CardContent({ children, className = "", ...props }: CardContentProps) {
  return (
    <View
      className={`
        px-4 py-3
        ${className}
      `}
      {...props}
    >
      {children}
    </View>
  );
}

interface CardFooterProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function CardFooter({ children, className = "", ...props }: CardFooterProps) {
  return (
    <View
      className={`
        px-4 pb-4 pt-2 flex-row items-center
        ${className}
      `}
      {...props}
    >
      {children}
    </View>
  );
}

// Pressable card variant for clickable cards

interface PressableCardProps extends PressableProps {
  children: React.ReactNode;
  className?: string;
}

export function PressableCard({
  children,
  className = "",
  ...props
}: PressableCardProps) {
  return (
    <Pressable
      className={`
        bg-white rounded-xl border border-gray-200 shadow-sm active:opacity-70
        ${className}
      `}
      {...props}
    >
      {children}
    </Pressable>
  );
}
