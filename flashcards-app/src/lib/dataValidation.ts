import { Flashcard } from '@/types/flashcard';

export interface ValidationResult {
  isValid: boolean;
  issues: string[];
  severity: 'low' | 'medium' | 'high';
}

export class DataValidator {
  /**
   * Validate a flashcard for basic quality requirements
   */
  static validateCard(card: Flashcard): ValidationResult {
    const issues: string[] = [];
    let severity: 'low' | 'medium' | 'high' = 'low';

    // Check for empty or invalid translations
    if (!card.english || card.english.trim().length === 0) {
      issues.push('Missing English translation');
      severity = 'high';
    }

    if (!card.kikuyu || card.kikuyu.trim().length === 0) {
      issues.push('Missing Kikuyu translation');
      severity = 'high';
    }

    // Check for incomplete or corrupted data
    if (card.english && card.english.includes('undefined') || card.english.includes('null')) {
      issues.push('Corrupted English content');
      severity = 'high';
    }

    if (card.kikuyu && card.kikuyu.includes('undefined') || card.kikuyu.includes('null')) {
      issues.push('Corrupted Kikuyu content');
      severity = 'high';
    }

    // Check for suspiciously short content
    if (card.english && card.english.trim().length < 2) {
      issues.push('English translation too short');
      severity = 'medium';
    }

    if (card.kikuyu && card.kikuyu.trim().length < 2) {
      issues.push('Kikuyu translation too short');
      severity = 'medium';
    }

    // Check for formatting artifacts
    const formatArtifacts = [', p. ', 'Rep. ', '(Also in', 'derived from', 'Morphologically'];
    const hasArtifacts = formatArtifacts.some(artifact => 
      card.english?.includes(artifact) || card.kikuyu?.includes(artifact)
    );

    if (hasArtifacts) {
      issues.push('Contains formatting artifacts from source data');
      severity = severity === 'high' ? 'high' : 'medium';
    }

    // Check for incomplete sentences or fragments
    if (card.english && /^[,.]/.test(card.english.trim())) {
      issues.push('English starts with punctuation (likely fragment)');
      severity = severity === 'high' ? 'high' : 'medium';
    }

    if (card.kikuyu && /^[,.]/.test(card.kikuyu.trim())) {
      issues.push('Kikuyu starts with punctuation (likely fragment)');
      severity = severity === 'high' ? 'high' : 'medium';
    }

    // Check for quality score issues
    const qualityScore = card.quality_score || card.quality?.confidence_score || 0;
    if (qualityScore < 2.0) {
      issues.push('Very low quality score');
      severity = severity === 'high' ? 'high' : 'medium';
    }

    return {
      isValid: issues.length === 0,
      issues,
      severity
    };
  }

  /**
   * Filter cards based on validation criteria
   */
  static filterValidCards(cards: Flashcard[], strictMode: boolean = false): {
    validCards: Flashcard[];
    invalidCards: Flashcard[];
    totalIssues: number;
  } {
    const validCards: Flashcard[] = [];
    const invalidCards: Flashcard[] = [];
    let totalIssues = 0;

    cards.forEach(card => {
      const validation = this.validateCard(card);
      totalIssues += validation.issues.length;

      if (validation.isValid) {
        validCards.push(card);
      } else {
        // In strict mode, exclude all cards with issues
        // In normal mode, only exclude high severity issues
        if (strictMode || validation.severity === 'high') {
          invalidCards.push({
            ...card,
            _validationIssues: validation.issues,
            _validationSeverity: validation.severity
          });
        } else {
          // Include with warnings
          validCards.push({
            ...card,
            _validationIssues: validation.issues,
            _validationSeverity: validation.severity
          });
        }
      }
    });

    return { validCards, invalidCards, totalIssues };
  }

  /**
   * Get data quality statistics
   */
  static getQualityStats(cards: Flashcard[]): {
    total: number;
    valid: number;
    lowSeverity: number;
    mediumSeverity: number;
    highSeverity: number;
    averageQuality: number;
  } {
    let lowSeverity = 0;
    let mediumSeverity = 0;
    let highSeverity = 0;
    let totalQuality = 0;

    cards.forEach(card => {
      const validation = this.validateCard(card);
      const quality = card.quality_score || card.quality?.confidence_score || 0;
      totalQuality += quality;

      if (!validation.isValid) {
        switch (validation.severity) {
          case 'low': lowSeverity++; break;
          case 'medium': mediumSeverity++; break;
          case 'high': highSeverity++; break;
        }
      }
    });

    return {
      total: cards.length,
      valid: cards.length - lowSeverity - mediumSeverity - highSeverity,
      lowSeverity,
      mediumSeverity,
      highSeverity,
      averageQuality: cards.length > 0 ? totalQuality / cards.length : 0
    };
  }
}