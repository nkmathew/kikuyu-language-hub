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

  return (
    <ScrollView style={[styles.container, isDark && styles.darkBg]}>
      <View style={styles.statsContainer}>
        <View style={[styles.statCard, isDark && styles.darkCard]}>
          <Text style={[styles.statNumber, isDark && styles.darkTextPrimary]}>{categoryData.total_count}</Text>
          <Text style={[styles.statLabel, isDark && styles.darkTextSecondary]}>Total Cards</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, isDark && styles.darkText]}>Choose Study Mode</Text>
        <Text style={[styles.sectionSubtitle, isDark && styles.darkTextSecondary]}>Select flashcards or study list for any difficulty level</Text>

        <View style={styles.difficultyContainer}>
          {/* All option */}
          <View
            style={[
              styles.difficultyCard,
              isDark && styles.darkCard,
            ]}
          >
            <View style={styles.difficultyHeader}>
              <Text style={[
                styles.difficultyTitle,
                isDark && styles.darkText,
              ]}>
                All Levels
              </Text>
              <Text style={[
                styles.difficultyCount,
                isDark && styles.darkTextSecondary,
              ]}>
                {categoryData.total_count} cards
              </Text>
            </View>

            <View style={styles.buttonRow}>
              <TouchableOpacity
                style={[
                  styles.actionButton,
                  styles.flashcardsButton,
                ]}
                onPress={() => navigation.navigate('Flashcard', { category, difficulties: ['all'] })}
                activeOpacity={0.7}
              >
                <Text style={styles.actionButtonText}>📚 Flashcards</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.actionButton,
                  styles.listButton,
                ]}
                onPress={() => navigation.navigate('StudyList', { category, difficulties: ['all'] })}
                activeOpacity={0.7}
              >
                <Text style={styles.actionButtonText}>📋 Study List</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Individual difficulty options */}
          {(['beginner', 'intermediate', 'advanced'] as DifficultyLevel[]).map(difficulty => {
            const count = categoryData.difficulty_counts[difficulty];

            return (
              <View
                key={difficulty}
                style={[
                  styles.difficultyCard,
                  isDark && styles.darkCard,
                  count === 0 && styles.difficultyCardDisabled,
                ]}
              >
                <View style={styles.difficultyHeader}>
                  <Text style={[
                    styles.difficultyTitle,
                    isDark && styles.darkText,
                  ]}>
                    {difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
                  </Text>
                  <Text style={[
                    styles.difficultyCount,
                    isDark && styles.darkTextSecondary,
                  ]}>
                    {count} cards
                  </Text>
                </View>

                <View style={styles.buttonRow}>
                  <TouchableOpacity
                    style={[
                      styles.actionButton,
                      styles.flashcardsButton,
                      count === 0 && styles.actionButtonDisabled,
                    ]}
                    onPress={() => navigation.navigate('Flashcard', { category, difficulties: [difficulty] })}
                    disabled={count === 0}
                    activeOpacity={0.7}
                  >
                    <Text style={styles.actionButtonText}>📚 Flashcards</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[
                      styles.actionButton,
                      styles.listButton,
                      count === 0 && styles.actionButtonDisabled,
                    ]}
                    onPress={() => navigation.navigate('StudyList', { category, difficulties: [difficulty] })}
                    disabled={count === 0}
                    activeOpacity={0.7}
                  >
                    <Text style={styles.actionButtonText}>📋 Study List</Text>
                  </TouchableOpacity>
                </View>
              </View>
            );
          })}
        </View>
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
  difficultyCardDisabled: {
    opacity: 0.5,
  },
  difficultyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  difficultyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  difficultyCount: {
    fontSize: 14,
    color: '#6b7280',
  },
  buttonRow: {
    flexDirection: 'row',
    gap: 8,
  },
  actionButton: {
    flex: 1,
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
  },
  flashcardsButton: {
    backgroundColor: '#2563eb',
  },
  listButton: {
    backgroundColor: '#059669',
  },
  actionButtonDisabled: {
    backgroundColor: '#9ca3af',
    opacity: 0.5,
  },
  actionButtonText: {
    color: '#fff',
    fontSize: 14,
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
