import { StudyProgress } from '../types/flashcard';

/**
 * SuperMemo SM-2 Algorithm Implementation
 * Based on: https://www.supermemo.com/en/archives1990-2015/english/ol/sm2
 */

export class SpacedRepetitionService {
  /**
   * Calculate next review date based on user's rating
   * @param cardId - Unique identifier for the flashcard
   * @param previousProgress - Previous progress data (null for new cards)
   * @param rating - User's rating: 'hard' (0-2), 'medium' (3), 'easy' (4-5)
   * @returns Updated progress data
   */
  calculateNextReview(
    cardId: string,
    previousProgress: StudyProgress | null,
    rating: 'easy' | 'medium' | 'hard'
  ): StudyProgress {
    const now = new Date().toISOString();

    // Initialize for new card
    if (!previousProgress) {
      return {
        cardId,
        difficulty: rating,
        lastReviewed: now,
        nextReview: this.getInitialReviewDate(rating),
        repetitions: 1,
        interval: this.getInitialInterval(rating),
        easeFactor: 2.5,
      };
    }

    // Convert rating to quality (0-5 scale)
    const quality = this.ratingToQuality(rating);

    // Calculate new ease factor
    let newEaseFactor = previousProgress.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

    // Ensure ease factor doesn't go below 1.3
    if (newEaseFactor < 1.3) {
      newEaseFactor = 1.3;
    }

    let newInterval: number;
    let newRepetitions: number;

    if (quality < 3) {
      // Failed - restart with interval of 1 day
      newInterval = 1;
      newRepetitions = 0;
    } else {
      // Passed
      if (previousProgress.repetitions === 0) {
        newInterval = 1;
      } else if (previousProgress.repetitions === 1) {
        newInterval = 6;
      } else {
        newInterval = Math.round(previousProgress.interval * newEaseFactor);
      }
      newRepetitions = previousProgress.repetitions + 1;
    }

    const nextReviewDate = new Date();
    nextReviewDate.setDate(nextReviewDate.getDate() + newInterval);

    return {
      cardId,
      difficulty: rating,
      lastReviewed: now,
      nextReview: nextReviewDate.toISOString(),
      repetitions: newRepetitions,
      interval: newInterval,
      easeFactor: newEaseFactor,
    };
  }

  /**
   * Check if a card is due for review
   */
  isDueForReview(progress: StudyProgress): boolean {
    const now = new Date();
    const nextReview = new Date(progress.nextReview);
    return now >= nextReview;
  }

  /**
   * Get cards due for review from progress data
   */
  getDueCards(allProgress: Record<string, StudyProgress>): string[] {
    return Object.values(allProgress)
      .filter(progress => this.isDueForReview(progress))
      .map(progress => progress.cardId);
  }

  /**
   * Get initial interval based on rating
   */
  private getInitialInterval(rating: 'easy' | 'medium' | 'hard'): number {
    switch (rating) {
      case 'easy':
        return 4;
      case 'medium':
        return 1;
      case 'hard':
        return 0.5;
    }
  }

  /**
   * Get initial review date
   */
  private getInitialReviewDate(rating: 'easy' | 'medium' | 'hard'): string {
    const date = new Date();
    const days = this.getInitialInterval(rating);
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }

  /**
   * Convert rating to quality (0-5 scale)
   */
  private ratingToQuality(rating: 'easy' | 'medium' | 'hard'): number {
    switch (rating) {
      case 'easy':
        return 5; // Perfect response
      case 'medium':
        return 3; // Correct response with hesitation
      case 'hard':
        return 1; // Incorrect response, but remembered on seeing answer
    }
  }

  /**
   * Get study statistics for a set of progress data
   */
  getStudyStats(allProgress: Record<string, StudyProgress>): {
    totalCards: number;
    dueToday: number;
    dueSoon: number; // Due within 3 days
    mastered: number; // Interval > 21 days
    learning: number; // Repetitions < 3
    averageEaseFactor: number;
  } {
    const progressArray = Object.values(allProgress);
    const now = new Date();
    const threeDaysFromNow = new Date();
    threeDaysFromNow.setDate(threeDaysFromNow.getDate() + 3);

    const dueToday = progressArray.filter(p => this.isDueForReview(p)).length;
    const dueSoon = progressArray.filter(p => {
      const nextReview = new Date(p.nextReview);
      return nextReview <= threeDaysFromNow && nextReview > now;
    }).length;
    const mastered = progressArray.filter(p => p.interval > 21).length;
    const learning = progressArray.filter(p => p.repetitions < 3).length;
    const averageEaseFactor = progressArray.length > 0
      ? progressArray.reduce((sum, p) => sum + p.easeFactor, 0) / progressArray.length
      : 2.5;

    return {
      totalCards: progressArray.length,
      dueToday,
      dueSoon,
      mastered,
      learning,
      averageEaseFactor: Math.round(averageEaseFactor * 100) / 100,
    };
  }

  /**
   * Get recommended study load for today
   */
  getRecommendedStudyLoad(allProgress: Record<string, StudyProgress>): {
    newCards: number;
    reviewCards: number;
    totalRecommended: number;
  } {
    const dueCards = this.getDueCards(allProgress);
    const progressedCards = Object.keys(allProgress);

    // Recommend 10 new cards per day
    const recommendedNew = 10;

    // Review all due cards, but cap at 50 per day
    const recommendedReview = Math.min(dueCards.length, 50);

    return {
      newCards: recommendedNew,
      reviewCards: recommendedReview,
      totalRecommended: recommendedNew + recommendedReview,
    };
  }
}

export const spacedRepetitionService = new SpacedRepetitionService();
