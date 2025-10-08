import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated, useColorScheme } from 'react-native';

interface AdvancedSpinnerProps {
  message?: string;
  type?: 'dots' | 'pulse' | 'wave';
  color?: string;
}

export default function AdvancedSpinner({ 
  message = 'Loading...', 
  type = 'dots',
  color = '#3b82f6' 
}: AdvancedSpinnerProps) {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;
  const animationValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const animation = Animated.loop(
      Animated.sequence([
        Animated.timing(animationValue, {
          toValue: 1,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(animationValue, {
          toValue: 0,
          duration: 1000,
          useNativeDriver: true,
        }),
      ])
    );
    animation.start();
    return () => animation.stop();
  }, [animationValue]);

  const renderDotsSpinner = () => (
    <View style={styles.dotsContainer}>
      {[0, 1, 2].map((index) => (
        <Animated.View
          key={index}
          style={[
            styles.dot,
            { backgroundColor: color },
            {
              opacity: animationValue.interpolate({
                inputRange: [0, 0.5, 1],
                outputRange: [0.3, 1, 0.3],
                extrapolate: 'clamp',
              }),
              transform: [
                {
                  scale: animationValue.interpolate({
                    inputRange: [0, 0.5, 1],
                    outputRange: [0.8, 1.2, 0.8],
                    extrapolate: 'clamp',
                  }),
                },
              ],
            },
          ]}
        />
      ))}
    </View>
  );

  const renderPulseSpinner = () => (
    <Animated.View
      style={[
        styles.pulseContainer,
        {
          opacity: animationValue.interpolate({
            inputRange: [0, 0.5, 1],
            outputRange: [0.3, 1, 0.3],
            extrapolate: 'clamp',
          }),
          transform: [
            {
              scale: animationValue.interpolate({
                inputRange: [0, 0.5, 1],
                outputRange: [0.8, 1.1, 0.8],
                extrapolate: 'clamp',
              }),
            },
          ],
        },
      ]}
    >
      <View style={[styles.pulseCircle, { backgroundColor: color }]} />
    </Animated.View>
  );

  const renderWaveSpinner = () => (
    <View style={styles.waveContainer}>
      {[0, 1, 2, 3, 4].map((index) => (
        <Animated.View
          key={index}
          style={[
            styles.waveBar,
            { backgroundColor: color },
            {
              height: animationValue.interpolate({
                inputRange: [0, 0.2, 0.4, 0.6, 0.8, 1],
                outputRange: [20, 40, 20, 40, 20, 20],
                extrapolate: 'clamp',
              }),
            },
          ]}
        />
      ))}
    </View>
  );

  const renderSpinner = () => {
    switch (type) {
      case 'dots':
        return renderDotsSpinner();
      case 'pulse':
        return renderPulseSpinner();
      case 'wave':
        return renderWaveSpinner();
      default:
        return renderDotsSpinner();
    }
  };

  return (
    <View style={[styles.container, isDark && styles.darkContainer]}>
      {renderSpinner()}
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
    marginTop: 20,
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
    fontWeight: '500',
  },
  darkMessage: {
    color: '#9ca3af',
  },
  // Dots spinner styles
  dotsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  dot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  // Pulse spinner styles
  pulseContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  pulseCircle: {
    width: 40,
    height: 40,
    borderRadius: 20,
  },
  // Wave spinner styles
  waveContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  waveBar: {
    width: 6,
    borderRadius: 3,
  },
});
