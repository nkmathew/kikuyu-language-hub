import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  ScrollView,
  useColorScheme,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import { RootStackParamList } from '../navigation/AppNavigator';
import { CategoryData, DifficultyLevel } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';

type CategoryScreenNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Category'>;
type CategoryScreenRouteProp = RouteProp<RootStackParamList, 'Category'>;

interface Props {
  navigation: CategoryScreenNavigationProp;
  route: CategoryScreenRouteProp;
}

export default function CategoryScreen({ navigation, route }: Props) {
  const { category } = route.params;
  const [categoryData, setCategoryData] = useState<CategoryData | null>(null);
  const [selectedDifficulties, setSelectedDifficulties] = useState<DifficultyLevel[]>(['beginner']);
  const [loading, setLoading] = useState(true);
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    loadCategoryData();
  }, [category]);

  const loadCategoryData = async () => {
    try {
      const data = await dataLoader.loadCategory(category);
      setCategoryData(data);
    } catch (error) {
      console.error('Error loading category:', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleDifficulty = (difficulty: DifficultyLevel) => {
    setSelectedDifficulties(prev => {
      if (prev.includes(difficulty)) {
        const newSelection = prev.filter(d => d !== difficulty);
        return newSelection.length > 0 ? newSelection : prev;
      } else {
        return [...prev, difficulty];
      }
    });
  };

  const handleStartStudy = () => {
    navigation.navigate('Flashcard', {
      category,
      difficulties: selectedDifficulties,
    });
  };

  const handleOpenStudyList = () => {
    navigation.navigate('StudyList', {
      category,
      difficulties: selectedDifficulties,
    });
  };

  if (loading) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    );
  }

  if (!categoryData) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <Text style={[styles.errorText, isDark && styles.darkText]}>Failed to load category data</Text>
      </View>
    );
  }

  const totalSelectedCards = selectedDifficulties.reduce((sum, diff) => {
    return sum + categoryData.difficulty_counts[diff];
  }, 0);

  return (
    <ScrollView style={[styles.container, isDark && styles.darkBg]}>
      <View style={styles.statsContainer}>
        <View style={[styles.statCard, isDark && styles.darkCard]}>
          <Text style={[styles.statNumber, isDark && styles.darkTextPrimary]}>{categoryData.total_count}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>Total Cards</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, isDark && styles.darkText]}>Select Difficulty</Text>
        <Text style={[styles.sectionSubtitle, isDark && styles.darkTextSecondary]}>Choose one or more levels</Text>

        <View style={styles.difficultyContainer}>
          {(['beginner', 'intermediate', 'advanced'] as DifficultyLevel[]).map(difficulty => {
            const count = categoryData.difficulty_counts[difficulty];
            const isSelected = selectedDifficulties.includes(difficulty);

            return (
              <TouchableOpacity
                key={difficulty}
                style={[
                  styles.difficultyCard,
                  isDark && styles.darkCard,
                  isSelected && (isDark ? styles.darkDifficultyCardSelected : styles.difficultyCardSelected),
                  count === 0 && styles.difficultyCardDisabled,
                ]}
                onPress={() => count > 0 && toggleDifficulty(difficulty)}
                disabled={count === 0}
                activeOpacity={0.7}
              >
                <View style={styles.difficultyHeader}>
                  <Text style={[
                    styles.difficultyTitle,
                    isDark && styles.darkText,
                    isSelected && styles.difficultyTitleSelected,
                  ]}>
                    {difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
                  </Text>
                  {isSelected && <Text style={styles.checkmark}>✓</Text>}
                </View>
                <Text style={[
                  styles.difficultyCount,
                  isDark && styles.darkTextSecondary,
                  isSelected && styles.difficultyCountSelected,
                ]}>
                  {count} cards
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      <View style={styles.footer}>
        <TouchableOpacity
          style={[
            styles.startButton,
            totalSelectedCards === 0 && styles.startButtonDisabled,
          ]}
          onPress={handleStartStudy}
          disabled={totalSelectedCards === 0}
        >
          <Text style={styles.startButtonText}>
            Start Studying ({totalSelectedCards} cards)
          </Text>
        </TouchableOpacity>
        <View style={{ height: 12 }} />
        <TouchableOpacity
          style={[
            styles.startButton,
            { backgroundColor: '#111827', borderWidth: 2, borderColor: '#2563eb' },
            totalSelectedCards === 0 && styles.startButtonDisabled,
          ]}
          onPress={handleOpenStudyList}
          disabled={totalSelectedCards === 0}
          activeOpacity={0.8}
        >
          <Text style={styles.startButtonText}>Study List Mode</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
  },
  errorText: {
    fontSize: 16,
    color: '#ef4444',
  },
  statsContainer: {
    padding: 16,
  },
  statCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statNumber: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#2563eb',
  },
  statLabel: {
    fontSize: 16,
    color: '#6b7280',
    marginTop: 4,
  },
  section: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  sectionSubtitle: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 16,
  },
  difficultyContainer: {
    gap: 12,
  },
  difficultyCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    borderWidth: 2,
    borderColor: '#e5e7eb',
  },
  difficultyCardSelected: {
    borderColor: '#2563eb',
    backgroundColor: '#eff6ff',
  },
  darkDifficultyCardSelected: {
    borderColor: '#3b82f6',
    backgroundColor: '#1e3a8a',
  },
  difficultyCardDisabled: {
    opacity: 0.5,
  },
  difficultyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  difficultyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  difficultyTitleSelected: {
    color: '#2563eb',
  },
  checkmark: {
    fontSize: 20,
    color: '#2563eb',
  },
  difficultyCount: {
    fontSize: 14,
    color: '#6b7280',
  },
  difficultyCountSelected: {
    color: '#2563eb',
  },
  footer: {
    padding: 16,
    paddingBottom: 32,
  },
  startButton: {
    backgroundColor: '#2563eb',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  startButtonDisabled: {
    backgroundColor: '#9ca3af',
  },
  startButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  darkCard: {
    backgroundColor: '#1f2937',
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextPrimary: {
    color: '#3b82f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
});
