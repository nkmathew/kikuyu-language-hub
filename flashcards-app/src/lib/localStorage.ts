import { StudyProgress, StudySession, UserPreferences } from '@/types/flashcard';

const STORAGE_KEYS = {
  PROGRESS: 'kikuyu_flashcards_progress',
  SESSIONS: 'kikuyu_flashcards_sessions',
  PREFERENCES: 'kikuyu_flashcards_preferences',
  KNOWN_CARDS: 'kikuyu_flashcards_known',
  FLAGGED_CARDS: 'kikuyu_flashcards_flagged',
  FLAG_REASONS: 'kikuyu_flashcards_flag_reasons',
} as const;

class LocalStorageManager {
  // Progress tracking
  getProgress(): Record<string, StudyProgress> {
    if (typeof window === 'undefined') return {};

    try {
      const stored = localStorage.getItem(STORAGE_KEYS.PROGRESS);
      return stored ? JSON.parse(stored) : {};
    } catch (error) {
      console.error('Error loading progress:', error);
      return {};
    }
  }

  saveProgress(cardId: string, progress: StudyProgress): void {
    if (typeof window === 'undefined') return;

    try {
      const allProgress = this.getProgress();
      allProgress[cardId] = progress;
      localStorage.setItem(STORAGE_KEYS.PROGRESS, JSON.stringify(allProgress));
    } catch (error) {
      console.error('Error saving progress:', error);
    }
  }
  
  // Study sessions
  getSessions(): StudySession[] {
    if (typeof window === 'undefined') return [];
    
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.SESSIONS);
      return stored ? JSON.parse(stored) : [];
    } catch (error) {
      console.error('Error loading sessions:', error);
      return [];
    }
  }
  
  saveSession(session: StudySession): void {
    if (typeof window === 'undefined') return;
    
    try {
      const sessions = this.getSessions();
      sessions.push(session);
      // Keep only last 50 sessions
      const recentSessions = sessions.slice(-50);
      localStorage.setItem(STORAGE_KEYS.SESSIONS, JSON.stringify(recentSessions));
    } catch (error) {
      console.error('Error saving session:', error);
    }
  }
  
  // User preferences
  getPreferences(): UserPreferences {
    if (typeof window === 'undefined') {
      return this.getDefaultPreferences();
    }
    
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.PREFERENCES);
      const defaults = this.getDefaultPreferences();
      return stored ? { ...defaults, ...JSON.parse(stored) } : defaults;
    } catch (error) {
      console.error('Error loading preferences:', error);
      return this.getDefaultPreferences();
    }
  }
  
  savePreferences(preferences: Partial<UserPreferences>): void {
    if (typeof window === 'undefined') return;
    
    try {
      const current = this.getPreferences();
      const updated = { ...current, ...preferences };
      localStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(updated));
    } catch (error) {
      console.error('Error saving preferences:', error);
    }
  }
  
  private getDefaultPreferences(): UserPreferences {
    return {
      defaultDifficulty: ['beginner', 'intermediate'],
      autoPlayAudio: false,
      showCulturalNotes: true,
      cardAutoAdvance: false,
      theme: 'light',
    };
  }
  
  // Known cards tracking
  getKnownCards(): Set<string> {
    if (typeof window === 'undefined') return new Set();

    try {
      const stored = localStorage.getItem(STORAGE_KEYS.KNOWN_CARDS);
      return stored ? new Set(JSON.parse(stored)) : new Set();
    } catch (error) {
      console.error('Error loading known cards:', error);
      return new Set();
    }
  }

  markCardAsKnown(cardId: string): void {
    if (typeof window === 'undefined') return;

    try {
      const knownCards = this.getKnownCards();
      knownCards.add(cardId);
      localStorage.setItem(STORAGE_KEYS.KNOWN_CARDS, JSON.stringify(Array.from(knownCards)));
    } catch (error) {
      console.error('Error marking card as known:', error);
    }
  }

  markCardAsUnknown(cardId: string): void {
    if (typeof window === 'undefined') return;

    try {
      const knownCards = this.getKnownCards();
      knownCards.delete(cardId);
      localStorage.setItem(STORAGE_KEYS.KNOWN_CARDS, JSON.stringify(Array.from(knownCards)));
    } catch (error) {
      console.error('Error marking card as unknown:', error);
    }
  }

  // Flagged cards tracking
  getFlaggedCards(): Set<string> {
    if (typeof window === 'undefined') return new Set();

    try {
      const stored = localStorage.getItem(STORAGE_KEYS.FLAGGED_CARDS);
      return stored ? new Set(JSON.parse(stored)) : new Set();
    } catch (error) {
      console.error('Error loading flagged cards:', error);
      return new Set();
    }
  }

  flagCard(cardId: string): void {
    if (typeof window === 'undefined') return;

    try {
      const flaggedCards = this.getFlaggedCards();
      flaggedCards.add(cardId);
      localStorage.setItem(STORAGE_KEYS.FLAGGED_CARDS, JSON.stringify(Array.from(flaggedCards)));
    } catch (error) {
      console.error('Error flagging card:', error);
    }
  }

  unflagCard(cardId: string): void {
    if (typeof window === 'undefined') return;

    try {
      const flaggedCards = this.getFlaggedCards();
      flaggedCards.delete(cardId);
      localStorage.setItem(STORAGE_KEYS.FLAGGED_CARDS, JSON.stringify(Array.from(flaggedCards)));
    } catch (error) {
      console.error('Error unflagging card:', error);
    }
  }

  toggleFlag(cardId: string): boolean {
    if (typeof window === 'undefined') return false;

    try {
      const flaggedCards = this.getFlaggedCards();
      if (flaggedCards.has(cardId)) {
        this.unflagCard(cardId);
        return false;
      } else {
        this.flagCard(cardId);
        return true;
      }
    } catch (error) {
      console.error('Error toggling flag:', error);
      return false;
    }
  }

  // Flag reasons tracking
  getFlagReasons(): Record<string, string> {
    if (typeof window === 'undefined') return {};

    try {
      const stored = localStorage.getItem(STORAGE_KEYS.FLAG_REASONS);
      return stored ? JSON.parse(stored) : {};
    } catch (error) {
      console.error('Error loading flag reasons:', error);
      return {};
    }
  }

  setFlagReason(cardId: string, reason: string): void {
    if (typeof window === 'undefined') return;

    try {
      const reasons = this.getFlagReasons();
      reasons[cardId] = reason;
      localStorage.setItem(STORAGE_KEYS.FLAG_REASONS, JSON.stringify(reasons));
    } catch (error) {
      console.error('Error setting flag reason:', error);
    }
  }

  removeFlagReason(cardId: string): void {
    if (typeof window === 'undefined') return;

    try {
      const reasons = this.getFlagReasons();
      delete reasons[cardId];
      localStorage.setItem(STORAGE_KEYS.FLAG_REASONS, JSON.stringify(reasons));
    } catch (error) {
      console.error('Error removing flag reason:', error);
    }
  }
  
  // Statistics
  getStudyStats() {
    const sessions = this.getSessions();
    const progress = this.getProgress();
    const knownCards = this.getKnownCards();
    
    const totalStudyTime = sessions.reduce((total, session) => {
      if (session.endTime) {
        const start = new Date(session.startTime);
        const end = new Date(session.endTime);
        return total + (end.getTime() - start.getTime());
      }
      return total;
    }, 0);
    
    const totalCardsStudied = sessions.reduce((total, session) => total + session.cardsStudied, 0);
    const totalCorrectAnswers = sessions.reduce((total, session) => total + session.correctAnswers, 0);
    
    return {
      totalSessions: sessions.length,
      totalStudyTime,
      totalCardsStudied,
      totalCorrectAnswers,
      knownCardsCount: knownCards.size,
      averageAccuracy: totalCardsStudied > 0 ? (totalCorrectAnswers / totalCardsStudied) * 100 : 0,
      progressEntries: Object.keys(progress).length,
    };
  }
  
  // Clear data
  clearAllData(): void {
    if (typeof window === 'undefined') return;
    
    Object.values(STORAGE_KEYS).forEach(key => {
      localStorage.removeItem(key);
    });
  }
}

export const localStorageManager = new LocalStorageManager();