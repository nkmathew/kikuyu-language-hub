import { StudyProgress, StudySession, UserPreferences } from '@/types/flashcard';

const STORAGE_KEYS = {
  PROGRESS: 'kikuyu_flashcards_progress',
  SESSIONS: 'kikuyu_flashcards_sessions',
  PREFERENCES: 'kikuyu_flashcards_preferences',
  KNOWN_CARDS: 'kikuyu_flashcards_known',
} as const;

class LocalStorageManager {
  // Progress tracking
  getProgress(): Record<number, StudyProgress> {
    if (typeof window === 'undefined') return {};
    
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.PROGRESS);
      return stored ? JSON.parse(stored) : {};
    } catch (error) {
      console.error('Error loading progress:', error);
      return {};
    }
  }
  
  saveProgress(cardId: number, progress: StudyProgress): void {
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
  getKnownCards(): Set<number> {
    if (typeof window === 'undefined') return new Set();
    
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.KNOWN_CARDS);
      return stored ? new Set(JSON.parse(stored)) : new Set();
    } catch (error) {
      console.error('Error loading known cards:', error);
      return new Set();
    }
  }
  
  markCardAsKnown(cardId: number): void {
    if (typeof window === 'undefined') return;
    
    try {
      const knownCards = this.getKnownCards();
      knownCards.add(cardId);
      localStorage.setItem(STORAGE_KEYS.KNOWN_CARDS, JSON.stringify(Array.from(knownCards)));
    } catch (error) {
      console.error('Error marking card as known:', error);
    }
  }
  
  markCardAsUnknown(cardId: number): void {
    if (typeof window === 'undefined') return;
    
    try {
      const knownCards = this.getKnownCards();
      knownCards.delete(cardId);
      localStorage.setItem(STORAGE_KEYS.KNOWN_CARDS, JSON.stringify(Array.from(knownCards)));
    } catch (error) {
      console.error('Error marking card as unknown:', error);
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