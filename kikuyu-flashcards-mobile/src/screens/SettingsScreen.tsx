import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
  Alert,
  useColorScheme,
} from 'react-native';
import { storageService } from '../lib/storage';

export default function SettingsScreen() {
  const [preferences, setPreferences] = useState({
    defaultDifficulty: ['beginner'],
    autoPlayAudio: false,
    showCulturalNotes: true,
    cardAutoAdvance: false,
    theme: 'dark' as 'light' | 'dark',
  });
  const [loading, setLoading] = useState(true);
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    loadPreferences();
  }, []);

  const loadPreferences = async () => {
    try {
      const prefs = await storageService.getPreferences();
      setPreferences(prefs);
    } catch (error) {
      console.error('Error loading preferences:', error);
    } finally {
      setLoading(false);
    }
  };

  const updatePreference = async (key: keyof typeof preferences, value: any) => {
    try {
      await storageService.updatePreference(key, value);
      setPreferences(prev => ({ ...prev, [key]: value }));
    } catch (error) {
      console.error('Error updating preference:', error);
      Alert.alert('Error', 'Failed to save preference');
    }
  };

  const clearAllData = () => {
    Alert.alert(
      'Clear All Data',
      'This will delete all your progress, sessions, and statistics. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            await storageService.clearAllData();
            Alert.alert('Success', 'All data has been cleared');
          },
        },
      ]
    );
  };

  const exportData = async () => {
    try {
      const data = await storageService.exportAllData();
      Alert.alert('Export Successful', `Data exported (${data.length} characters). In a production app, this would save to a file.`);
    } catch (error) {
      Alert.alert('Export Failed', 'Could not export data. Please try again.');
    }
  };

  if (loading) {
    return (
      <View style={[styles.container, isDark && styles.darkBg]}>
        <Text style={[styles.loadingText, isDark && styles.darkText]}>Loading settings...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={[styles.container, isDark && styles.darkBg]}>
      <View style={[styles.header, isDark && styles.darkHeader]}>
        <Text style={[styles.headerTitle, isDark && styles.darkText]}>Settings</Text>
        <Text style={[styles.headerSubtitle, isDark && styles.darkTextSecondary]}>
          Customize your learning experience
        </Text>
      </View>

      {/* Study Preferences */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, isDark && styles.darkText]}>Study Preferences</Text>
        
        <View style={[styles.settingItem, isDark && styles.darkCard]}>
          <View style={styles.settingContent}>
            <Text style={[styles.settingLabel, isDark && styles.darkText]}>Show Cultural Notes</Text>
            <Text style={[styles.settingDescription, isDark && styles.darkTextSecondary]}>
              Display cultural context and notes
            </Text>
          </View>
          <Switch
            value={preferences.showCulturalNotes}
            onValueChange={(value) => updatePreference('showCulturalNotes', value)}
            trackColor={{ false: '#d1d5db', true: '#3b82f6' }}
            thumbColor={preferences.showCulturalNotes ? '#ffffff' : '#f3f4f6'}
          />
        </View>

        <View style={[styles.settingItem, isDark && styles.darkCard]}>
          <View style={styles.settingContent}>
            <Text style={[styles.settingLabel, isDark && styles.darkText]}>Auto Advance Cards</Text>
            <Text style={[styles.settingDescription, isDark && styles.darkTextSecondary]}>
              Automatically move to next card after rating
            </Text>
          </View>
          <Switch
            value={preferences.cardAutoAdvance}
            onValueChange={(value) => updatePreference('cardAutoAdvance', value)}
            trackColor={{ false: '#d1d5db', true: '#3b82f6' }}
            thumbColor={preferences.cardAutoAdvance ? '#ffffff' : '#f3f4f6'}
          />
        </View>
      </View>

      {/* Data Management */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, isDark && styles.darkText]}>Data Management</Text>
        
        <TouchableOpacity
          style={[styles.actionButton, styles.exportButton, isDark && styles.darkActionButton]}
          onPress={exportData}
        >
          <Text style={styles.actionButtonText}>📤 Export All Data</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.actionButton, styles.clearButton, isDark && styles.darkClearButton]}
          onPress={clearAllData}
        >
          <Text style={[styles.actionButtonText, styles.clearButtonText]}>🗑️ Clear All Data</Text>
        </TouchableOpacity>
      </View>

      {/* App Info */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, isDark && styles.darkText]}>About</Text>
        
        <View style={[styles.infoCard, isDark && styles.darkCard]}>
          <Text style={[styles.infoText, isDark && styles.darkTextSecondary]}>
            Kikuyu Flashcards Mobile v1.0.0
          </Text>
          <Text style={[styles.infoText, isDark && styles.darkTextSecondary]}>
            Learn Kikuyu language with spaced repetition
          </Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  loadingText: {
    textAlign: 'center',
    marginTop: 50,
    fontSize: 16,
    color: '#6b7280',
  },
  header: {
    backgroundColor: '#fff',
    padding: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  darkHeader: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
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
  section: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 16,
  },
  settingItem: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  darkCard: {
    backgroundColor: '#1f2937',
  },
  settingContent: {
    flex: 1,
    marginRight: 16,
  },
  settingLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  settingDescription: {
    fontSize: 14,
    color: '#6b7280',
  },
  actionButton: {
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginBottom: 12,
  },
  exportButton: {
    backgroundColor: '#10b981',
  },
  clearButton: {
    backgroundColor: '#fee2e2',
    borderWidth: 2,
    borderColor: '#ef4444',
  },
  darkActionButton: {
    backgroundColor: '#059669',
  },
  darkClearButton: {
    backgroundColor: '#7f1d1d',
    borderColor: '#dc2626',
  },
  actionButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  clearButtonText: {
    color: '#ef4444',
  },
  infoCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  infoText: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 4,
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
});
