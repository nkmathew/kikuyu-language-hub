'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import StudyCard from '@/components/StudyCard';
import { Flashcard } from '@/types/flashcard';
import { localStorageManager } from '@/lib/localStorage';
import { dataLoader } from '@/lib/dataLoader';

export default function FlaggedTranslationsPage() {
  const [flaggedCards, setFlaggedCards] = useState<Flashcard[]>([]);
  const [loading, setLoading] = useState(true);
  const [flagReasons, setFlagReasons] = useState<Record<string, string>>({});
  const router = useRouter();

  useEffect(() => {
    loadFlaggedCards();
  }, []);

  const loadFlaggedCards = async () => {
    try {
      setLoading(true);
      
      // Get flagged card IDs from localStorage
      const flaggedIds = localStorageManager.getFlaggedCards();
      const reasons = localStorageManager.getFlagReasons();
      
      if (flaggedIds.size === 0) {
        setFlaggedCards([]);
        setFlagReasons({});
        return;
      }

      // Load all categories to find flagged cards
      const allCards: Flashcard[] = [];
      const categories = ['general', 'greetings', 'family', 'numbers', 'colors', 'animals', 'food', 'time', 'weather'];
      
      for (const category of categories) {
        try {
          const categoryData = await dataLoader.loadCategoryWithValidation(category as any, false);
          const categoryCards = categoryData.items.all || [];
          allCards.push(...categoryCards);
        } catch (error) {
          console.error(`Error loading category ${category}:`, error);
        }
      }

      // Filter to only flagged cards
      const flagged = allCards.filter(card => flaggedIds.has(card.id));
      
      setFlaggedCards(flagged);
      setFlagReasons(reasons);
    } catch (error) {
      console.error('Error loading flagged cards:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUnflagCard = (cardId: string) => {
    localStorageManager.unflagCard(cardId);
    localStorageManager.removeFlagReason(cardId);
    
    setFlaggedCards(prev => prev.filter(card => card.id !== cardId));
    setFlagReasons(prev => {
      const newReasons = { ...prev };
      delete newReasons[cardId];
      return newReasons;
    });
  };

  const handleExportFlagged = () => {
    if (flaggedCards.length === 0) {
      alert('No flagged translations to export');
      return;
    }

    const exportData = flaggedCards.map(card => ({
      kikuyu: card.kikuyu,
      english: card.english,
      difficulty: card.difficulty,
      category: card.category,
      notes: card.cultural_notes || card.notes || '',
      flagReason: flagReasons[card.id] || '',
      source: card.source?.origin || '',
    }));

    const jsonString = JSON.stringify(exportData, null, 2);
    
    // Create and download file
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `flagged-translations-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleClearAll = () => {
    if (flaggedCards.length === 0) return;
    
    if (confirm(`Are you sure you want to remove all ${flaggedCards.length} flagged translations?`)) {
      flaggedCards.forEach(card => {
        localStorageManager.unflagCard(card.id);
        localStorageManager.removeFlagReason(card.id);
      });
      
      setFlaggedCards([]);
      setFlagReasons({});
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
        <div className="container mx-auto px-4 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
              <p className="text-gray-600 dark:text-gray-400">Loading flagged translations...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
                Flagged Translations
              </h1>
              <p className="text-gray-600 dark:text-gray-400">
                Review and manage your flagged translations ({flaggedCards.length} items)
              </p>
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => router.back()}
                className="btn btn-secondary"
              >
                ← Back
              </button>
              {flaggedCards.length > 0 && (
                <>
                  <button
                    onClick={handleExportFlagged}
                    className="btn btn-success"
                  >
                    📥 Export
                  </button>
                  <button
                    onClick={handleClearAll}
                    className="btn btn-danger"
                  >
                    🗑️ Clear All
                  </button>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Content */}
        {flaggedCards.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">🏳️</div>
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
              No Flagged Translations
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              Flag translations in the study list to see them here.
            </p>
            <button
              onClick={() => router.push('/study/general')}
              className="btn btn-primary"
            >
              Go to Study List
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {flaggedCards.map((card) => (
              <div key={card.id} className="relative">
                <StudyCard
                  card={card}
                  isFlagged={true}
                  onToggleFlag={() => handleUnflagCard(card.id)}
                  className="border-l-4 border-red-500"
                />
                
                {/* Flag reason if available */}
                {flagReasons[card.id] && (
                  <div className="mt-2 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
                    <p className="text-sm text-red-800 dark:text-red-200">
                      <span className="font-medium">Flag reason:</span> {flagReasons[card.id]}
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
