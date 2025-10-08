import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  useColorScheme,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { CategoryType } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';

type HomeScreenNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Home'>;

interface Props {
  navigation: HomeScreenNavigationProp;
}

interface CategoryItem {
  key: CategoryType;
  title: string;
  description: string;
  icon: string;
  count: number;
}

export default function HomeScreen({ navigation }: Props) {
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const colorScheme = useColorScheme();
  const isDark = true; // Force dark mode as default

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const allCategories = await dataLoader.loadAllCategories();

      const categoryItems: CategoryItem[] = [
        {
          key: 'vocabulary',
          title: 'Vocabulary',
          description: 'Essential words and phrases',
          icon: '📚',
          count: allCategories.vocabulary.total_count,
        },
        {
          key: 'phrases',
          title: 'Phrases',
          description: 'Common expressions',
          icon: '💬',
          count: allCategories.phrases.total_count,
        },
        {
          key: 'grammar',
          title: 'Grammar',
          description: 'Language structure',
          icon: '📖',
          count: allCategories.grammar.total_count,
        },
        {
          key: 'conjugations',
          title: 'Conjugations',
          description: 'Verb forms',
          icon: '🔄',
          count: allCategories.conjugations.total_count,
        },
        {
          key: 'proverbs',
          title: 'Proverbs',
          description: 'Traditional wisdom',
          icon: '💡',
          count: allCategories.proverbs.total_count,
        },
      ];

      setCategories(categoryItems);
    } catch (error) {
      console.error('Error loading categories:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryPress = (category: CategoryType) => {
    navigation.navigate('Category', { category });
  };

  const renderCategoryCard = ({ item }: { item: CategoryItem }) => (
    <TouchableOpacity
      style={[styles.card, isDark && styles.darkCard]}
      onPress={() => handleCategoryPress(item.key)}
      activeOpacity={0.7}
    >
      <View style={styles.cardContent}>
        <Text style={styles.icon}>{item.icon}</Text>
        <View style={styles.cardTextContainer}>
          <Text style={[styles.cardTitle, isDark && styles.darkText]}>{item.title}</Text>
          <Text style={[styles.cardDescription, isDark && styles.darkTextSecondary]}>{item.description}</Text>
          <Text style={[styles.cardCount, isDark && styles.darkTextSecondary]}>{item.count} cards</Text>
        </View>
        <Text style={[styles.arrow, isDark && styles.darkText]}>›</Text>
      </View>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={[styles.centerContainer, isDark && styles.darkBg]}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={[styles.loadingText, isDark && styles.darkText]}>Loading flashcards...</Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, isDark && styles.darkBg]}>
      <View style={[styles.header, isDark && styles.darkHeader]}>
        <Text style={[styles.headerTitle, isDark && styles.darkText]}>Learn Kikuyu</Text>
        <Text style={[styles.headerSubtitle, isDark && styles.darkTextSecondary]}>Choose a category to begin</Text>
      </View>
      <FlatList
        data={categories}
        renderItem={renderCategoryCard}
        keyExtractor={item => item.key}
        contentContainerStyle={styles.listContainer}
      />
    </View>
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
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#6b7280',
  },
  header: {
    backgroundColor: '#fff',
    padding: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#6b7280',
  },
  listContainer: {
    padding: 16,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  cardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
  },
  icon: {
    fontSize: 40,
    marginRight: 16,
  },
  cardTextContainer: {
    flex: 1,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  cardDescription: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 4,
  },
  cardCount: {
    fontSize: 12,
    color: '#2563eb',
    fontWeight: '500',
  },
  arrow: {
    fontSize: 24,
    color: '#9ca3af',
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  darkHeader: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
  },
  darkCard: {
    backgroundColor: '#1f2937',
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
});
