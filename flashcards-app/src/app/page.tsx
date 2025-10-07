'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { dataLoader } from '@/lib/dataLoader';
import { localStorageManager } from '@/lib/localStorage';
import { CategoryData, CategoryType } from '@/types/flashcard';

interface CategoryCardProps {
  category: CategoryType;
  data: CategoryData;
  stats: any;
}

function CategoryCard({ category, data, stats }: CategoryCardProps) {
  const categoryNames = {
    vocabulary: 'Vocabulary',
    proverbs: 'Proverbs & Wisdom',
    conjugations: 'Verb Conjugations',
    grammar: 'Grammar Rules',
    general: 'General Content'
  };

  const categoryDescriptions = {
    vocabulary: 'Essential words and everyday terms',
    proverbs: 'Traditional wisdom and cultural sayings',
    conjugations: 'Verb patterns and tenses',
    grammar: 'Language rules and structures',
    general: 'Mixed content and expressions'
  };

  const categoryIcons = {
    vocabulary: '📚',
    proverbs: '🏛️',
    conjugations: '🔄',
    grammar: '📖',
    general: '🌟'
  };

  const knownCount = stats.knownCardsCount || 0;
  const progressPercentage = data.total_count > 0 ? Math.round((knownCount / data.total_count) * 100) : 0;

  return (
    <Link href={`/study/${category}`} className="block">
      <div className="card hover:shadow-xl transform hover:-translate-y-2 transition-all duration-300 h-full">
        <div className="flex flex-col h-full">
          {/* Icon and Title */}
          <div className="flex items-center mb-4">
            <span className="text-4xl mr-3">{categoryIcons[category]}</span>
            <div>
              <h3 className="text-xl font-bold text-gray-900">{categoryNames[category]}</h3>
              <p className="text-sm text-gray-600">{categoryDescriptions[category]}</p>
            </div>
          </div>

          {/* Stats */}
          <div className="flex-1">
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-kikuyu-600">{data.total_count}</div>
                <div className="text-xs text-gray-500">Total Cards</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">{knownCount}</div>
                <div className="text-xs text-gray-500">Known</div>
              </div>
            </div>

            {/* Progress Bar */}
            <div className="mb-4">
              <div className="flex justify-between text-xs text-gray-500 mb-1">
                <span>Progress</span>
                <span>{progressPercentage}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-gradient-to-r from-kikuyu-500 to-green-500 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${progressPercentage}%` }}
                ></div>
              </div>
            </div>

            {/* Difficulty Breakdown */}
            <div className="grid grid-cols-3 gap-2 text-xs">
              <div className="text-center p-2 bg-green-50 rounded">
                <div className="font-semibold text-green-600">{data.difficulty_counts.beginner}</div>
                <div className="text-green-500">Beginner</div>
              </div>
              <div className="text-center p-2 bg-yellow-50 rounded">
                <div className="font-semibold text-yellow-600">{data.difficulty_counts.intermediate}</div>
                <div className="text-yellow-500">Intermediate</div>
              </div>
              <div className="text-center p-2 bg-red-50 rounded">
                <div className="font-semibold text-red-600">{data.difficulty_counts.advanced}</div>
                <div className="text-red-500">Advanced</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Link>
  );
}

export default function HomePage() {
  const [categories, setCategories] = useState<Record<CategoryType, CategoryData> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<any>({});

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [categoryData, studyStats] = await Promise.all([
        dataLoader.loadAllCategories(),
        Promise.resolve(localStorageManager.getStudyStats())
      ]);
      
      setCategories(categoryData);
      setStats(studyStats);
    } catch (err) {
      setError('Failed to load flashcard data. Please refresh the page.');
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-kikuyu-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading flashcard data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button 
            onClick={loadData}
            className="btn btn-primary"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="text-center mb-12">
        <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
          🇰🇪 Kikuyu Flashcards
        </h1>
        <p className="text-xl text-gray-600 mb-6">
          Learn Kikuyu language with interactive flashcards
        </p>
        <p className="text-lg text-gray-500 max-w-3xl mx-auto">
          Study vocabulary, traditional proverbs, and verb conjugations from authentic native speaker content. 
          Over 3,000+ flashcards to help you master the Kikuyu language.
        </p>
      </div>

      {/* Study Stats Summary */}
      {stats.totalSessions > 0 && (
        <div className="card mb-8">
          <h2 className="text-xl font-bold mb-4">Your Progress</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-kikuyu-600">{stats.totalSessions}</div>
              <div className="text-sm text-gray-500">Study Sessions</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">{stats.knownCardsCount}</div>
              <div className="text-sm text-gray-500">Cards Learned</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-blue-600">{Math.round(stats.averageAccuracy)}%</div>
              <div className="text-sm text-gray-500">Accuracy</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">
                {Math.round(stats.totalStudyTime / (1000 * 60))}m
              </div>
              <div className="text-sm text-gray-500">Study Time</div>
            </div>
          </div>
        </div>
      )}

      {/* Category Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        {categories && Object.entries(categories).map(([categoryKey, categoryData]) => (
          <CategoryCard
            key={categoryKey}
            category={categoryKey as CategoryType}
            data={categoryData}
            stats={stats}
          />
        ))}
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2 className="text-xl font-bold mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link href="/study/vocabulary?difficulty=beginner" className="btn btn-primary text-center">
            🌱 Start with Beginner Vocabulary
          </Link>
          <Link href="/study/proverbs" className="btn btn-secondary text-center">
            🏛️ Explore Traditional Wisdom
          </Link>
          <Link href="/progress" className="btn btn-secondary text-center">
            📊 View Detailed Progress
          </Link>
        </div>
      </div>

      {/* Footer */}
      <div className="text-center mt-12 text-gray-500">
        <p>Content sourced from native Kikuyu speakers</p>
        <p className="text-sm mt-2">
          Built with ❤️ for the Kikuyu language learning community
        </p>
      </div>
    </div>
  );
}