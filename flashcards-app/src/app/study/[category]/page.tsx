'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import FlashCard from '@/components/FlashCard';
import StudyCard from '@/components/StudyCard';
import ModeToggle from '@/components/ModeToggle';
import ThemeToggle from '@/components/ThemeToggle';
import { dataLoader } from '@/lib/dataLoader';
import { localStorageManager } from '@/lib/localStorage';
import { Flashcard, CategoryType, DifficultyLevel } from '@/types/flashcard';

interface StudyFlashcard extends Flashcard {
  _studied?: boolean;
}

// generateStaticParams removed - conflicts with "use client"
// For static export, we'll handle this differently in build process

export default function StudyPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const category = params.category as CategoryType;
  const difficultyFilter = searchParams.get('difficulty') as DifficultyLevel | null;
  
  const [cards, setCards] = useState<StudyFlashcard[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [knownCards, setKnownCards] = useState<Set<string>>(new Set());
  const [flaggedCards, setFlaggedCards] = useState<Set<string>>(new Set());
  const [sessionStartTime] = useState(new Date());
  const [cardsStudied, setCardsStudied] = useState(0);
  const [correctAnswers, setCorrectAnswers] = useState(0);
  const [mode, setMode] = useState<'study' | 'flashcard'>('study');
  const [sortBy, setSortBy] = useState<'recent' | 'alphabetical' | 'random'>('recent');

  // Load cards and user progress
  useEffect(() => {
    loadCards();
    loadUserProgress();
  }, [category, difficultyFilter, sortBy]);

  const loadCards = async () => {
    try {
      setLoading(true);
      const categoryData = await dataLoader.loadCategoryWithValidation(category, false);

      let cardList: StudyFlashcard[] = [];

      if (difficultyFilter) {
        cardList = categoryData.items[difficultyFilter] || [];
      } else {
        cardList = categoryData.items.all || [];
      }

      // Apply sorting
      const sortedCards = sortCards([...cardList], sortBy);
      setCards(sortedCards);

      if (sortedCards.length === 0) {
        setError('No flashcards found for this category and difficulty level.');
      }
    } catch (err) {
      setError('Failed to load flashcards. Please try again.');
      console.error('Error loading cards:', err);
    } finally {
      setLoading(false);
    }
  };

  const sortCards = (cardList: StudyFlashcard[], sortType: 'recent' | 'alphabetical' | 'random'): StudyFlashcard[] => {
    switch (sortType) {
      case 'recent':
        return cardList.sort((a, b) => {
          const dateA = a.source?.last_updated || a.source?.created_date || '';
          const dateB = b.source?.last_updated || b.source?.created_date || '';
          return dateB.localeCompare(dateA); // Most recent first
        });
      case 'alphabetical':
        return cardList.sort((a, b) => a.english.localeCompare(b.english));
      case 'random':
        return cardList.sort(() => Math.random() - 0.5);
      default:
        return cardList;
    }
  };

  const loadUserProgress = () => {
    const known = localStorageManager.getKnownCards();
    const flagged = localStorageManager.getFlaggedCards();
    setKnownCards(known);
    setFlaggedCards(flagged);
  };

  const handleFlip = useCallback(() => {
    // Track card as studied when flipped
    if (cards[currentIndex] && !cards[currentIndex]._studied) {
      cards[currentIndex]._studied = true;
      setCardsStudied(prev => prev + 1);
    }
  }, [cards, currentIndex]);

  const handleNext = useCallback(() => {
    if (currentIndex < cards.length - 1) {
      setCurrentIndex(currentIndex + 1);
    }
  }, [currentIndex, cards.length]);

  const handlePrevious = useCallback(() => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
    }
  }, [currentIndex]);

  const handleMarkKnown = useCallback(() => {
    const currentCard = cards[currentIndex];
    if (currentCard) {
      localStorageManager.markCardAsKnown(currentCard.id);
      setKnownCards(prev => new Set(Array.from(prev).concat(currentCard.id)));
      setCorrectAnswers(prev => prev + 1);
      
      // Auto-advance to next card
      setTimeout(() => {
        if (currentIndex < cards.length - 1) {
          setCurrentIndex(currentIndex + 1);
        }
      }, 500);
    }
  }, [cards, currentIndex]);

  const handleMarkUnknown = useCallback(() => {
    const currentCard = cards[currentIndex];
    if (currentCard) {
      localStorageManager.markCardAsUnknown(currentCard.id);
      setKnownCards(prev => {
        const newSet = new Set(prev);
        newSet.delete(currentCard.id);
        return newSet;
      });
    }
  }, [cards, currentIndex]);

  const handleToggleFlag = useCallback(() => {
    const currentCard = cards[currentIndex];
    if (currentCard) {
      const isNowFlagged = localStorageManager.toggleFlag(currentCard.id);
      setFlaggedCards(prev => {
        const newSet = new Set(prev);
        if (isNowFlagged) {
          newSet.add(currentCard.id);
        } else {
          newSet.delete(currentCard.id);
        }
        return newSet;
      });
    }
  }, [cards, currentIndex]);

  // Save study session when component unmounts or user leaves
  useEffect(() => {
    const saveSession = () => {
      if (cardsStudied > 0) {
        localStorageManager.saveSession({
          category,
          difficulty: difficultyFilter ? [difficultyFilter] : ['all'],
          mode: 'flashcards' as const,
          cardsStudied,
          correctAnswers,
          startTime: sessionStartTime.toISOString(),
          endTime: new Date().toISOString(),
        });
      }
    };

    const handleBeforeUnload = () => saveSession();
    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      saveSession();
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [category, difficultyFilter, cardsStudied, correctAnswers, sessionStartTime]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-kikuyu-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading flashcards...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <div className="space-x-4">
            <button 
              onClick={loadCards}
              className="btn btn-primary"
            >
              Try Again
            </button>
            <Link href="/" className="btn btn-secondary">
              Back to Home
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (cards.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <p className="text-gray-600 mb-4">No flashcards available for this selection.</p>
          <Link href="/" className="btn btn-primary">
            Back to Home
          </Link>
        </div>
      </div>
    );
  }

  const currentCard = cards[currentIndex];
  const isCardKnown = knownCards.has(currentCard.id);
  const progress = ((currentIndex + 1) / cards.length) * 100;

  return (
    <div className={`mx-auto ${mode === 'study' ? 'max-w-7xl' : 'max-w-4xl'}`}>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center space-x-4">
          <Link href="/" className="btn btn-secondary">
            ← Back
          </Link>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 capitalize">
            {category} {difficultyFilter && `- ${difficultyFilter}`}
          </h1>
        </div>

        <div className="flex items-center gap-4">
          <ThemeToggle />
          <ModeToggle mode={mode} onModeChange={setMode} />
          <div className="text-right">
            <div className="text-sm text-gray-500 dark:text-gray-400">
              {mode === 'flashcard' ? `Card ${currentIndex + 1} of ${cards.length}` : `${cards.length} cards`}
            </div>
            <div className="text-xs text-gray-400 dark:text-gray-500">
              {cardsStudied} studied • {correctAnswers} known
            </div>
          </div>
        </div>
      </div>

      {/* Study Options - Moved to Top */}
      <div className="mb-6 card p-4">
        <div className="grid md:grid-cols-2 gap-4">
          {/* Difficulty Filter */}
          <div>
            <h3 className="text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">Difficulty Level</h3>
            <div className="flex flex-wrap gap-2">
              <Link
                href={`/study/${category}`}
                className={`btn ${!difficultyFilter ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                All Levels
              </Link>
              <Link
                href={`/study/${category}?difficulty=beginner`}
                className={`btn ${difficultyFilter === 'beginner' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                Beginner
              </Link>
              <Link
                href={`/study/${category}?difficulty=intermediate`}
                className={`btn ${difficultyFilter === 'intermediate' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                Intermediate
              </Link>
              <Link
                href={`/study/${category}?difficulty=advanced`}
                className={`btn ${difficultyFilter === 'advanced' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                Advanced
              </Link>
            </div>
          </div>

          {/* Sort Options */}
          <div>
            <h3 className="text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">Sort By</h3>
            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => setSortBy('recent')}
                className={`btn ${sortBy === 'recent' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                📅 Recently Updated
              </button>
              <button
                onClick={() => setSortBy('alphabetical')}
                className={`btn ${sortBy === 'alphabetical' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                🔤 A-Z
              </button>
              <button
                onClick={() => setSortBy('random')}
                className={`btn ${sortBy === 'random' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              >
                🔀 Random
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Progress Bar (Flashcard Mode Only) */}
      {mode === 'flashcard' && (
        <div className="mb-6">
          <div className="flex justify-between text-sm text-gray-500 dark:text-gray-400 mb-1">
            <span>Progress</span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
            <div 
              className="bg-gradient-to-r from-kikuyu-500 to-green-500 h-2 rounded-full"
              style={{ width: `${progress}%` }}
            ></div>
          </div>
        </div>
      )}

      {/* Study/Flashcard Content */}
      {mode === 'study' ? (
        <div className="space-y-6 mb-6">
          {cards.map((card, index) => (
            <StudyCard
              key={card.id}
              card={card}
              onMarkKnown={() => {
                localStorageManager.markCardAsKnown(card.id);
                setKnownCards(prev => new Set(Array.from(prev).concat(card.id)));
                setCorrectAnswers(prev => prev + 1);
                if (!card._studied) {
                  card._studied = true;
                  setCardsStudied(prev => prev + 1);
                }
              }}
              onMarkUnknown={() => {
                localStorageManager.markCardAsUnknown(card.id);
                setKnownCards(prev => {
                  const newSet = new Set(prev);
                  newSet.delete(card.id);
                  return newSet;
                });
              }}
              onToggleFlag={() => {
                const isNowFlagged = localStorageManager.toggleFlag(card.id);
                setFlaggedCards(prev => {
                  const newSet = new Set(prev);
                  if (isNowFlagged) {
                    newSet.add(card.id);
                  } else {
                    newSet.delete(card.id);
                  }
                  return newSet;
                });
              }}
              isKnown={knownCards.has(card.id)}
              isFlagged={flaggedCards.has(card.id)}
              className="mb-6"
            />
          ))}
        </div>
      ) : (
        <FlashCard
          card={currentCard}
          onFlip={handleFlip}
          onNext={currentIndex < cards.length - 1 ? handleNext : undefined}
          onPrevious={currentIndex > 0 ? handlePrevious : undefined}
          onMarkKnown={handleMarkKnown}
          onMarkUnknown={handleMarkUnknown}
          onToggleFlag={handleToggleFlag}
          isKnown={isCardKnown}
          isFlagged={flaggedCards.has(currentCard.id)}
          className="mb-6"
        />
      )}

      {/* Session Complete (Flashcard Mode Only) */}
      {mode === 'flashcard' && currentIndex === cards.length - 1 && (
        <div className="card text-center">
          <h2 className="text-xl font-bold mb-4">🎉 Session Complete!</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div>
              <div className="text-2xl font-bold text-kikuyu-600">{cards.length}</div>
              <div className="text-sm text-gray-500 dark:text-gray-400">Total Cards</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">{cardsStudied}</div>
              <div className="text-sm text-gray-500 dark:text-gray-400">Studied</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-blue-600">{correctAnswers}</div>
              <div className="text-sm text-gray-500 dark:text-gray-400">Known</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">
                {cardsStudied > 0 ? Math.round((correctAnswers / cardsStudied) * 100) : 0}%
              </div>
              <div className="text-sm text-gray-500 dark:text-gray-400">Accuracy</div>
            </div>
          </div>

          <div className="space-x-4">
            <button
              onClick={() => {
                setCurrentIndex(0);
                setCardsStudied(0);
                setCorrectAnswers(0);
                // Re-sort cards based on current sort setting
                const resortedCards = sortCards([...cards], sortBy);
                setCards(resortedCards);
              }}
              className="btn btn-primary"
            >
              Study Again
            </button>
            <Link href="/" className="btn btn-secondary">
              Back to Home
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}