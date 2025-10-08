import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';

export default function ProgressScreen() {
  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Your Progress</Text>
        <Text style={styles.headerSubtitle}>Coming soon!</Text>
      </View>

      <View style={styles.content}>
        <Text style={styles.placeholderText}>
          Track your learning progress here.{'\n\n'}
          Future features:{'\n'}
          • Study statistics{'\n'}
          • Streak tracking{'\n'}
          • Spaced repetition{'\n'}
          • Performance analytics
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  header: {
    backgroundColor: '#fff',
    padding: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#6b7280',
  },
  content: {
    padding: 24,
  },
  placeholderText: {
    fontSize: 16,
    color: '#6b7280',
    lineHeight: 24,
  },
});
