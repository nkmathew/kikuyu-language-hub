'use client';

interface ModeToggleProps {
  mode: 'study' | 'flashcard';
  onModeChange: (mode: 'study' | 'flashcard') => void;
  className?: string;
}

export default function ModeToggle({ mode, onModeChange, className = '' }: ModeToggleProps) {
  return (
    <div className={`inline-flex bg-gray-100 rounded-lg p-1 ${className}`}>
      <button
        onClick={() => onModeChange('study')}
        className={`px-4 py-2 text-sm font-medium rounded-md transition-all duration-200 ${
          mode === 'study'
            ? 'bg-white text-blue-600 shadow-sm'
            : 'text-gray-600 hover:text-blue-600'
        }`}
      >
        📚 Study Mode
      </button>
      <button
        onClick={() => onModeChange('flashcard')}
        className={`px-4 py-2 text-sm font-medium rounded-md transition-all duration-200 ${
          mode === 'flashcard'
            ? 'bg-white text-blue-600 shadow-sm'
            : 'text-gray-600 hover:text-blue-600'
        }`}
      >
        ⚡ Flashcard Mode
      </button>
    </div>
  );
}