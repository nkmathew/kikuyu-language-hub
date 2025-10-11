'use client';

interface ModeToggleProps {
  mode: 'study' | 'flashcard';
  onModeChange: (mode: 'study' | 'flashcard') => void;
  className?: string;
}

export default function ModeToggle({ mode, onModeChange, className = '' }: ModeToggleProps) {
  return (
    <div className={`inline-flex bg-gray-100 dark:bg-gray-700 rounded-lg p-1 ${className}`}>
      <button
        onClick={() => onModeChange('study')}
        className={`px-4 py-2 text-sm font-medium rounded-md ${
          mode === 'study'
            ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow-sm'
            : 'text-gray-600 dark:text-gray-300'
        }`}
      >
        📚 Study Mode
      </button>
      <button
        onClick={() => onModeChange('flashcard')}
        className={`px-4 py-2 text-sm font-medium rounded-md ${
          mode === 'flashcard'
            ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow-sm'
            : 'text-gray-600 dark:text-gray-300'
        }`}
      >
        ⚡ Flashcard Mode
      </button>
    </div>
  );
}