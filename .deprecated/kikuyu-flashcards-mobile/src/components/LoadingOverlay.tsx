import React from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Modal, useColorScheme } from 'react-native';

interface LoadingOverlayProps {
  visible: boolean;
  message?: string;
  transparent?: boolean;
  size?: 'small' | 'large';
  color?: string;
}

export default function LoadingOverlay({ 
  visible,
  message = 'Loading...',
  transparent = true,
  size = 'large',
  color = '#3b82f6'
}: LoadingOverlayProps) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  if (!visible) return null;

  return (
    <Modal
      transparent={transparent}
      visible={visible}
      animationType="fade"
    >
      <View style={[styles.overlay, isDark && styles.darkOverlay]}>
        <View style={[styles.container, isDark && styles.darkContainer]}>
          <ActivityIndicator size={size} color={color} />
          {message && (
            <Text style={[styles.message, isDark && styles.darkMessage]}>
              {message}
            </Text>
          )}
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  darkOverlay: {
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  container: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
    minWidth: 120,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  darkContainer: {
    backgroundColor: '#1f2937',
  },
  message: {
    marginTop: 12,
    fontSize: 16,
    color: '#374151',
    textAlign: 'center',
    fontWeight: '500',
  },
  darkMessage: {
    color: '#d1d5db',
  },
});
