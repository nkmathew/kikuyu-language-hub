import React from 'react';
import { View, Text, StyleSheet, ActivityIndicator, useColorScheme } from 'react-native';
import LoadingSpinner from './LoadingSpinner';
import AdvancedSpinner from './AdvancedSpinner';

interface LoadingStatesProps {
  type?: 'basic' | 'advanced' | 'minimal';
  message?: string;
  spinnerType?: 'dots' | 'pulse' | 'wave';
  size?: 'small' | 'large';
  color?: string;
}

export default function LoadingStates({ 
  type = 'basic',
  message = 'Loading...',
  spinnerType = 'dots',
  size = 'large',
  color = '#3b82f6'
}: LoadingStatesProps) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  switch (type) {
    case 'advanced':
      return <AdvancedSpinner message={message} type={spinnerType} color={color} />;
    case 'minimal':
      return (
        <View style={[styles.minimalContainer, isDark && styles.darkContainer]}>
          <ActivityIndicator size={size} color={color} />
        </View>
      );
    case 'basic':
    default:
      return <LoadingSpinner message={message} size={size} color={color} />;
  }
}

const styles = StyleSheet.create({
  minimalContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    padding: 20,
  },
  darkContainer: {
    backgroundColor: '#111827',
  },
});
