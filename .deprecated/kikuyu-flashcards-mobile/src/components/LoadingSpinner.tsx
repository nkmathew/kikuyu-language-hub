import React from 'react';
import { View, Text, StyleSheet, ActivityIndicator, useColorScheme } from 'react-native';

interface LoadingSpinnerProps {
  message?: string;
  size?: 'small' | 'large';
  color?: string;
}

export default function LoadingSpinner({ 
  message = 'Loading...', 
  size = 'large', 
  color = '#3b82f6' 
}: LoadingSpinnerProps) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  return (
    <View style={[styles.container, isDark && styles.darkContainer]}>
      <ActivityIndicator size={size} color={color} />
      <Text style={[styles.message, isDark && styles.darkMessage]}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    padding: 20,
  },
  darkContainer: {
    backgroundColor: '#111827',
  },
  message: {
    marginTop: 16,
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  darkMessage: {
    color: '#9ca3af',
  },
});
