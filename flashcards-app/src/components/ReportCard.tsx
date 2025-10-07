'use client';

import { useState } from 'react';
import { Flashcard } from '@/types/flashcard';

interface ReportCardProps {
  card: Flashcard;
  onSubmit?: (report: CardReport) => void;
  className?: string;
}

export interface CardReport {
  cardId: string | number;
  cardEnglish: string;
  cardKikuyu: string;
  issueType: 'incorrect_translation' | 'missing_content' | 'cultural_issue' | 'formatting_error' | 'other';
  description: string;
  suggestedFix?: string;
  reporterEmail?: string;
  timestamp: string;
}

export default function ReportCard({ card, onSubmit, className = '' }: ReportCardProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [issueType, setIssueType] = useState<CardReport['issueType']>('incorrect_translation');
  const [description, setDescription] = useState('');
  const [suggestedFix, setSuggestedFix] = useState('');
  const [reporterEmail, setReporterEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const issueTypes = [
    { value: 'incorrect_translation', label: 'Incorrect Translation', description: 'The translation is wrong or misleading' },
    { value: 'missing_content', label: 'Missing Content', description: 'Important information is missing (pronunciation, context, etc.)' },
    { value: 'cultural_issue', label: 'Cultural Issue', description: 'Cultural context or notes are inappropriate or missing' },
    { value: 'formatting_error', label: 'Formatting Error', description: 'Text formatting, artifacts, or display issues' },
    { value: 'other', label: 'Other Issue', description: 'Something else needs attention' }
  ] as const;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!description.trim()) return;

    setIsSubmitting(true);

    const report: CardReport = {
      cardId: card.id,
      cardEnglish: card.english,
      cardKikuyu: card.kikuyu,
      issueType,
      description: description.trim(),
      suggestedFix: suggestedFix.trim() || undefined,
      reporterEmail: reporterEmail.trim() || undefined,
      timestamp: new Date().toISOString()
    };

    try {
      // Store report locally for now (could be sent to API later)
      const existingReports = JSON.parse(localStorage.getItem('cardReports') || '[]');
      existingReports.push(report);
      localStorage.setItem('cardReports', JSON.stringify(existingReports));

      // Call callback if provided
      onSubmit?.(report);

      setSubmitted(true);
      setTimeout(() => {
        setIsOpen(false);
        setSubmitted(false);
        setDescription('');
        setSuggestedFix('');
        setReporterEmail('');
        setIssueType('incorrect_translation');
      }, 2000);

    } catch (error) {
      console.error('Failed to submit report:', error);
      alert('Failed to submit report. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className={`fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 ${className}`}>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md mx-4">
          <div className="text-center">
            <div className="text-green-600 text-4xl mb-4">✓</div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">Report Submitted!</h3>
            <p className="text-gray-600 dark:text-gray-400">Thank you for helping improve the Kikuyu learning experience.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Report Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="text-xs text-gray-500 dark:text-gray-400"
        title="Report an issue with this card"
      >
        🚩 Report Issue
      </button>

      {/* Report Modal */}
      {isOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              {/* Header */}
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">Report Card Issue</h3>
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    <p><strong>English:</strong> {card.english}</p>
                    <p><strong>Kikuyu:</strong> {card.kikuyu}</p>
                    <p><strong>Card ID:</strong> {typeof card.id === 'string' ? card.id : `#${card.id}`}</p>
                  </div>
                </div>
                <button
                  onClick={() => setIsOpen(false)}
                  className="text-gray-400 dark:text-gray-500"
                >
                  ✕
                </button>
              </div>

              {/* Form */}
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Issue Type */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    What's the issue?
                  </label>
                  <div className="space-y-2">
                    {issueTypes.map((type) => (
                      <label key={type.value} className="flex items-start space-x-3 cursor-pointer">
                        <input
                          type="radio"
                          name="issueType"
                          value={type.value}
                          checked={issueType === type.value}
                          onChange={(e) => setIssueType(e.target.value as CardReport['issueType'])}
                          className="mt-1"
                        />
                        <div>
                          <div className="font-medium text-gray-900 dark:text-gray-100">{type.label}</div>
                          <div className="text-sm text-gray-600 dark:text-gray-400">{type.description}</div>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Description */}
                <div>
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Describe the issue *
                  </label>
                  <textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows={3}
                    placeholder="Please explain what's wrong and how it should be corrected..."
                    required
                  />
                </div>

                {/* Suggested Fix */}
                <div>
                  <label htmlFor="suggestedFix" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Suggested correction (optional)
                  </label>
                  <input
                    id="suggestedFix"
                    type="text"
                    value={suggestedFix}
                    onChange={(e) => setSuggestedFix(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="What should the correct translation be?"
                  />
                </div>

                {/* Email */}
                <div>
                  <label htmlFor="reporterEmail" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Email (optional)
                  </label>
                  <input
                    id="reporterEmail"
                    type="email"
                    value={reporterEmail}
                    onChange={(e) => setReporterEmail(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="your.email@example.com (for follow-up questions)"
                  />
                </div>

                {/* Actions */}
                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setIsOpen(false)}
                    className="btn btn-secondary"
                    disabled={isSubmitting}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={isSubmitting || !description.trim()}
                  >
                    {isSubmitting ? 'Submitting...' : 'Submit Report'}
                  </button>
                </div>
              </form>

              {/* Info */}
              <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900 dark:bg-opacity-20 rounded-md">
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  <strong>Privacy:</strong> Reports are stored locally and help improve content quality. 
                  We appreciate community contributions to make Kikuyu learning materials better for everyone.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}