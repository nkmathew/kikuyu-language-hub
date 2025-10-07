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
  const cardRef = useRef<HTMLDivElement>(null);

  const handleFlip = () => {
    setIsFlipped(!isFlipped);
    onFlip?.();
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
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [isFlipped, isKnown, onFlip, onNext, onPrevious, onMarkKnown, onMarkUnknown]);

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
                  Score: {card.quality_score.toFixed(1)}
                </span>
              </div>
              
              <div className="flex-1 flex items-center justify-center">
                <h2 className="text-2xl md:text-3xl font-bold text-kikuyu-900 text-center leading-relaxed">
                  {card.kikuyu}
                </h2>
              </div>
              
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
        <div className="flex justify-center space-x-4">
          <button
            onClick={isKnown ? onMarkUnknown : onMarkKnown}
            className={`btn ${isKnown ? 'btn-warning' : 'btn-success'} flex items-center`}
          >
            {isKnown ? '❌ Mark as Unknown' : '✅ Mark as Known'}
          </button>
        </div>
      </div>

      {/* Categories */}
      {card.categories.length > 0 && (
        <div className="mt-6 flex flex-wrap gap-2 justify-center">
          {card.categories.slice(0, 3).map((category, index) => (
            <span
              key={index}
              className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
            >
              {category}
            </span>
          ))}
          {card.categories.length > 3 && (
            <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
              +{card.categories.length - 3} more
            </span>
          )}
        </div>
      )}

      {/* Keyboard Shortcuts Help */}
      <div className="mt-8 text-center text-xs text-gray-500">
        <p>Keyboard shortcuts: Space/Enter (flip) • ← → (navigate) • K (mark known)</p>
        <p>Touch: Tap (flip) • Swipe left/right (navigate)</p>
      </div>
    </div>
  );
}