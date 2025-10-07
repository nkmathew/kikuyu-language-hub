export interface SubTranslation {
  source: string;
  target: string;
  position: number;
  context: string;
}

export interface Flashcard {
  id: number;
  english: string;
  kikuyu: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  context: string;
  cultural_notes: string;
  quality_score: number;
  categories: string[];
  has_sub_translations: boolean;
  sub_translations?: SubTranslation[];
}

export interface CategoryData {
  category: string;
  total_count: number;
  difficulty_counts: {
    beginner: number;
    intermediate: number;
    advanced: number;
  };
  items: {
    beginner: Flashcard[];
    intermediate: Flashcard[];
    advanced: Flashcard[];
    all: Flashcard[];
  };
}

export interface StudyProgress {
  cardId: number;
  difficulty: 'easy' | 'medium' | 'hard';
  lastReviewed: string;
  nextReview: string;
  repetitions: number;
  interval: number;
  easeFactor: number;
}

export interface StudySession {
  category: string;
  difficulty: string[];
  mode: 'flashcards' | 'quiz' | 'review';
  cardsStudied: number;
  correctAnswers: number;
  startTime: string;
  endTime?: string;
}

export interface UserPreferences {
  defaultDifficulty: string[];
  autoPlayAudio: boolean;
  showCulturalNotes: boolean;
  cardAutoAdvance: boolean;
  theme: 'light' | 'dark';
}

export type StudyMode = 'flashcards' | 'quiz' | 'review';
export type DifficultyLevel = 'beginner' | 'intermediate' | 'advanced';
export type CategoryType = 'vocabulary' | 'proverbs' | 'conjugations' | 'grammar' | 'general';