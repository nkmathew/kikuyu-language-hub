import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Animated,
  Dimensions,
  ActivityIndicator,
  Alert,
  useColorScheme,
} from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { Flashcard, StudySession } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';
import { storageService } from '../lib/storage';
import { spacedRepetitionService } from '../lib/spacedRepetition';

type FlashcardScreenNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Flashcard'>;
type FlashcardScreenRouteProp = RouteProp<RootStackParamList, 'Flashcard'>;

interface Props {
  navigation: FlashcardScreenNavigationProp;
  route: FlashcardScreenRouteProp;
}

const { width } = Dimensions.get('window');
const CARD_WIDTH = width - 48;

export default function FlashcardScreen({ navigation, route }: Props) {
  const { category, difficulties } = route.params;
  const [cards, setCards] = useState<Flashcard[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [sessionStartTime] = useState(new Date().toISOString());
  const [correctAnswers, setCorrectAnswers] = useState(0);
  const flipAnimation = useRef(new Animated.Value(0)).current;
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    loadCards();
  }, [category, difficulties]);

  const loadCards = async () => {
    try {
      const categoryData = await dataLoader.loadCategory(category);
      const selectedCards = dataLoader.getCardsByDifficulty(categoryData, difficulties);
      const shuffledCards = dataLoader.shuffleCards(selectedCards);
      setCards(shuffledCards);
    } catch (error) {
      console.error('Error loading cards:', error);
    } finally {
      setLoading(false);
    }
  };

  const flipCard = () => {
    Animated.spring(flipAnimation, {
      toValue: isFlipped ? 0 : 180,
      friction: 8,
      tension: 10,
      useNativeDriver: true,
    }).start();
    setIsFlipped(!isFlipped);
  };

  const handleRating = async (rating: 'easy' | 'medium' | 'hard') => {
    const currentCard = cards[currentIndex];

    // Update progress with spaced repetition
    const previousProgress = await storageService.getCardProgress(currentCard.id);
    const newProgress = spacedRepetitionService.calculateNextReview(
      currentCard.id,
      previousProgress,
      rating
    );
    await storageService.saveCardProgress(currentCard.id, newProgress);

    // Track correct answers
    if (rating === 'easy' || rating === 'medium') {
      setCorrectAnswers(prev => prev + 1);
    }

    // Increment cards studied
    await storageService.incrementCardsStudied();

    // Update streak
    await storageService.updateStreak();

    // Move to next card
    if (currentIndex < cards.length - 1) {
      nextCard();
    } else {
      await finishSession();
    }
  };

  const nextCard = () => {
    setCurrentIndex(currentIndex + 1);
    setIsFlipped(false);
    flipAnimation.setValue(0);
  };

  const previousCard = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
      setIsFlipped(false);
      flipAnimation.setValue(0);
    }
  };

  const finishSession = async () => {
    const endTime = new Date().toISOString();
    const startTime = new Date(sessionStartTime);
    const endTimeDate = new Date(endTime);
    const durationMinutes = Math.round((endTimeDate.getTime() - startTime.getTime()) / (1000 * 60));

    // Save session
    const session: StudySession = {
      category,
      difficulty: difficulties,
      mode: 'flashcards',
      cardsStudied: cards.length,
      correctAnswers,
      startTime: sessionStartTime,
      endTime,
    };

    await storageService.saveSession(session);
    await storageService.incrementSessionsCompleted();
    await storageService.addStudyTime(durationMinutes);

    // Show completion alert
    Alert.alert(
      'Session Complete! 🎉',
      `You studied ${cards.length} cards with ${Math.round((correctAnswers / cards.length) * 100)}% accuracy!`,
      [
        {
          text: 'View Progress',
          onPress: () => navigation.navigate('Home'),
        },
        {
          text: 'Study More',
          onPress: () => navigation.goBack(),
        },
      ]
    );
  };

  const frontInterpolate = flipAnimation.interpolate({
    inputRange: [0, 180],
    outputRange: ['0deg', '180deg'],
  });

  const backInterpolate = flipAnimation.interpolate({
    inputRange: [0, 180],
    outputRange: ['180deg', '360deg'],
  });

  const frontOpacity = flipAnimation.interpolate({
    inputRange: [0, 90, 180],
    outputRange: [1, 0, 0],
  });

  const backOpacity = flipAnimation.interpolate({
    inputRange: [0, 90, 180],
    outputRange: [0, 0, 1],
  });

  if (loading) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    );
  }

  if (cards.length === 0) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <Text style={[styles.emptyText, isDark && styles.darkText]}>No cards available</Text>
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <Text style={styles.backButtonText}>Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const currentCard = cards[currentIndex];

  return (
    <View style={[styles.container, isDark && styles.darkBg]}>
      <View style={styles.progressContainer}>
        <Text style={[styles.progressText, isDark && styles.darkTextSecondary]}>
          Card {currentIndex + 1} of {cards.length}
        </Text>
        <View style={styles.progressBar}>
          <View
            style={[
              styles.progressFill,
              { width: `${((currentIndex + 1) / cards.length) * 100}%` },
            ]}
          />
        </View>
        <Text style={styles.accuracyText}>
          Accuracy: {Math.round((correctAnswers / (currentIndex + 1)) * 100)}%
        </Text>
      </View>

      <View style={styles.cardContainer}>
        <TouchableOpacity onPress={flipCard} activeOpacity={0.9} style={styles.cardTouchable}>
          <Animated.View
            style={[
              styles.card,
              isDark && styles.darkCard,
              {
                transform: [{ rotateY: frontInterpolate }],
                opacity: frontOpacity,
              },
            ]}
          >
            <Text style={[styles.cardLabel, isDark && styles.darkTextSecondary]}>Kikuyu</Text>
            <Text style={[styles.cardText, isDark && styles.darkText]}>{currentCard.kikuyu}</Text>
            {currentCard.difficulty && (
              <View style={[styles.difficultyBadge, styles[`badge_${currentCard.difficulty}`]]}>
                <Text style={styles.difficultyText}>{currentCard.difficulty}</Text>
              </View>
            )}
            <Text style={[styles.tapHint, isDark && styles.darkTextMuted]}>Tap to see translation</Text>
          </Animated.View>

          <Animated.View
            style={[
              styles.card,
              styles.cardBackPositioned,
              isDark && styles.darkCard,
              {
                transform: [{ rotateY: backInterpolate }],
                opacity: backOpacity,
              },
            ]}
            pointerEvents={isFlipped ? 'auto' : 'none'}
          >
            <Text style={[styles.cardLabel, isDark && styles.darkTextSecondary]}>English</Text>
            <Text style={[styles.cardText, isDark && styles.darkText]}>{currentCard.english}</Text>
            {currentCard.notes && (
              <View style={[styles.notesContainer, isDark && styles.darkNotesContainer]}>
                <Text style={[styles.notesLabel, isDark && styles.darkTextSecondary]}>Notes:</Text>
                <Text style={[styles.notesText, isDark && styles.darkText]}>{currentCard.notes}</Text>
              </View>
            )}
            <Text style={[styles.tapHint, isDark && styles.darkTextMuted]}>How well did you know this?</Text>
          </Animated.View>
        </TouchableOpacity>
      </View>

      {isFlipped && (
        <View style={styles.ratingContainer}>
          <TouchableOpacity
            style={[styles.ratingButton, styles.hardButton, isDark && styles.darkRatingHard]}
            onPress={() => handleRating('hard')}
          >
            <Text style={styles.ratingButtonText}>😰 Hard</Text>
            <Text style={styles.ratingSubtext}>{'< 1 day'}</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.ratingButton, styles.mediumButton, isDark && styles.darkRatingMedium]}
            onPress={() => handleRating('medium')}
          >
            <Text style={styles.ratingButtonText}>🤔 Good</Text>
            <Text style={styles.ratingSubtext}>1 day</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.ratingButton, styles.easyButton, isDark && styles.darkRatingEasy]}
            onPress={() => handleRating('easy')}
          >
            <Text style={styles.ratingButtonText}>😊 Easy</Text>
            <Text style={styles.ratingSubtext}>4 days</Text>
          </TouchableOpacity>
        </View>
      )}

      {!isFlipped && (
        <View style={styles.navigationContainer}>
          <TouchableOpacity
            style={[styles.navButton, currentIndex === 0 && (isDark ? styles.navButtonDisabledDark : styles.navButtonDisabled)]}
            onPress={previousCard}
            disabled={currentIndex === 0}
          >
            <Text style={styles.navButtonText}>← Previous</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.navButton, styles.navButtonSecondary]}
            onPress={() => navigation.goBack()}
          >
            <Text style={styles.navButtonText}>Exit</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.navButton, currentIndex === cards.length - 1 && (isDark ? styles.navButtonDisabledDark : styles.navButtonDisabled)]}
            onPress={nextCard}
            disabled={currentIndex === cards.length - 1}
          >
            <Text style={styles.navButtonText}>Next →</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
    padding: 16,
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
  },
  emptyText: {
    fontSize: 18,
    color: '#6b7280',
    marginBottom: 16,
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
  darkTextMuted: {
    color: '#6b7280',
  },
  backButton: {
    backgroundColor: '#3b82f6',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  backButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  progressContainer: {
    marginBottom: 20,
  },
  progressText: {
    fontSize: 16,
    color: '#6b7280',
    marginBottom: 8,
    textAlign: 'center',
    fontWeight: '600',
  },
  progressBar: {
    height: 6,
    backgroundColor: '#e5e7eb',
    borderRadius: 3,
    overflow: 'hidden',
    marginBottom: 8,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#3b82f6',
  },
  accuracyText: {
    fontSize: 14,
    color: '#10b981',
    textAlign: 'center',
    fontWeight: '600',
  },
  cardContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 20,
  },
  cardTouchable: {
    width: CARD_WIDTH,
    height: 400,
  },
  card: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 24,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
    backfaceVisibility: 'hidden',
  },
  darkCard: {
    backgroundColor: '#1f2937',
  },
  cardBackPositioned: {
    position: 'absolute',
  },
  cardLabel: {
    fontSize: 14,
    color: '#9ca3af',
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: 16,
  },
  cardText: {
    fontSize: 22,
    color: '#111827',
    fontWeight: '600',
    textAlign: 'center',
    lineHeight: 32,
    marginBottom: 16,
  },
  difficultyBadge: {
    paddingHorizontal: 16,
    paddingVertical: 6,
    borderRadius: 16,
    marginTop: 12,
  },
  badge_beginner: {
    backgroundColor: '#dcfce7',
  },
  badge_intermediate: {
    backgroundColor: '#fef3c7',
  },
  badge_advanced: {
    backgroundColor: '#fee2e2',
  },
  difficultyText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#374151',
    textTransform: 'capitalize',
  },
  notesContainer: {
    marginTop: 16,
    padding: 16,
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    width: '100%',
  },
  darkNotesContainer: {
    backgroundColor: '#374151',
  },
  notesLabel: {
    fontSize: 12,
    color: '#6b7280',
    fontWeight: '700',
    marginBottom: 6,
    textTransform: 'uppercase',
  },
  notesText: {
    fontSize: 14,
    color: '#374151',
    lineHeight: 22,
  },
  tapHint: {
    fontSize: 13,
    color: '#9ca3af',
    fontStyle: 'italic',
    marginTop: 16,
  },
  ratingContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 20,
  },
  ratingButton: {
    flex: 1,
    paddingVertical: 18,
    borderRadius: 16,
    alignItems: 'center',
    borderWidth: 3,
  },
  hardButton: {
    backgroundColor: '#fef2f2',
    borderColor: '#ef4444',
  },
  mediumButton: {
    backgroundColor: '#fffbeb',
    borderColor: '#f59e0b',
  },
  easyButton: {
    backgroundColor: '#f0fdf4',
    borderColor: '#22c55e',
  },
  ratingButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 4,
  },
  ratingSubtext: {
    fontSize: 12,
    color: '#6b7280',
    fontWeight: '600',
  },
  navigationContainer: {
    flexDirection: 'row',
    gap: 12,
  },
  navButton: {
    flex: 1,
    backgroundColor: '#3b82f6',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  navButtonSecondary: {
    backgroundColor: '#6b7280',
  },
  navButtonDisabled: {
    backgroundColor: '#d1d5db',
  },
  navButtonDisabledDark: {
    backgroundColor: '#374151',
  },
  darkRatingHard: {
    backgroundColor: '#7f1d1d',
    borderColor: '#ef4444',
  },
  darkRatingMedium: {
    backgroundColor: '#78350f',
    borderColor: '#f59e0b',
  },
  darkRatingEasy: {
    backgroundColor: '#064e3b',
    borderColor: '#22c55e',
  },
  navButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
});
