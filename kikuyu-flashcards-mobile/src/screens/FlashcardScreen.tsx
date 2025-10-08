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
const CARD_WIDTH = width - 32;

export default function FlashcardScreen({ navigation, route }: Props) {
  const { category, difficulties } = route.params;
  const [cards, setCards] = useState<Flashcard[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [sessionStartTime] = useState(new Date().toISOString());
  const [correctAnswers, setCorrectAnswers] = useState(0);
  const flipAnimation = useRef(new Animated.Value(0)).current;

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
    Animated.timing(flipAnimation, {
      toValue: isFlipped ? 0 : 1,
      duration: 300,
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
    inputRange: [0, 1],
    outputRange: ['0deg', '180deg'],
  });

  const backInterpolate = flipAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: ['180deg', '360deg'],
  });

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#2563eb" />
      </View>
    );
  }

  if (cards.length === 0) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.emptyText}>No cards available</Text>
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <Text style={styles.backButtonText}>Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const currentCard = cards[currentIndex];

  return (
    <View style={styles.container}>
      <View style={styles.progressContainer}>
        <Text style={styles.progressText}>
          {currentIndex + 1} / {cards.length}
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
          Accuracy: {cards.length > 0 ? Math.round((correctAnswers / (currentIndex + 1)) * 100) : 0}%
        </Text>
      </View>

      <View style={styles.cardContainer}>
        <TouchableOpacity onPress={flipCard} activeOpacity={0.9}>
          <Animated.View
            style={[
              styles.card,
              styles.cardFront,
              { transform: [{ rotateY: frontInterpolate }] },
            ]}
          >
            <Text style={styles.cardLabel}>Kikuyu</Text>
            <Text style={styles.cardText}>{currentCard.kikuyu}</Text>
            {currentCard.difficulty && (
              <View style={[styles.difficultyBadge, styles[`badge_${currentCard.difficulty}`]]}>
                <Text style={styles.difficultyText}>{currentCard.difficulty}</Text>
              </View>
            )}
            <Text style={styles.tapHint}>Tap to flip</Text>
          </Animated.View>

          <Animated.View
            style={[
              styles.card,
              styles.cardBack,
              { transform: [{ rotateY: backInterpolate }] },
            ]}
          >
            <Text style={styles.cardLabel}>English</Text>
            <Text style={styles.cardText}>{currentCard.english}</Text>
            {currentCard.notes && (
              <View style={styles.notesContainer}>
                <Text style={styles.notesLabel}>Notes:</Text>
                <Text style={styles.notesText}>{currentCard.notes}</Text>
              </View>
            )}
            <Text style={styles.tapHint}>Rate your recall</Text>
          </Animated.View>
        </TouchableOpacity>
      </View>

      {isFlipped && (
        <View style={styles.ratingContainer}>
          <TouchableOpacity
            style={[styles.ratingButton, styles.hardButton]}
            onPress={() => handleRating('hard')}
          >
            <Text style={styles.ratingButtonText}>Hard</Text>
            <Text style={styles.ratingSubtext}>Again soon</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.ratingButton, styles.mediumButton]}
            onPress={() => handleRating('medium')}
          >
            <Text style={styles.ratingButtonText}>Good</Text>
            <Text style={styles.ratingSubtext}>In 1 day</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.ratingButton, styles.easyButton]}
            onPress={() => handleRating('easy')}
          >
            <Text style={styles.ratingButtonText}>Easy</Text>
            <Text style={styles.ratingSubtext}>In 4 days</Text>
          </TouchableOpacity>
        </View>
      )}

      <View style={styles.navigationContainer}>
        <TouchableOpacity
          style={[styles.navButton, currentIndex === 0 && styles.navButtonDisabled]}
          onPress={previousCard}
          disabled={currentIndex === 0}
        >
          <Text style={styles.navButtonText}>← Previous</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[
            styles.navButton,
            (currentIndex === cards.length - 1 || !isFlipped) && styles.navButtonDisabled,
          ]}
          onPress={() => handleRating('medium')}
          disabled={currentIndex === cards.length - 1 || !isFlipped}
        >
          <Text style={styles.navButtonText}>Skip →</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
    padding: 16,
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
  backButton: {
    backgroundColor: '#2563eb',
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
    marginBottom: 16,
  },
  progressText: {
    fontSize: 16,
    color: '#6b7280',
    marginBottom: 8,
    textAlign: 'center',
  },
  progressBar: {
    height: 4,
    backgroundColor: '#e5e7eb',
    borderRadius: 2,
    overflow: 'hidden',
    marginBottom: 4,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#2563eb',
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
  },
  card: {
    width: CARD_WIDTH,
    minHeight: 300,
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 24,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 5,
    backfaceVisibility: 'hidden',
  },
  cardFront: {
    position: 'absolute',
  },
  cardBack: {
    position: 'absolute',
  },
  cardLabel: {
    fontSize: 14,
    color: '#6b7280',
    fontWeight: '600',
    textTransform: 'uppercase',
    marginBottom: 12,
  },
  cardText: {
    fontSize: 24,
    color: '#111827',
    fontWeight: '600',
    textAlign: 'center',
    marginBottom: 16,
  },
  difficultyBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
    marginTop: 8,
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
    fontWeight: '600',
    color: '#374151',
  },
  notesContainer: {
    marginTop: 16,
    padding: 12,
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    width: '100%',
  },
  notesLabel: {
    fontSize: 12,
    color: '#6b7280',
    fontWeight: '600',
    marginBottom: 4,
  },
  notesText: {
    fontSize: 14,
    color: '#374151',
    lineHeight: 20,
  },
  tapHint: {
    fontSize: 12,
    color: '#9ca3af',
    fontStyle: 'italic',
    marginTop: 8,
  },
  ratingContainer: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 16,
  },
  ratingButton: {
    flex: 1,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  hardButton: {
    backgroundColor: '#fef2f2',
    borderWidth: 2,
    borderColor: '#ef4444',
  },
  mediumButton: {
    backgroundColor: '#fefce8',
    borderWidth: 2,
    borderColor: '#eab308',
  },
  easyButton: {
    backgroundColor: '#f0fdf4',
    borderWidth: 2,
    borderColor: '#22c55e',
  },
  ratingButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 2,
  },
  ratingSubtext: {
    fontSize: 12,
    color: '#6b7280',
  },
  navigationContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 16,
  },
  navButton: {
    flex: 1,
    backgroundColor: '#2563eb',
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  navButtonDisabled: {
    backgroundColor: '#d1d5db',
  },
  navButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
