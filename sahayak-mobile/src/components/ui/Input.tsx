// ─────────────────────────────────────────────
// Sahayak Mobile - Input Component
// Native equivalent of shadcn Input
// ─────────────────────────────────────────────

import React, { useState } from "react";
import {
  TextInput,
  View,
  Text,
  TextInputProps,
  TouchableOpacity,
} from "react-native";

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  onRightIconPress?: () => void;
  containerClassName?: string;
  className?: string;
}

export function Input({
  label,
  error,
  hint,
  leftIcon,
  rightIcon,
  onRightIconPress,
  containerClassName = "",
  className = "",
  editable = true,
  ...props
}: InputProps) {
  const [isFocused, setIsFocused] = useState(false);

  const borderColor = error
    ? "border-sahayak-red"
    : isFocused
      ? "border-sahayak-blue"
      : "border-gray-300";

  return (
    <View className={containerClassName}>
      {label && (
        <Text className="text-sm font-medium text-gray-700 mb-1.5">{label}</Text>
      )}
      <View
        className={`
          flex-row items-center
          bg-white rounded-lg border
          ${borderColor}
          ${!editable ? "bg-gray-100" : ""}
        `}
      >
        {leftIcon && <View className="pl-3">{leftIcon}</View>}
        <TextInput
          className={`
            flex-1 h-11 px-3 py-2 text-base text-gray-900
            ${leftIcon ? "pl-2" : ""}
            ${rightIcon ? "pr-2" : ""}
            ${className}
          `}
          placeholderTextColor="#9ca3af"
          editable={editable}
          onFocus={(e) => {
            setIsFocused(true);
            props.onFocus?.(e);
          }}
          onBlur={(e) => {
            setIsFocused(false);
            props.onBlur?.(e);
          }}
          {...props}
        />
        {rightIcon && (
          <TouchableOpacity
            onPress={onRightIconPress}
            disabled={!onRightIconPress}
            className="pr-3"
          >
            {rightIcon}
          </TouchableOpacity>
        )}
      </View>
      {error && <Text className="text-sm text-sahayak-red mt-1">{error}</Text>}
      {hint && !error && (
        <Text className="text-sm text-gray-500 mt-1">{hint}</Text>
      )}
    </View>
  );
}

// Search input variant
interface SearchInputProps extends Omit<InputProps, "leftIcon"> {
  onSearch?: (text: string) => void;
}

export function SearchInput({
  onSearch,
  placeholder = "Search...",
  ...props
}: SearchInputProps) {
  return (
    <Input
      placeholder={placeholder}
      returnKeyType="search"
      onSubmitEditing={(e) => onSearch?.(e.nativeEvent.text)}
      leftIcon={
        <Text className="text-gray-400">🔍</Text>
      }
      {...props}
    />
  );
}

// Textarea variant (multiline input)
interface TextareaProps extends InputProps {
  numberOfLines?: number;
}

export function Textarea({
  numberOfLines = 4,
  className = "",
  ...props
}: TextareaProps) {
  return (
    <Input
      multiline
      numberOfLines={numberOfLines}
      textAlignVertical="top"
      className={`h-auto min-h-[100px] py-3 ${className}`}
      {...props}
    />
  );
}
