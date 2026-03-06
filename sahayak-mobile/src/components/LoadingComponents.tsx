/**
 * Loading Components for Slow Networks
 * Skeleton loaders and spinners for poor connectivity scenarios
 */

import React, { useEffect, useRef } from 'react';
import { View, Text, Animated, Easing } from 'react-native';

interface SkeletonProps {
  /** Width of the skeleton (number or percentage string) */
  width?: number | `${number}%`;
  /** Height of the skeleton */
  height?: number;
  /** Border radius */
  borderRadius?: number;
  /** Additional className for NativeWind */
  className?: string;
}

/**
 * Animated skeleton loader for content placeholders
 */
export function Skeleton({
  width = '100%' as const,
  height = 20,
  borderRadius = 4,
  className = '',
}: SkeletonProps) {
  const animatedValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const animation = Animated.loop(
      Animated.sequence([
        Animated.timing(animatedValue, {
          toValue: 1,
          duration: 1000,
          easing: Easing.ease,
          useNativeDriver: true,
        }),
        Animated.timing(animatedValue, {
          toValue: 0,
          duration: 1000,
          easing: Easing.ease,
          useNativeDriver: true,
        }),
      ])
    );
    animation.start();
    return () => animation.stop();
  }, [animatedValue]);

  const opacity = animatedValue.interpolate({
    inputRange: [0, 1],
    outputRange: [0.3, 0.7],
  });

  return (
    <Animated.View
      style={[
        {
          width: width as number | `${number}%`,
          height,
          borderRadius,
          backgroundColor: '#E5E7EB',
        },
        { opacity },
      ]}
      className={className}
    />
  );
}

/**
 * Doctor card skeleton for loading states
 */
export function DoctorCardSkeleton() {
  return (
    <View className="bg-white rounded-xl p-4 mb-3 border border-gray-200">
      <View className="flex-row">
        <Skeleton width={60} height={60} borderRadius={30} />
        <View className="ml-3 flex-1">
          <Skeleton width="70%" height={18} className="mb-2" />
          <Skeleton width="50%" height={14} className="mb-2" />
          <Skeleton width="40%" height={14} />
        </View>
      </View>
      <View className="mt-3 pt-3 border-t border-gray-100">
        <Skeleton width="100%" height={12} className="mb-1" />
        <Skeleton width="60%" height={12} />
      </View>
    </View>
  );
}

/**
 * Medicine card skeleton for loading states
 */
export function MedicineCardSkeleton() {
  return (
    <View className="bg-white rounded-xl p-4 mb-3 border border-gray-200">
      <View className="flex-row justify-between items-start mb-3">
        <View className="flex-1">
          <Skeleton width="80%" height={18} className="mb-2" />
          <Skeleton width="40%" height={14} />
        </View>
        <Skeleton width={60} height={24} borderRadius={12} />
      </View>
      <View className="pt-3 border-t border-gray-100">
        <Skeleton width="100%" height={14} className="mb-2" />
        <Skeleton width="70%" height={14} />
      </View>
    </View>
  );
}

/**
 * Hospital card skeleton for loading states
 */
export function HospitalCardSkeleton() {
  return (
    <View className="bg-white rounded-xl p-4 mb-3 border border-gray-200">
      <View className="flex-row items-start">
        <Skeleton width={48} height={48} borderRadius={8} />
        <View className="ml-3 flex-1">
          <Skeleton width="80%" height={18} className="mb-2" />
          <Skeleton width="60%" height={14} className="mb-2" />
          <Skeleton width="40%" height={14} />
        </View>
      </View>
      <View className="flex-row mt-3 pt-3 border-t border-gray-100">
        <Skeleton width="45%" height={36} borderRadius={8} className="mr-2" />
        <Skeleton width="45%" height={36} borderRadius={8} />
      </View>
    </View>
  );
}

/**
 * Spinner for loading states
 */
interface SpinnerProps {
  size?: 'small' | 'medium' | 'large';
  color?: string;
}

export function Spinner({ size = 'medium', color = '#2563EB' }: SpinnerProps) {
  const spinValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const animation = Animated.loop(
      Animated.timing(spinValue, {
        toValue: 1,
        duration: 1000,
        easing: Easing.linear,
        useNativeDriver: true,
      })
    );
    animation.start();
    return () => animation.stop();
  }, [spinValue]);

  const spin = spinValue.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '360deg'],
  });

  const sizeMap = {
    small: 20,
    medium: 32,
    large: 48,
  };

  const dimension = sizeMap[size];

  return (
    <Animated.View
      style={{
        width: dimension,
        height: dimension,
        borderRadius: dimension / 2,
        borderWidth: dimension / 8,
        borderColor: color,
        borderTopColor: 'transparent',
        transform: [{ rotate: spin }],
      }}
    />
  );
}

/**
 * Full-screen loading overlay
 */
interface LoadingOverlayProps {
  visible: boolean;
  message?: string;
  /** Show estimated time for slow connections */
  showSlowMessage?: boolean;
}

export function LoadingOverlay({
  visible,
  message = 'Loading...',
  showSlowMessage = false,
}: LoadingOverlayProps) {
  if (!visible) return null;

  return (
    <View className="absolute inset-0 bg-black/50 justify-center items-center z-50">
      <View className="bg-white rounded-2xl p-6 mx-8 items-center">
        <Spinner size="large" />
        <Text className="mt-4 text-gray-800 font-medium text-center">
          {message}
        </Text>
        {showSlowMessage && (
          <Text className="mt-2 text-gray-500 text-sm text-center">
            This may take longer on slow connections
          </Text>
        )}
      </View>
    </View>
  );
}

/**
 * Content placeholder with retry for failed loads
 */
interface LoadingPlaceholderProps {
  isLoading: boolean;
  error?: Error | null;
  onRetry?: () => void;
  children: React.ReactNode;
  skeleton?: React.ReactNode;
  emptyMessage?: string;
  isEmpty?: boolean;
}

export function LoadingPlaceholder({
  isLoading,
  error,
  onRetry,
  children,
  skeleton,
  emptyMessage = 'No data found',
  isEmpty = false,
}: LoadingPlaceholderProps) {
  if (isLoading) {
    return <>{skeleton || <Spinner />}</>;
  }

  if (error) {
    return (
      <View className="p-6 items-center">
        <Text className="text-red-600 text-center mb-4">
          {error.message || 'Something went wrong'}
        </Text>
        {onRetry && (
          <View className="bg-primary-600 rounded-lg px-6 py-2">
            <Text className="text-white font-medium" onPress={onRetry}>
              Retry
            </Text>
          </View>
        )}
      </View>
    );
  }

  if (isEmpty) {
    return (
      <View className="p-6 items-center">
        <Text className="text-gray-500 text-center">{emptyMessage}</Text>
      </View>
    );
  }

  return <>{children}</>;
}

export default Skeleton;
