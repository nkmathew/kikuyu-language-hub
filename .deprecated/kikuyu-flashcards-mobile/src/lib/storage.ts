import AsyncStorage from '@react-native-async-storage/async-storage';
import { StudyProgress, StudySession, UserPreferences } from '../types/flashcard';

const STORAGE_KEYS = {
  PROGRESS: '@kikuyu_flashcards:progress',
  SESSIONS: '@kikuyu_flashcards:sessions',
  PREFERENCES: '@kikuyu_flashcards:preferences',
  STREAK: '@kikuyu_flashcards:streak',
  STATS: '@kikuyu_flashcards:stats',
};

export interface AppStats {
  totalCardsStudied: number;
  totalSessionsCompleted: number;
  totalTimeSpent: number; // in minutes
  lastStudyDate: string;
  streakCount: number;
  streakStartDate: string;
}

class StorageService {
  // ========== Progress Tracking ==========

  async getCardProgress(cardId: string): Promise<StudyProgress | null> {
    try {
      const progressData = await AsyncStorage.getItem(STORAGE_KEYS.PROGRESS);
      if (!progressData) return null;

      const allProgress: Record<string, StudyProgress> = JSON.parse(progressData);
      return allProgress[cardId] || null;
    } catch (error) {
      console.error('Error getting card progress:', error);
      return null;
    }
  }

  async saveCardProgress(cardId: string, progress: StudyProgress): Promise<void> {
    try {
      const progressData = await AsyncStorage.getItem(STORAGE_KEYS.PROGRESS);
      const allProgress: Record<string, StudyProgress> = progressData ? JSON.parse(progressData) : {};

      allProgress[cardId] = progress;
      await AsyncStorage.setItem(STORAGE_KEYS.PROGRESS, JSON.stringify(allProgress));
    } catch (error) {
      console.error('Error saving card progress:', error);
    }
  }

  async getAllProgress(): Promise<Record<string, StudyProgress>> {
    try {
      const progressData = await AsyncStorage.getItem(STORAGE_KEYS.PROGRESS);
      return progressData ? JSON.parse(progressData) : {};
    } catch (error) {
      console.error('Error getting all progress:', error);
      return {};
    }
  }

  async clearProgress(): Promise<void> {
    try {
      await AsyncStorage.removeItem(STORAGE_KEYS.PROGRESS);
    } catch (error) {
      console.error('Error clearing progress:', error);
    }
  }

  // ========== Session Management ==========

  async saveSession(session: StudySession): Promise<void> {
    try {
      const sessionsData = await AsyncStorage.getItem(STORAGE_KEYS.SESSIONS);
      const allSessions: StudySession[] = sessionsData ? JSON.parse(sessionsData) : [];

      allSessions.push(session);

      // Keep only last 100 sessions
      if (allSessions.length > 100) {
        allSessions.shift();
      }

      await AsyncStorage.setItem(STORAGE_KEYS.SESSIONS, JSON.stringify(allSessions));
    } catch (error) {
      console.error('Error saving session:', error);
    }
  }

  async getSessions(limit?: number): Promise<StudySession[]> {
    try {
      const sessionsData = await AsyncStorage.getItem(STORAGE_KEYS.SESSIONS);
      const allSessions: StudySession[] = sessionsData ? JSON.parse(sessionsData) : [];

      if (limit) {
        return allSessions.slice(-limit);
      }

      return allSessions;
    } catch (error) {
      console.error('Error getting sessions:', error);
      return [];
    }
  }

  async getSessionsByCategory(category: string): Promise<StudySession[]> {
    try {
      const sessions = await this.getSessions();
      return sessions.filter(s => s.category === category);
    } catch (error) {
      console.error('Error getting sessions by category:', error);
      return [];
    }
  }

  // ========== Statistics ==========

  async getStats(): Promise<AppStats> {
    try {
      const statsData = await AsyncStorage.getItem(STORAGE_KEYS.STATS);
      if (!statsData) {
        return this.createDefaultStats();
      }
      return JSON.parse(statsData);
    } catch (error) {
      console.error('Error getting stats:', error);
      return this.createDefaultStats();
    }
  }

  async updateStats(updates: Partial<AppStats>): Promise<void> {
    try {
      const currentStats = await this.getStats();
      const updatedStats = { ...currentStats, ...updates };
      await AsyncStorage.setItem(STORAGE_KEYS.STATS, JSON.stringify(updatedStats));
    } catch (error) {
      console.error('Error updating stats:', error);
    }
  }

  private createDefaultStats(): AppStats {
    return {
      totalCardsStudied: 0,
      totalSessionsCompleted: 0,
      totalTimeSpent: 0,
      lastStudyDate: new Date().toISOString(),
      streakCount: 0,
      streakStartDate: new Date().toISOString(),
    };
  }

  async incrementCardsStudied(count: number = 1): Promise<void> {
    try {
      const stats = await this.getStats();
      stats.totalCardsStudied += count;
      stats.lastStudyDate = new Date().toISOString();
      await this.updateStats(stats);
    } catch (error) {
      console.error('Error incrementing cards studied:', error);
    }
  }

  async incrementSessionsCompleted(): Promise<void> {
    try {
      const stats = await this.getStats();
      stats.totalSessionsCompleted += 1;
      stats.lastStudyDate = new Date().toISOString();
      await this.updateStats(stats);
    } catch (error) {
      console.error('Error incrementing sessions completed:', error);
    }
  }

  async addStudyTime(minutes: number): Promise<void> {
    try {
      const stats = await this.getStats();
      stats.totalTimeSpent += minutes;
      await this.updateStats(stats);
    } catch (error) {
      console.error('Error adding study time:', error);
    }
  }

  // ========== Streak Tracking ==========

  async updateStreak(): Promise<number> {
    try {
      const stats = await this.getStats();
      const today = new Date().setHours(0, 0, 0, 0);
      const lastStudy = new Date(stats.lastStudyDate).setHours(0, 0, 0, 0);
      const daysDiff = Math.floor((today - lastStudy) / (1000 * 60 * 60 * 24));

      if (daysDiff === 0) {
        // Studied today, keep streak
        return stats.streakCount;
      } else if (daysDiff === 1) {
        // Studied yesterday, increment streak
        stats.streakCount += 1;
        stats.lastStudyDate = new Date().toISOString();
        await this.updateStats(stats);
        return stats.streakCount;
      } else {
        // Streak broken
        stats.streakCount = 1;
        stats.streakStartDate = new Date().toISOString();
        stats.lastStudyDate = new Date().toISOString();
        await this.updateStats(stats);
        return 1;
      }
    } catch (error) {
      console.error('Error updating streak:', error);
      return 0;
    }
  }

  async getStreak(): Promise<number> {
    try {
      const stats = await this.getStats();
      return stats.streakCount;
    } catch (error) {
      console.error('Error getting streak:', error);
      return 0;
    }
  }

  // ========== User Preferences ==========

  async getPreferences(): Promise<UserPreferences> {
    try {
      const prefsData = await AsyncStorage.getItem(STORAGE_KEYS.PREFERENCES);
      if (!prefsData) {
        return this.createDefaultPreferences();
      }
      return JSON.parse(prefsData);
    } catch (error) {
      console.error('Error getting preferences:', error);
      return this.createDefaultPreferences();
    }
  }

  async savePreferences(preferences: UserPreferences): Promise<void> {
    try {
      await AsyncStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(preferences));
    } catch (error) {
      console.error('Error saving preferences:', error);
    }
  }

  private createDefaultPreferences(): UserPreferences {
    return {
      defaultDifficulty: ['beginner'],
      autoPlayAudio: false,
      showCulturalNotes: true,
      cardAutoAdvance: false,
      theme: 'light',
    };
  }

  async updatePreference<K extends keyof UserPreferences>(
    key: K,
    value: UserPreferences[K]
  ): Promise<void> {
    try {
      const prefs = await this.getPreferences();
      prefs[key] = value;
      await this.savePreferences(prefs);
    } catch (error) {
      console.error('Error updating preference:', error);
    }
  }

  // ========== Data Export/Import ==========

  async exportAllData(): Promise<string> {
    try {
      const progress = await this.getAllProgress();
      const sessions = await this.getSessions();
      const stats = await this.getStats();
      const preferences = await this.getPreferences();

      const exportData = {
        progress,
        sessions,
        stats,
        preferences,
        exportDate: new Date().toISOString(),
        version: '1.0.0',
      };

      return JSON.stringify(exportData, null, 2);
    } catch (error) {
      console.error('Error exporting data:', error);
      throw error;
    }
  }

  async importData(jsonData: string): Promise<void> {
    try {
      const importData = JSON.parse(jsonData);

      if (importData.progress) {
        await AsyncStorage.setItem(STORAGE_KEYS.PROGRESS, JSON.stringify(importData.progress));
      }
      if (importData.sessions) {
        await AsyncStorage.setItem(STORAGE_KEYS.SESSIONS, JSON.stringify(importData.sessions));
      }
      if (importData.stats) {
        await AsyncStorage.setItem(STORAGE_KEYS.STATS, JSON.stringify(importData.stats));
      }
      if (importData.preferences) {
        await AsyncStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(importData.preferences));
      }
    } catch (error) {
      console.error('Error importing data:', error);
      throw error;
    }
  }

  async clearAllData(): Promise<void> {
    try {
      await AsyncStorage.multiRemove([
        STORAGE_KEYS.PROGRESS,
        STORAGE_KEYS.SESSIONS,
        STORAGE_KEYS.STATS,
        STORAGE_KEYS.PREFERENCES,
        STORAGE_KEYS.STREAK,
      ]);
    } catch (error) {
      console.error('Error clearing all data:', error);
    }
  }
}

export const storageService = new StorageService();
