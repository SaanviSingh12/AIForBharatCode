// ─────────────────────────────────────────────
// Sahayak Mobile - BottomSheet Component
// Uses @gorhom/bottom-sheet for native modals
// ─────────────────────────────────────────────

import React, { useCallback, useMemo, forwardRef, useImperativeHandle } from "react";
import { View, Text, StyleSheet, TouchableOpacity, Dimensions } from "react-native";
import BottomSheetLib, {
  BottomSheetView,
  BottomSheetModal,
  BottomSheetModalProvider,
  BottomSheetBackdrop,
  BottomSheetScrollView,
} from "@gorhom/bottom-sheet";
import { GestureHandlerRootView } from "react-native-gesture-handler";

// Re-export provider for app setup
export { BottomSheetModalProvider, GestureHandlerRootView };

const { height: SCREEN_HEIGHT } = Dimensions.get("window");

// ─────────────────────────────────────────────
// Basic BottomSheet (persistent, always visible)
// ─────────────────────────────────────────────

interface BottomSheetProps {
  children: React.ReactNode;
  snapPoints?: (string | number)[];
  initialIndex?: number;
  onChange?: (index: number) => void;
  enablePanDownToClose?: boolean;
  className?: string;
}

export function BottomSheet({
  children,
  snapPoints: customSnapPoints,
  initialIndex = 0,
  onChange,
  enablePanDownToClose = false,
  className = "",
}: BottomSheetProps) {
  const snapPoints = useMemo(
    () => customSnapPoints || ["25%", "50%", "90%"],
    [customSnapPoints]
  );

  return (
    <BottomSheetLib
      index={initialIndex}
      snapPoints={snapPoints}
      onChange={onChange}
      enablePanDownToClose={enablePanDownToClose}
      backgroundStyle={styles.sheetBackground}
      handleIndicatorStyle={styles.handleIndicator}
    >
      <BottomSheetView style={styles.contentContainer}>
        {children}
      </BottomSheetView>
    </BottomSheetLib>
  );
}

// ─────────────────────────────────────────────
// Modal BottomSheet (dismissible overlay)
// ─────────────────────────────────────────────

export interface BottomSheetModalRef {
  open: () => void;
  close: () => void;
}

interface BottomSheetModalProps {
  children: React.ReactNode;
  snapPoints?: (string | number)[];
  title?: string;
  onDismiss?: () => void;
  showCloseButton?: boolean;
}

export const AppBottomSheetModal = forwardRef<BottomSheetModalRef, BottomSheetModalProps>(
  function AppBottomSheetModal(
    {
      children,
      snapPoints: customSnapPoints,
      title,
      onDismiss,
      showCloseButton = true,
    },
    ref
  ) {
    const bottomSheetModalRef = React.useRef<BottomSheetModal>(null);

    const snapPoints = useMemo(
      () => customSnapPoints || ["50%", "75%"],
      [customSnapPoints]
    );

    useImperativeHandle(ref, () => ({
      open: () => bottomSheetModalRef.current?.present(),
      close: () => bottomSheetModalRef.current?.dismiss(),
    }));

    const renderBackdrop = useCallback(
      (props: any) => (
        <BottomSheetBackdrop
          {...props}
          appearsOnIndex={0}
          disappearsOnIndex={-1}
          opacity={0.5}
        />
      ),
      []
    );

    return (
      <BottomSheetModal
        ref={bottomSheetModalRef}
        snapPoints={snapPoints}
        onDismiss={onDismiss}
        backdropComponent={renderBackdrop}
        backgroundStyle={styles.sheetBackground}
        handleIndicatorStyle={styles.handleIndicator}
      >
        <BottomSheetView style={styles.contentContainer}>
          {(title || showCloseButton) && (
            <View style={styles.header}>
              {title && <Text style={styles.title}>{title}</Text>}
              {showCloseButton && (
                <TouchableOpacity
                  onPress={() => bottomSheetModalRef.current?.dismiss()}
                  style={styles.closeButton}
                >
                  <Text style={styles.closeButtonText}>✕</Text>
                </TouchableOpacity>
              )}
            </View>
          )}
          {children}
        </BottomSheetView>
      </BottomSheetModal>
    );
  }
);

// ─────────────────────────────────────────────
// Scrollable BottomSheet Content
// ─────────────────────────────────────────────

interface ScrollableBottomSheetProps {
  children: React.ReactNode;
  className?: string;
}

export function ScrollableBottomSheetContent({
  children,
  className = "",
}: ScrollableBottomSheetProps) {
  return (
    <BottomSheetScrollView
      contentContainerStyle={styles.scrollContent}
    >
      {children}
    </BottomSheetScrollView>
  );
}

// ─────────────────────────────────────────────
// Pre-composed Modals for Common Use Cases
// ─────────────────────────────────────────────

interface ConfirmModalProps {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel?: () => void;
  isDestructive?: boolean;
}

export const ConfirmModal = forwardRef<BottomSheetModalRef, ConfirmModalProps>(
  function ConfirmModal(
    {
      title,
      message,
      confirmText = "Confirm",
      cancelText = "Cancel",
      onConfirm,
      onCancel,
      isDestructive = false,
    },
    ref
  ) {
    const modalRef = React.useRef<BottomSheetModalRef>(null);

    useImperativeHandle(ref, () => ({
      open: () => modalRef.current?.open(),
      close: () => modalRef.current?.close(),
    }));

    const handleConfirm = () => {
      onConfirm();
      modalRef.current?.close();
    };

    const handleCancel = () => {
      onCancel?.();
      modalRef.current?.close();
    };

    return (
      <AppBottomSheetModal
        ref={modalRef}
        title={title}
        snapPoints={["35%"]}
        showCloseButton={false}
      >
        <View style={styles.confirmContent}>
          <Text style={styles.confirmMessage}>{message}</Text>
          <View style={styles.confirmButtons}>
            <TouchableOpacity
              onPress={handleCancel}
              style={[styles.confirmButton, styles.cancelButton]}
            >
              <Text style={styles.cancelButtonText}>{cancelText}</Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={handleConfirm}
              style={[
                styles.confirmButton,
                isDestructive ? styles.destructiveButton : styles.primaryButton,
              ]}
            >
              <Text style={styles.primaryButtonText}>{confirmText}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </AppBottomSheetModal>
    );
  }
);

// Filter/Sort Modal for lists
interface FilterOption {
  id: string;
  label: string;
  icon?: string;
}

interface FilterModalProps {
  title: string;
  options: FilterOption[];
  selectedId?: string;
  onSelect: (id: string) => void;
}

export const FilterModal = forwardRef<BottomSheetModalRef, FilterModalProps>(
  function FilterModal({ title, options, selectedId, onSelect }, ref) {
    const modalRef = React.useRef<BottomSheetModalRef>(null);

    useImperativeHandle(ref, () => ({
      open: () => modalRef.current?.open(),
      close: () => modalRef.current?.close(),
    }));

    const handleSelect = (id: string) => {
      onSelect(id);
      modalRef.current?.close();
    };

    return (
      <AppBottomSheetModal ref={modalRef} title={title} snapPoints={["50%"]}>
        <View style={styles.filterContent}>
          {options.map((option) => (
            <TouchableOpacity
              key={option.id}
              onPress={() => handleSelect(option.id)}
              style={[
                styles.filterOption,
                selectedId === option.id && styles.filterOptionSelected,
              ]}
            >
              {option.icon && (
                <Text style={styles.filterOptionIcon}>{option.icon}</Text>
              )}
              <Text
                style={[
                  styles.filterOptionText,
                  selectedId === option.id && styles.filterOptionTextSelected,
                ]}
              >
                {option.label}
              </Text>
              {selectedId === option.id && (
                <Text style={styles.checkmark}>✓</Text>
              )}
            </TouchableOpacity>
          ))}
        </View>
      </AppBottomSheetModal>
    );
  }
);

// ─────────────────────────────────────────────
// Styles
// ─────────────────────────────────────────────

const styles = StyleSheet.create({
  sheetBackground: {
    backgroundColor: "#ffffff",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 10,
  },
  handleIndicator: {
    backgroundColor: "#d1d5db",
    width: 40,
    height: 4,
  },
  contentContainer: {
    flex: 1,
    paddingHorizontal: 16,
  },
  scrollContent: {
    paddingBottom: 24,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: "#e5e7eb",
    marginBottom: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    color: "#111827",
  },
  closeButton: {
    padding: 8,
  },
  closeButtonText: {
    fontSize: 18,
    color: "#6b7280",
  },
  confirmContent: {
    paddingVertical: 8,
  },
  confirmMessage: {
    fontSize: 16,
    color: "#4b5563",
    marginBottom: 24,
    lineHeight: 24,
  },
  confirmButtons: {
    flexDirection: "row",
    gap: 12,
  },
  confirmButton: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: "center",
  },
  cancelButton: {
    backgroundColor: "#f3f4f6",
  },
  primaryButton: {
    backgroundColor: "#2563eb",
  },
  destructiveButton: {
    backgroundColor: "#dc2626",
  },
  cancelButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#374151",
  },
  primaryButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#ffffff",
  },
  filterContent: {
    paddingVertical: 8,
  },
  filterOption: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 14,
    paddingHorizontal: 4,
    borderBottomWidth: 1,
    borderBottomColor: "#f3f4f6",
  },
  filterOptionSelected: {
    backgroundColor: "#eff6ff",
    marginHorizontal: -4,
    paddingHorizontal: 8,
    borderRadius: 8,
    borderBottomWidth: 0,
  },
  filterOptionIcon: {
    fontSize: 18,
    marginRight: 12,
  },
  filterOptionText: {
    flex: 1,
    fontSize: 16,
    color: "#374151",
  },
  filterOptionTextSelected: {
    color: "#2563eb",
    fontWeight: "600",
  },
  checkmark: {
    fontSize: 18,
    color: "#2563eb",
    fontWeight: "bold",
  },
});
