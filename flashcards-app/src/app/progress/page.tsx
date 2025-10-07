'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { localStorageManager } from '@/lib/localStorage';
import { dataLoader } from '@/lib/dataLoader';
import { CategoryType } from '@/types/flashcard';

interface CategoryProgress {
  category: CategoryType;
  totalCards: number;
  knownCards: number;
  percentage: number;
  difficultyBreakdown: {
    beginner: { total: number; known: number };
    intermediate: { total: number; known: number };
    advanced: { total: number; known: number };
  };
}

export default function ProgressPage() {
  const [stats, setStats] = useState<any>({});
  const [categoryProgress, setCategoryProgress] = useState<CategoryProgress[]>([]);
  const [sessions, setSessions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProgressData();
  }, []);

  const loadProgressData = async () => {
    try {
      setLoading(true);
      
      // Load basic stats
      const studyStats = localStorageManager.getStudyStats();
      setStats(studyStats);
      
      // Load study sessions
      const studySessions = localStorageManager.getSessions();
      setSessions(studySessions.slice(-10)); // Show last 10 sessions
      
      // Load known cards
      const knownCards = localStorageManager.getKnownCards();
      
      // Load all categories to calculate progress
      const categories = await dataLoader.loadAllCategories();
      const progressData: CategoryProgress[] = [];
      
      for (const [categoryKey, categoryData] of Object.entries(categories)) {
        const category = categoryKey as CategoryType;
        
        // Count known cards in each difficulty
        const difficulties = ['beginner', 'intermediate', 'advanced'] as const;
        const difficultyBreakdown = {
          beginner: { total: 0, known: 0 },
          intermediate: { total: 0, known: 0 },
          advanced: { total: 0, known: 0 },
        };
        
        let totalKnownInCategory = 0;
        
        for (const difficulty of difficulties) {
          const cards = categoryData.items[difficulty] || [];
          difficultyBreakdown[difficulty].total = cards.length;
          difficultyBreakdown[difficulty].known = cards.filter(card => knownCards.has(card.id)).length;
          totalKnownInCategory += difficultyBreakdown[difficulty].known;
        }
        
        const totalCards = categoryData.total_count;
        const percentage = totalCards > 0 ? Math.round((totalKnownInCategory / totalCards) * 100) : 0;
        
        progressData.push({
          category,
          totalCards,
          knownCards: totalKnownInCategory,
          percentage,
          difficultyBreakdown,
        });
      }
      
      setCategoryProgress(progressData);
    } catch (error) {
      console.error('Error loading progress data:', error);
    } finally {
      setLoading(false);
    }
  };

  const clearAllProgress = () => {
    if (confirm('Are you sure you want to clear all progress? This cannot be undone.')) {
      localStorageManager.clearAllData();
      loadProgressData();
    }
  };

  const formatDuration = (ms: number) => {
    const minutes = Math.floor(ms / (1000 * 60));
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes % 60}m`;
    }
    return `${minutes}m`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const categoryNames: Record<CategoryType, string> = {
    vocabulary: 'Vocabulary',
    proverbs: 'Proverbs & Wisdom',
    conjugations: 'Verb Conjugations',
    grammar: 'Grammar Rules',
    phrases: 'Common Phrases',
    general: 'All Content'
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-kikuyu-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Loading progress data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Your Progress</h1>
          <p className="text-gray-600 dark:text-gray-400">Track your Kikuyu language learning journey</p>
        </div>
        <Link href="/" className="btn btn-secondary">
          ← Back to Home
        </Link>
      </div>

      {/* Overall Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card text-center">
          <div className="text-3xl font-bold text-kikuyu-600 mb-2">{stats.totalSessions || 0}</div>
          <div className="text-gray-600 dark:text-gray-400">Study Sessions</div>
        </div>
        <div className="card text-center">
          <div className="text-3xl font-bold text-green-600 mb-2">{stats.knownCardsCount || 0}</div>
          <div className="text-gray-600 dark:text-gray-400">Cards Learned</div>
        </div>
        <div className="card text-center">
          <div className="text-3xl font-bold text-blue-600 mb-2">
            {Math.round(stats.averageAccuracy || 0)}%
          </div>
          <div className="text-gray-600 dark:text-gray-400">Average Accuracy</div>
        </div>
        <div className="card text-center">
          <div className="text-3xl font-bold text-purple-600 mb-2">
            {formatDuration(stats.totalStudyTime || 0)}
          </div>
          <div className="text-gray-600 dark:text-gray-400">Study Time</div>
        </div>
      </div>

      {/* Category Progress */}
      <div className="card mb-8">
        <h2 className="text-xl font-bold mb-6">Progress by Category</h2>
        <div className="space-y-6">
          {categoryProgress.map((progress) => (
            <div key={progress.category} className="border-b border-gray-200 pb-6 last:border-b-0 last:pb-0">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-lg font-semibold">{categoryNames[progress.category]}</h3>
                <div className="text-right">
                  <span className="text-lg font-bold text-kikuyu-600">{progress.percentage}%</span>
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    {progress.knownCards}/{progress.totalCards} cards
                  </div>
                </div>
              </div>
              
              {/* Progress Bar */}
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-3 mb-4">
                <div 
                  className="bg-gradient-to-r from-kikuyu-500 to-green-500 h-3 rounded-full"
                  style={{ width: `${progress.percentage}%` }}
                ></div>
              </div>
              
              {/* Difficulty Breakdown */}
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div className="text-center p-3 bg-green-50 rounded-lg">
                  <div className="font-semibold text-green-600">
                    {progress.difficultyBreakdown.beginner.known}/{progress.difficultyBreakdown.beginner.total}
                  </div>
                  <div className="text-green-500">Beginner</div>
                </div>
                <div className="text-center p-3 bg-yellow-50 rounded-lg">
                  <div className="font-semibold text-yellow-600">
                    {progress.difficultyBreakdown.intermediate.known}/{progress.difficultyBreakdown.intermediate.total}
                  </div>
                  <div className="text-yellow-500">Intermediate</div>
                </div>
                <div className="text-center p-3 bg-red-50 rounded-lg">
                  <div className="font-semibold text-red-600">
                    {progress.difficultyBreakdown.advanced.known}/{progress.difficultyBreakdown.advanced.total}
                  </div>
                  <div className="text-red-500">Advanced</div>
                </div>
              </div>
              
              {/* Study Link */}
              <div className="mt-4">
                <Link 
                  href={`/study/${progress.category}`}
                  className="btn btn-primary btn-sm"
                >
                  Continue Studying
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Sessions */}
      {sessions.length > 0 && (
        <div className="card mb-8">
          <h2 className="text-xl font-bold mb-6">Recent Study Sessions</h2>
          <div className="space-y-3">
            {sessions.map((session, index) => (
              <div key={index} className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <div>
                  <div className="font-medium capitalize">
                    {categoryNames[session.category as CategoryType] || session.category}
                    {session.difficulty !== 'all' && (
                      <span className="ml-2 text-sm text-gray-500 dark:text-gray-400">({session.difficulty})</span>
                    )}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    {formatDate(session.startTime)}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-sm font-medium">
                    {session.correctAnswers}/{session.cardsStudied} correct
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    {session.cardsStudied > 0 
                      ? Math.round((session.correctAnswers / session.cardsStudied) * 100)
                      : 0}% accuracy
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="card">
        <h2 className="text-xl font-bold mb-4">Manage Progress</h2>
        <div className="space-y-4">
          <p className="text-gray-600 dark:text-gray-400">
            Your progress is saved locally in your browser. This data includes which cards you've marked as known 
            and your study session history.
          </p>
          <div className="flex space-x-4">
            <button 
              onClick={clearAllProgress}
              className="btn btn-warning"
            >
              Clear All Progress
            </button>
            <Link href="/" className="btn btn-primary">
              Continue Learning
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}