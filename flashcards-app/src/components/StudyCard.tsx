'use client';

import { useState } from 'react';
import { Flashcard } from '@/types/flashcard';
import ReportCard from './ReportCard';

interface StudyCardProps {
  card: Flashcard;
  showCulturalNotes?: boolean;
  onMarkKnown?: () => void;
  onMarkUnknown?: () => void;
  isKnown?: boolean;
  className?: string;
}

export default function StudyCard({
  card,
  showCulturalNotes = true,
  onMarkKnown,
  onMarkUnknown,
  isKnown = false,
  className = ''
}: StudyCardProps) {
  const [copyFeedback, setCopyFeedback] = useState<string | null>(null);

  const handleCopyCard = async () => {
    try {
      let copyText = `Source: ${card.english}\nTranslation: ${card.kikuyu}`;
      
      // Add card ID for reference
      const cardId = typeof card.id === 'string' ? card.id : `#${card.id}`;
      copyText += `\n[ID: ${cardId}]`;
      
      // Add pronunciation if available
      if (card.pronunciation?.ipa) {
        copyText += `\nPronunciation: /${card.pronunciation.ipa}/`;
      } else if (card.pronunciation?.simplified) {
        copyText += `\nPronunciation: ${card.pronunciation.simplified}`;
      }
      
      // Add context if available
      if (card.context) {
        copyText += `\nContext: ${card.context}`;
      }
      
      // Add cultural notes if available and shown
      if (showCulturalNotes && card.cultural_notes) {
        copyText += `\nCultural Note: ${card.cultural_notes}`;
      }
      
      // Add example if available
      if (card.examples && card.examples.length > 0) {
        const firstExample = card.examples[0];
        copyText += `\nExample Source: ${firstExample.english}\nExample Translation: ${firstExample.kikuyu}`;
      }
      
      // Add source info for curated content
      if (card.source?.origin) {
        copyText += `\nContent Source: ${card.source.origin}`;
      }
      
      await navigator.clipboard.writeText(copyText);
      setCopyFeedback('Copied!');
      setTimeout(() => setCopyFeedback(null), 2000);
    } catch (error) {
      console.error('Failed to copy:', error);
      setCopyFeedback('Copy failed');
      setTimeout(() => setCopyFeedback(null), 2000);
    }
  };

  return (
    <div className={`card mb-6 ${className}`}>
      {/* Card Status Indicator */}
      {isKnown && (
        <div className="flex justify-end mb-3">
          <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
            ✓ Known
          </span>
        </div>
      )}

      <div className="flex flex-col xl:flex-row xl:gap-8">
        {/* Main Content */}
        <div className="flex-1">
          {/* Languages Side by Side */}
          <div className="grid md:grid-cols-2 gap-6 mb-6">
            {/* English */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="px-3 py-1 bg-blue-100 text-blue-800 text-sm rounded-full font-medium">
                  English
                </span>
                <span className={`px-2 py-1 text-xs rounded-full font-medium ${
                  card.difficulty === 'beginner' ? 'bg-green-100 text-green-800' :
                  card.difficulty === 'intermediate' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-red-100 text-red-800'
                }`}>
                  {card.difficulty}
                </span>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <h3 className="text-lg md:text-xl font-bold text-gray-900 dark:text-gray-100 leading-relaxed">
                  {card.english}
                </h3>
              </div>
            </div>

            {/* Kikuyu */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="px-3 py-1 bg-kikuyu-100 text-kikuyu-800 text-sm rounded-full font-medium">
                  Kikuyu
                </span>
                <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs rounded-full font-medium">
                  Score: {(card.quality_score || card.quality?.confidence_score || 0).toFixed(1)}
                </span>
              </div>
              <div className="p-4 bg-kikuyu-50 rounded-lg">
                <h3 className="text-lg md:text-xl font-bold text-kikuyu-900 leading-relaxed">
                  {card.kikuyu}
                </h3>
                {/* Pronunciation */}
                {card.pronunciation && (
                  <div className="mt-2 pt-2 border-t border-kikuyu-200">
                    {card.pronunciation.ipa && (
                      <p className="text-sm text-gray-600 font-mono">
                        IPA: /{card.pronunciation.ipa}/
                      </p>
                    )}
                    {card.pronunciation.simplified && (
                      <p className="text-sm text-gray-600">
                        {card.pronunciation.simplified}
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Context and Cultural Notes */}
          {(card.context || (showCulturalNotes && card.cultural_notes)) && (
            <div className="mb-6 p-4 bg-blue-50 rounded-lg space-y-2">
              {card.context && (
                <p className="text-sm text-gray-700">
                  <span className="font-medium">Context:</span> {card.context}
                </p>
              )}
              {showCulturalNotes && card.cultural_notes && (
                <p className="text-xs text-gray-600">
                  <span className="font-medium">Cultural Note:</span> {card.cultural_notes}
                </p>
              )}
            </div>
          )}
        </div>

        {/* Sidebar Content */}
        <div className="xl:w-80 xl:flex-shrink-0 space-y-6">
          {/* Examples */}
          {card.examples && card.examples.length > 0 && (
            <div>
              <h4 className="text-md font-semibold mb-3 text-gray-900 dark:text-gray-100">Examples</h4>
              <div className="space-y-3">
                {card.examples.slice(0, 2).map((example, index) => (
                  <div key={index} className="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <p className="font-medium text-gray-900 dark:text-gray-100 mb-1 text-sm">{example.english}</p>
                    <p className="text-kikuyu-700 font-medium mb-1 text-sm">{example.kikuyu}</p>
                    {example.context && (
                      <p className="text-xs text-gray-600 italic">{example.context}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Grammar */}
          {card.grammatical_info && (
            <div>
              <h4 className="text-md font-semibold mb-3 text-gray-900 dark:text-gray-100">Grammar</h4>
              <div className="space-y-2 text-sm">
                {card.grammatical_info.part_of_speech && (
                  <div className="flex justify-between">
                    <span className="font-medium text-gray-600">Part of Speech:</span>
                    <span className="text-gray-900 dark:text-gray-100">{card.grammatical_info.part_of_speech}</span>
                  </div>
                )}
                {card.grammatical_info.noun_class && (
                  <div className="flex justify-between">
                    <span className="font-medium text-gray-600">Noun Class:</span>
                    <span className="text-gray-900 dark:text-gray-100">{card.grammatical_info.noun_class}</span>
                  </div>
                )}
                {card.grammatical_info.infinitive && (
                  <div className="flex justify-between">
                    <span className="font-medium text-gray-600">Infinitive:</span>
                    <span className="text-kikuyu-700 font-mono">{card.grammatical_info.infinitive}</span>
                  </div>
                )}
                {card.grammatical_info.verb_class && (
                  <div className="flex justify-between">
                    <span className="font-medium text-gray-600">Verb Class:</span>
                    <span className="text-gray-900 dark:text-gray-100">{card.grammatical_info.verb_class}</span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-wrap gap-3">
            <button
              onClick={isKnown ? onMarkUnknown : onMarkKnown}
              className={`btn ${isKnown ? 'btn-warning' : 'btn-success'} flex items-center text-sm`}
            >
              {isKnown ? '❌ Unknown' : '✅ Known'}
            </button>
            
            <button
              onClick={handleCopyCard}
              className="btn btn-secondary flex items-center relative text-sm"
              title="Copy translation with context"
            >
              📋 Copy
              {copyFeedback && (
                <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 px-2 py-1 bg-black text-white text-xs rounded whitespace-nowrap">
                  {copyFeedback}
                </span>
              )}
            </button>

            <ReportCard card={card} />
          </div>

          {/* Categories and Tags */}
          {((card.categories && card.categories.length > 0) || (card.tags && card.tags.length > 0) || card.subcategory) && (
            <div className="space-y-2">
              {/* Subcategory and Categories */}
              <div className="flex flex-wrap gap-1">
                {card.subcategory && (
                  <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-medium">
                    {card.subcategory.replace(/_/g, ' ')}
                  </span>
                )}
                {card.categories && card.categories.slice(0, 3).map((category, index) => (
                  <span
                    key={index}
                    className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
                  >
                    {category}
                  </span>
                ))}
              </div>
              
              {/* Tags */}
              {card.tags && card.tags.length > 0 && (
                <div className="flex flex-wrap gap-1">
                  {card.tags.slice(0, 5).map((tag, index) => (
                    <span
                      key={index}
                      className="px-2 py-1 bg-kikuyu-100 text-kikuyu-800 text-xs rounded-full"
                    >
                      #{tag}
                    </span>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}