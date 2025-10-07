'use client';

import { useState, useRef, useEffect } from 'react';
import { Flashcard } from '@/types/flashcard';

interface FlashCardProps {
  card: Flashcard;
  showCulturalNotes?: boolean;
  onFlip?: () => void;
  onNext?: () => void;
  onPrevious?: () => void;
  onMarkKnown?: () => void;
  onMarkUnknown?: () => void;
  isKnown?: boolean;
  className?: string;
}

export default function FlashCard({
  card,
  showCulturalNotes = true,
  onFlip,
  onNext,
  onPrevious,
  onMarkKnown,
  onMarkUnknown,
  isKnown = false,
  className = ''
}: FlashCardProps) {
  const [isFlipped, setIsFlipped] = useState(false);
  const [touchStart, setTouchStart] = useState<{ x: number; y: number } | null>(null);
  const [copyFeedback, setCopyFeedback] = useState<string | null>(null);
  const cardRef = useRef<HTMLDivElement>(null);

  const handleFlip = () => {
    setIsFlipped(!isFlipped);
    onFlip?.();
  };

  const handleCopyCard = async () => {
    try {
      let copyText = `${card.english} → ${card.kikuyu}`;
      
      // Add card ID for reference (prioritize string ID from curated content)
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
        copyText += `\nExample: ${firstExample.english} → ${firstExample.kikuyu}`;
      }
      
      // Add source info for curated content
      if (card.source?.origin) {
        copyText += `\nSource: ${card.source.origin}`;
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

  const handleTouchStart = (e: React.TouchEvent) => {
    const touch = e.touches[0];
    setTouchStart({ x: touch.clientX, y: touch.clientY });
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (!touchStart) return;

    const touch = e.changedTouches[0];
    const deltaX = touch.clientX - touchStart.x;
    const deltaY = touch.clientY - touchStart.y;
    const absDeltaX = Math.abs(deltaX);
    const absDeltaY = Math.abs(deltaY);

    // Swipe threshold
    const threshold = 50;

    // Horizontal swipe (left/right navigation)
    if (absDeltaX > threshold && absDeltaX > absDeltaY) {
      if (deltaX > 0) {
        onPrevious?.();
      } else {
        onNext?.();
      }
    }
    // Vertical swipe or tap (flip card)
    else if (absDeltaY < threshold && absDeltaX < threshold) {
      handleFlip();
    }

    setTouchStart(null);
  };

  // Keyboard navigation
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      switch (e.key) {
        case ' ':
        case 'Enter':
          e.preventDefault();
          handleFlip();
          break;
        case 'ArrowLeft':
          e.preventDefault();
          onPrevious?.();
          break;
        case 'ArrowRight':
          e.preventDefault();
          onNext?.();
          break;
        case 'k':
        case 'K':
          e.preventDefault();
          if (isKnown) {
            onMarkUnknown?.();
          } else {
            onMarkKnown?.();
          }
          break;
        case 'c':
        case 'C':
          e.preventDefault();
          handleCopyCard();
          break;
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [isFlipped, isKnown, onFlip, onNext, onPrevious, onMarkKnown, onMarkUnknown, handleCopyCard]);

  return (
    <div className={`w-full max-w-2xl mx-auto ${className}`}>
      {/* Card Status Indicator */}
      {isKnown && (
        <div className="flex justify-center mb-4">
          <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
            ✓ Known Card
          </span>
        </div>
      )}

      {/* Main Flashcard */}
      <div
        ref={cardRef}
        className={`flashcard ${isFlipped ? 'flipped' : ''} mb-6`}
        onClick={handleFlip}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
        role="button"
        tabIndex={0}
        aria-label="Flashcard - click or press space to flip"
      >
        <div className="flashcard-inner">
          {/* Front Side (English) */}
          <div className="flashcard-front">
            <div className="flex flex-col h-full justify-between">
              <div className="flex justify-between items-start mb-4">
                <span className="px-3 py-1 bg-blue-100 text-blue-800 text-sm rounded-full font-medium">
                  English
                </span>
                <span className={`px-3 py-1 text-sm rounded-full font-medium ${
                  card.difficulty === 'beginner' ? 'bg-green-100 text-green-800' :
                  card.difficulty === 'intermediate' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-red-100 text-red-800'
                }`}>
                  {card.difficulty}
                </span>
              </div>
              
              <div className="flex-1 flex items-center justify-center">
                <h2 className="text-2xl md:text-3xl font-bold text-gray-900 text-center leading-relaxed">
                  {card.english}
                </h2>
              </div>
              
              <div className="text-center text-gray-500 text-sm">
                Tap to reveal Kikuyu
              </div>
            </div>
          </div>

          {/* Back Side (Kikuyu) */}
          <div className="flashcard-back">
            <div className="flex flex-col h-full justify-between">
              <div className="flex justify-between items-start mb-4">
                <span className="px-3 py-1 bg-kikuyu-100 text-kikuyu-800 text-sm rounded-full font-medium">
                  Kikuyu
                </span>
                <span className="px-3 py-1 bg-gray-100 text-gray-800 text-sm rounded-full font-medium">
                  Score: {(card.quality_score || card.quality?.confidence_score || 0).toFixed(1)}
                </span>
              </div>
              
              <div className="flex-1 flex items-center justify-center">
                <h2 className="text-2xl md:text-3xl font-bold text-kikuyu-900 text-center leading-relaxed">
                  {card.kikuyu}
                </h2>
              </div>
              
              {/* Pronunciation (if available) */}
              {card.pronunciation && (
                <div className="text-center mb-3">
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

              {/* Context and Cultural Notes */}
              {(card.context || (showCulturalNotes && card.cultural_notes)) && (
                <div className="border-t border-kikuyu-200 pt-4 space-y-2">
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
          </div>
        </div>
      </div>

      {/* Examples Section */}
      {isFlipped && card.examples && card.examples.length > 0 && (
        <div className="card mb-6">
          <h3 className="text-lg font-semibold mb-3 text-gray-900">Usage Examples</h3>
          <div className="space-y-3">
            {card.examples.slice(0, 2).map((example, index) => (
              <div key={index} className="p-3 bg-gray-50 rounded-lg">
                <p className="font-medium text-gray-900 mb-1">{example.english}</p>
                <p className="text-kikuyu-700 font-medium mb-1">{example.kikuyu}</p>
                {example.context && (
                  <p className="text-xs text-gray-600 italic">{example.context}</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Grammatical Information */}
      {isFlipped && card.grammatical_info && (
        <div className="card mb-6">
          <h3 className="text-lg font-semibold mb-3 text-gray-900">Grammar</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            {card.grammatical_info.part_of_speech && (
              <div>
                <span className="font-medium text-gray-600">Part of Speech:</span>
                <span className="ml-2 text-gray-900">{card.grammatical_info.part_of_speech}</span>
              </div>
            )}
            {card.grammatical_info.noun_class && (
              <div>
                <span className="font-medium text-gray-600">Noun Class:</span>
                <span className="ml-2 text-gray-900">{card.grammatical_info.noun_class}</span>
              </div>
            )}
            {card.grammatical_info.infinitive && (
              <div>
                <span className="font-medium text-gray-600">Infinitive:</span>
                <span className="ml-2 text-kikuyu-700 font-mono">{card.grammatical_info.infinitive}</span>
              </div>
            )}
            {card.grammatical_info.verb_class && (
              <div>
                <span className="font-medium text-gray-600">Verb Class:</span>
                <span className="ml-2 text-gray-900">{card.grammatical_info.verb_class}</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Sub-translations (Morphological Analysis) */}
      {isFlipped && card.has_sub_translations && card.sub_translations && (
        <div className="card mb-6">
          <h3 className="text-lg font-semibold mb-3 text-gray-900">Word Breakdown</h3>
          <div className="space-y-2">
            {card.sub_translations.map((sub, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex-1">
                  <span className="font-mono text-kikuyu-700 font-medium">{sub.target}</span>
                  <span className="mx-2 text-gray-400">→</span>
                  <span className="text-gray-700">{sub.source}</span>
                </div>
                <span className="text-xs text-gray-500 ml-4">{sub.context}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Navigation and Action Buttons */}
      <div className="flex flex-col space-y-4">
        {/* Navigation */}
        <div className="flex justify-between items-center">
          <button
            onClick={onPrevious}
            className="btn btn-secondary flex items-center"
            disabled={!onPrevious}
          >
            ← Previous
          </button>
          
          <button
            onClick={handleFlip}
            className="btn btn-primary"
          >
            {isFlipped ? 'Show English' : 'Show Kikuyu'}
          </button>
          
          <button
            onClick={onNext}
            className="btn btn-secondary flex items-center"
            disabled={!onNext}
          >
            Next →
          </button>
        </div>

        {/* Knowledge Actions */}
        <div className="flex flex-wrap justify-center gap-4">
          <button
            onClick={isKnown ? onMarkUnknown : onMarkKnown}
            className={`btn ${isKnown ? 'btn-warning' : 'btn-success'} flex items-center`}
          >
            {isKnown ? '❌ Mark as Unknown' : '✅ Mark as Known'}
          </button>
          
          <button
            onClick={handleCopyCard}
            className="btn btn-secondary flex items-center relative"
            title="Copy translation with context"
          >
            📋 Copy Translation
            {copyFeedback && (
              <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 px-2 py-1 bg-black text-white text-xs rounded whitespace-nowrap">
                {copyFeedback}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Categories and Tags */}
      {((card.categories && card.categories.length > 0) || (card.tags && card.tags.length > 0) || card.subcategory) && (
        <div className="mt-6 space-y-2">
          {/* Legacy categories or new subcategory */}
          <div className="flex flex-wrap gap-2 justify-center">
            {card.subcategory && (
              <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-medium">
                {card.subcategory.replace(/_/g, ' ')}
              </span>
            )}
            {card.categories && card.categories.slice(0, 2).map((category, index) => (
              <span
                key={index}
                className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
              >
                {category}
              </span>
            ))}
            {card.categories && card.categories.length > 2 && (
              <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                +{card.categories.length - 2} more
              </span>
            )}
          </div>
          
          {/* Tags from curated content */}
          {card.tags && card.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 justify-center">
              {card.tags.slice(0, 4).map((tag, index) => (
                <span
                  key={index}
                  className="px-2 py-1 bg-kikuyu-100 text-kikuyu-800 text-xs rounded-full"
                >
                  #{tag}
                </span>
              ))}
              {card.tags.length > 4 && (
                <span className="px-2 py-1 bg-kikuyu-100 text-kikuyu-800 text-xs rounded-full">
                  +{card.tags.length - 4}
                </span>
              )}
            </div>
          )}
        </div>
      )}

      {/* Keyboard Shortcuts Help */}
      <div className="mt-8 text-center text-xs text-gray-500">
        <p>Keyboard shortcuts: Space/Enter (flip) • ← → (navigate) • K (mark known) • C (copy)</p>
        <p>Touch: Tap (flip) • Swipe left/right (navigate)</p>
      </div>
    </div>
  );
}