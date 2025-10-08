export interface SubTranslation {
  source: string;
  target: string;
  position: number;
  context: string;
}

export interface ExampleSentence {
  english: string;
  kikuyu: string;
  context?: string;
}

export interface PronunciationInfo {
  ipa?: string;
  simplified?: string;
  audio_file?: string;
}

export interface GrammaticalInfo {
  part_of_speech?: string;
  verb_class?: string;
  noun_class?: string;
  infinitive?: string;
}

export interface QualityInfo {
  verified: boolean;
  confidence_score: number;
  source_quality: 'native_speaker' | 'academic' | 'dictionary' | 'community' | 'automated';
  reviewer?: string;
  review_date?: string;
}

export interface SourceInfo {
  origin: string;
  attribution?: string;
  license?: string;
  url?: string;
  created_date?: string;
  last_updated?: string;
}

export interface Flashcard {
  id: string;
  english: string;
  kikuyu: string;
  category: 'vocabulary' | 'proverbs' | 'grammar' | 'conjugations' | 'cultural' | 'numbers' | 'phrases';
  subcategory?: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  context?: string;
  cultural_notes?: string;
  pronunciation?: PronunciationInfo;
  examples?: ExampleSentence[];
  grammatical_info?: GrammaticalInfo;
  tags?: string[];
  quality?: QualityInfo;
  source?: SourceInfo;
  // Legacy fields for backward compatibility
  quality_score?: number;
  categories?: string[];
  has_sub_translations?: boolean;
  sub_translations?: SubTranslation[];
  notes?: string;
}

export interface BatchInfo {
  batch_number: string;
  total_cards: number;
  category: string;
  source_files: string[];
  created_date: string;
  last_updated?: string;
  description?: string;
}

export interface CuratedContentMetadata {
  schema_version: string;
  created_date: string;
  last_updated?: string;
  curator: string;
  source_files: string[];
  total_entries: number;
  description?: string;
}

export interface CuratedContent {
  batch_info?: BatchInfo;
  metadata?: CuratedContentMetadata;
  flashcards?: Flashcard[];
  entries?: Flashcard[];
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
  cardId: string;
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
export type CategoryType = 'vocabulary' | 'proverbs' | 'conjugations' | 'grammar' | 'general' | 'phrases' | 'all';
