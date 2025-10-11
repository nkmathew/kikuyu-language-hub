import React from 'react';
import { View, Text, StyleSheet, ActivityIndicator, useColorScheme } from 'react-native';

interface DataLoadingSpinnerProps {
  message?: string;
  progress?: number; // 0-100
  showProgress?: boolean;
  color?: string;
}

export default function DataLoadingSpinner({ 
  message = 'Loading data...',
  progress,
  showProgress = false,
  color = '#3b82f6'
}: DataLoadingSpinnerProps) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  return (
    <View style={[styles.container, isDark && styles.darkContainer]}>
      <ActivityIndicator size="large" color={color} />
      <Text style={[styles.message, isDark && styles.darkMessage]}>{message}</Text>
      {showProgress && progress !== undefined && (
        <View style={[styles.progressContainer, isDark && styles.darkProgressContainer]}>
          <View style={[styles.progressBar, isDark && styles.darkProgressBar]}>
            <View 
              style={[
                styles.progressFill, 
                { width: `${Math.min(100, Math.max(0, progress))}%` }
              ]} 
            />
          </View>
          <Text style={[styles.progressText, isDark && styles.darkProgressText]}>
            {Math.round(progress)}%
          </Text>
        </View>
      )}
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
    fontWeight: '500',
  },
  darkMessage: {
    color: '#9ca3af',
  },
  progressContainer: {
    marginTop: 20,
    width: '80%',
    alignItems: 'center',
  },
  darkProgressContainer: {
    // Same as light
  },
  progressBar: {
    width: '100%',
    height: 4,
    backgroundColor: '#e5e7eb',
    borderRadius: 2,
    overflow: 'hidden',
  },
  darkProgressBar: {
    backgroundColor: '#374151',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#3b82f6',
    borderRadius: 2,
  },
  progressText: {
    marginTop: 8,
    fontSize: 14,
    color: '#6b7280',
    fontWeight: '600',
  },
  darkProgressText: {
    color: '#9ca3af',
  },
});
