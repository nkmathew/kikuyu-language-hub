import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
  TouchableOpacity,
  RefreshControl,
  Alert,
  useColorScheme,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { storageService, AppStats } from '../lib/storage';
import { spacedRepetitionService } from '../lib/spacedRepetition';
import { StudySession } from '../types/flashcard';

export default function ProgressScreen() {
  const [stats, setStats] = useState<AppStats | null>(null);
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [studyStats, setStudyStats] = useState<ReturnType<typeof spacedRepetitionService.getStudyStats> | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const colorScheme = useColorScheme();
  const isDark = true; // Force dark mode as default

  useFocusEffect(
    useCallback(() => {
      loadProgress();
    }, [])
  );

  const loadProgress = async () => {
    try {
      const [statsData, sessionsData, allProgress] = await Promise.all([
        storageService.getStats(),
        storageService.getSessions(10),
        storageService.getAllProgress(),
      ]);

      setStats(statsData);
      setSessions(sessionsData);
      setStudyStats(spacedRepetitionService.getStudyStats(allProgress));
    } catch (error) {
      console.error('Error loading progress:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const onRefresh = () => {
    setRefreshing(true);
    loadProgress();
  };

  const handleExportData = async () => {
    try {
      const data = await storageService.exportAllData();
      Alert.alert('Export Successful', `Data exported (${data.length} characters). In a production app, this would save to a file or share via social media.`);
    } catch (error) {
      Alert.alert('Export Failed', 'Could not export data. Please try again.');
    }
  };

  const handleClearData = () => {
    Alert.alert(
      'Clear All Data?',
      'This will delete all your progress, sessions, and statistics. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            await storageService.clearAllData();
            loadProgress();
            Alert.alert('Data Cleared', 'All progress has been reset.');
          },
        },
      ]
    );
  };

  if (loading) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    );
  }

  const todayStudyTime = Math.round(stats?.totalTimeSpent || 0);
  const averageAccuracy = sessions.length > 0
    ? Math.round(sessions.reduce((sum, s) => sum + (s.correctAnswers / s.cardsStudied) * 100, 0) / sessions.length)
    : 0;

  return (
    <ScrollView
      style={[styles.container, isDark && styles.darkBg]}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3b82f6']} />
      }
    >
      {/* Header Stats */}
      <View style={[styles.header, isDark && styles.darkHeader]}>
        <Text style={[styles.headerTitle, isDark && styles.darkText]}>Your Progress</Text>
        <Text style={[styles.headerSubtitle, isDark && styles.darkTextSecondary]}>Keep up the great work!</Text>
      </View>

      {/* Main Stats Grid */}
      <View style={styles.statsGrid}>
        <View style={[styles.statCard, styles.statCardPrimary, isDark && styles.darkCardPrimary]}>
          <Text style={styles.statNumber}>{stats?.streakCount || 0}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>🔥 Day Streak</Text>
        </View>

        <View style={[styles.statCard, isDark && styles.darkCard]}>
          <Text style={[styles.statNumber, isDark && styles.darkTextPrimary]}>{stats?.totalCardsStudied || 0}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>Cards Studied</Text>
        </View>

        <View style={[styles.statCard, isDark && styles.darkCard]}>
          <Text style={[styles.statNumber, isDark && styles.darkTextPrimary]}>{stats?.totalSessionsCompleted || 0}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>Sessions</Text>
        </View>

        <View style={[styles.statCard, isDark && styles.darkCard]}>
          <Text style={[styles.statNumber, isDark && styles.darkTextPrimary]}>{todayStudyTime}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>Minutes</Text>
        </View>
      </View>

      {/* Spaced Repetition Stats */}
      {studyStats && studyStats.totalCards > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Learning Status</Text>
          <View style={styles.card}>
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Due Today</Text>
              <Text style={[styles.progressValue, styles.dueValue]}>{studyStats.dueToday}</Text>
            </View>
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Due Soon (3 days)</Text>
              <Text style={styles.progressValue}>{studyStats.dueSoon}</Text>
            </View>
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Learning</Text>
              <Text style={styles.progressValue}>{studyStats.learning}</Text>
            </View>
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Mastered</Text>
              <Text style={[styles.progressValue, styles.masteredValue]}>{studyStats.mastered}</Text>
            </View>
            <View style={styles.divider} />
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Total Cards</Text>
              <Text style={[styles.progressValue, styles.totalValue]}>{studyStats.totalCards}</Text>
            </View>
            <View style={styles.progressRow}>
              <Text style={styles.progressLabel}>Average Ease Factor</Text>
              <Text style={styles.progressValue}>{studyStats.averageEaseFactor}</Text>
            </View>
          </View>
        </View>
      )}

      {/* Recent Sessions */}
      {sessions.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Recent Sessions</Text>
          {sessions.reverse().map((session, index) => {
            const accuracy = Math.round((session.correctAnswers / session.cardsStudied) * 100);
            const date = new Date(session.startTime);
            const timeAgo = getTimeAgo(date);

            return (
              <View key={index} style={styles.sessionCard}>
                <View style={styles.sessionHeader}>
                  <Text style={styles.sessionCategory}>{session.category}</Text>
                  <Text style={styles.sessionTime}>{timeAgo}</Text>
                </View>
                <View style={styles.sessionStats}>
                  <Text style={styles.sessionText}>
                    {session.cardsStudied} cards • {accuracy}% accuracy
                  </Text>
                  <Text style={styles.sessionDifficulty}>
                    {session.difficulty.join(', ')}
                  </Text>
                </View>
                <View style={styles.sessionProgressBar}>
                  <View style={[styles.sessionProgressFill, { width: `${accuracy}%` }]} />
                </View>
              </View>
            );
          })}
        </View>
      )}

      {/* Overall Performance */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Overall Performance</Text>
        <View style={styles.card}>
          <View style={styles.performanceRow}>
            <Text style={styles.performanceLabel}>Average Accuracy</Text>
            <Text style={[styles.performanceValue, getAccuracyStyle(averageAccuracy)]}>
              {averageAccuracy}%
            </Text>
          </View>
          <View style={styles.performanceRow}>
            <Text style={styles.performanceLabel}>Total Study Time</Text>
            <Text style={styles.performanceValue}>
              {Math.floor(todayStudyTime / 60)}h {todayStudyTime % 60}m
            </Text>
          </View>
          {stats?.lastStudyDate && (
            <View style={styles.performanceRow}>
              <Text style={styles.performanceLabel}>Last Studied</Text>
              <Text style={styles.performanceValue}>
                {getTimeAgo(new Date(stats.lastStudyDate))}
              </Text>
            </View>
          )}
        </View>
      </View>

      {/* Actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Data Management</Text>
        <TouchableOpacity style={styles.actionButton} onPress={handleExportData}>
          <Text style={styles.actionButtonText}>📤 Export Progress Data</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.actionButton, styles.actionButtonDanger]}
          onPress={handleClearData}
        >
          <Text style={[styles.actionButtonText, styles.actionButtonDangerText]}>
            🗑️ Clear All Data
          </Text>
        </TouchableOpacity>
      </View>

      <View style={styles.footer}>
        <Text style={styles.footerText}>Pull down to refresh</Text>
      </View>
    </ScrollView>
  );
}

function getTimeAgo(date: Date): string {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days}d ago`;
  return date.toLocaleDateString();
}

function getAccuracyStyle(accuracy: number) {
  if (accuracy >= 80) return { color: '#10b981' };
  if (accuracy >= 60) return { color: '#eab308' };
  return { color: '#ef4444' };
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 12,
    gap: 12,
  },
  statCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statCardPrimary: {
    backgroundColor: '#eff6ff',
    borderWidth: 2,
    borderColor: '#2563eb',
  },
  statNumber: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#2563eb',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 14,
    color: '#6b7280',
    textAlign: 'center',
  },
  section: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 12,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  progressRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  progressLabel: {
    fontSize: 16,
    color: '#6b7280',
  },
  progressValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  dueValue: {
    color: '#ef4444',
  },
  masteredValue: {
    color: '#10b981',
  },
  totalValue: {
    color: '#2563eb',
  },
  divider: {
    height: 1,
    backgroundColor: '#e5e7eb',
    marginVertical: 8,
  },
  sessionCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sessionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  sessionCategory: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
    textTransform: 'capitalize',
  },
  sessionTime: {
    fontSize: 14,
    color: '#9ca3af',
  },
  sessionStats: {
    marginBottom: 8,
  },
  sessionText: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 4,
  },
  sessionDifficulty: {
    fontSize: 12,
    color: '#9ca3af',
    textTransform: 'capitalize',
  },
  sessionProgressBar: {
    height: 4,
    backgroundColor: '#e5e7eb',
    borderRadius: 2,
    overflow: 'hidden',
  },
  sessionProgressFill: {
    height: '100%',
    backgroundColor: '#10b981',
  },
  performanceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
  },
  performanceLabel: {
    fontSize: 16,
    color: '#6b7280',
  },
  performanceValue: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  actionButton: {
    backgroundColor: '#2563eb',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginBottom: 12,
  },
  actionButtonDanger: {
    backgroundColor: '#fee2e2',
    borderWidth: 2,
    borderColor: '#ef4444',
  },
  actionButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  actionButtonDangerText: {
    color: '#ef4444',
  },
  footer: {
    padding: 24,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 14,
    color: '#9ca3af',
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  darkHeader: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
  },
  darkCard: {
    backgroundColor: '#1f2937',
  },
  darkCardPrimary: {
    backgroundColor: '#1e3a8a',
    borderColor: '#3b82f6',
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextPrimary: {
    color: '#3b82f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
});
