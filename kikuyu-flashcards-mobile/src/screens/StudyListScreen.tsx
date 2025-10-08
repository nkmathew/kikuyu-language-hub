import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, useColorScheme, Clipboard, Alert } from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { Flashcard } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';
import AsyncStorage from '@react-native-async-storage/async-storage';

type StudyListNavigationProp = NativeStackNavigationProp<RootStackParamList, 'StudyList'>;
type StudyListRouteProp = RouteProp<RootStackParamList, 'StudyList'>;

interface Props {
  navigation: StudyListNavigationProp;
  route: StudyListRouteProp;
}

type SortOption = 'difficulty' | 'recent' | 'alphabetical';

export default function StudyListScreen({ route }: Props) {
  const { category, difficulties } = route.params;
  const [items, setItems] = useState<Flashcard[]>([]);
  const [sortBy, setSortBy] = useState<SortOption>('recent');
  const [flaggedItems, setFlaggedItems] = useState<Set<string>>(new Set());
  const [currentPosition, setCurrentPosition] = useState({ visibleStart: 0, visibleEnd: 0, total: 0 });
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    (async () => {
      const categoryData = await dataLoader.loadCategory(category);
      const selected = dataLoader.getCardsByDifficulty(categoryData, difficulties);
      setItems(selected);
      
      // Load flagged items from storage
      try {
        const storedFlagged = await AsyncStorage.getItem('flaggedItems');
        if (storedFlagged) {
          setFlaggedItems(new Set(JSON.parse(storedFlagged)));
        }
      } catch (error) {
        console.error('Error loading flagged items:', error);
      }
    })();
  }, [category, difficulties]);

  const sortItems = (items: Flashcard[], sortBy: SortOption): Flashcard[] => {
    const sorted = [...items];
    switch (sortBy) {
      case 'difficulty':
        return sorted.sort((a, b) => {
          const difficultyOrder = { beginner: 0, intermediate: 1, advanced: 2 };
          return difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty];
        });
      case 'recent':
        return sorted.sort((a, b) => {
          const aDate = new Date(a.source?.last_updated || a.source?.created_date || '2025-01-01');
          const bDate = new Date(b.source?.last_updated || b.source?.created_date || '2025-01-01');
          return bDate.getTime() - aDate.getTime();
        });
      case 'alphabetical':
        return sorted.sort((a, b) => a.kikuyu.localeCompare(b.kikuyu));
      default:
        return sorted;
    }
  };

  const toggleFlag = async (id: string) => {
    setFlaggedItems(prev => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      
      // Persist to storage
      AsyncStorage.setItem('flaggedItems', JSON.stringify([...newSet]));
      
      return newSet;
    });
  };

  const copyToClipboard = (text: string) => {
    Clipboard.setString(text);
    Alert.alert('Copied!', 'Text copied to clipboard');
  };

  const exportFlaggedItems = () => {
    const flaggedCards = items.filter(item => flaggedItems.has(item.id));
    if (flaggedCards.length === 0) {
      Alert.alert('No Flagged Items', 'Please flag some items first');
      return;
    }
    
    const exportData = flaggedCards.map(card => ({
      kikuyu: card.kikuyu,
      english: card.english,
      difficulty: card.difficulty,
      category: card.category,
      notes: card.cultural_notes || card.notes || '',
    }));
    
    const jsonString = JSON.stringify(exportData, null, 2);
    Clipboard.setString(jsonString);
    Alert.alert('Exported!', `Copied ${flaggedCards.length} flagged items to clipboard`);
  };

  const renderItem = ({ item }: { item: Flashcard }) => {
    const isFlagged = flaggedItems.has(item.id);
    const lastUpdated = item.source?.last_updated || item.source?.created_date;
    
    return (
      <View style={[styles.row, isDark && styles.darkRow, isFlagged && styles.flaggedRow]}>
        <View style={styles.cardContainer}>
          {/* Kikuyu Card */}
          <View style={[styles.miniCard, styles.kikuyuCard, isDark && styles.darkMiniCard]}>
            <View style={styles.miniCardHeader}>
              <Text style={[styles.miniCardLabel, isDark && styles.darkTextSecondary]}>Kikuyu</Text>
              <View style={styles.actionButtons}>
                <TouchableOpacity 
                  style={[styles.iconButton, isDark && styles.darkIconButton]} 
                  onPress={() => toggleFlag(item.id)}
                >
                  <Text style={styles.iconText}>{isFlagged ? '🚩' : '🏳️'}</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                  style={[styles.iconButton, isDark && styles.darkIconButton]} 
                  onPress={() => copyToClipboard(`${item.kikuyu} - ${item.english}`)}
                >
                  <Text style={styles.iconText}>📋</Text>
                </TouchableOpacity>
              </View>
            </View>
            <Text style={[styles.miniCardText, isDark && styles.darkText]}>{item.kikuyu}</Text>
          </View>

          {/* English Card */}
          <View style={[styles.miniCard, styles.englishCard, isDark && styles.darkMiniCard]}>
            <Text style={[styles.miniCardLabel, isDark && styles.darkTextSecondary]}>English</Text>
            <Text style={[styles.miniCardText, isDark && styles.darkTextSecondary]}>{item.english}</Text>
          </View>
        </View>

        {/* Notes and metadata */}
        {item.cultural_notes && (
          <Text style={[styles.notes, isDark && styles.darkTextSecondary]}>
            {item.cultural_notes}
          </Text>
        )}
        
        <View style={styles.cardFooter}>
          <Text style={[styles.difficultyBadge, styles[`difficulty_${item.difficulty}`]]}>
            {item.difficulty}
          </Text>
          {lastUpdated && (
            <Text style={[styles.lastUpdated, isDark && styles.darkTextSecondary]}>
              Updated: {new Date(lastUpdated).toLocaleDateString()}
            </Text>
          )}
        </View>
      </View>
    );
  };

  const sortedItems = sortItems(items, sortBy);

  const handleViewableItemsChanged = ({ viewableItems }: any) => {
    if (viewableItems.length > 0) {
      const firstIndex = viewableItems[0].index || 0;
      const lastIndex = viewableItems[viewableItems.length - 1].index || 0;
      setCurrentPosition({
        visibleStart: firstIndex + 1,
        visibleEnd: lastIndex + 1,
        total: sortedItems.length
      });
    }
  };

  return (
    <View style={[styles.container, isDark && styles.darkBg]}>
      <View style={[styles.header, isDark && styles.darkHeader]}>
        <View style={[styles.positionIndicator, isDark && styles.darkPositionIndicator]}>
          <Text style={[styles.positionText, isDark && styles.darkText]}>
            📍 {currentPosition.visibleStart}-{currentPosition.visibleEnd} of {currentPosition.total}
          </Text>
        </View>
        <View style={styles.sortButtons}>
          <TouchableOpacity 
            style={[styles.sortButton, isDark && styles.darkSortButton, sortBy === 'difficulty' && styles.sortButtonActive]}
            onPress={() => setSortBy('difficulty')}
          >
            <Text style={[styles.sortButtonText, isDark && styles.darkSortButtonText]}>📊 Difficulty</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.sortButton, isDark && styles.darkSortButton, sortBy === 'recent' && styles.sortButtonActive]}
            onPress={() => setSortBy('recent')}
          >
            <Text style={[styles.sortButtonText, isDark && styles.darkSortButtonText]}>🕒 Recent</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.sortButton, isDark && styles.darkSortButton, sortBy === 'alphabetical' && styles.sortButtonActive]}
            onPress={() => setSortBy('alphabetical')}
          >
            <Text style={[styles.sortButtonText, isDark && styles.darkSortButtonText]}>🔤 A-Z</Text>
          </TouchableOpacity>
        </View>
        {flaggedItems.size > 0 && (
          <TouchableOpacity 
            style={[styles.exportButton, isDark && styles.darkExportButton]}
            onPress={exportFlaggedItems}
          >
            <Text style={styles.exportButtonText}>
              📤 Export {flaggedItems.size} Flagged
            </Text>
          </TouchableOpacity>
        )}
      </View>
      <FlatList
        data={sortedItems}
        keyExtractor={i => i.id}
        renderItem={renderItem}
        ItemSeparatorComponent={() => <View style={styles.sep} />}
        contentContainerStyle={{ padding: 16, paddingBottom: 24 }}
        onViewableItemsChanged={handleViewableItemsChanged}
        viewabilityConfig={{
          itemVisiblePercentThreshold: 50
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  darkBg: {
    backgroundColor: '#111827',
  },
  row: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  darkRow: {
    backgroundColor: '#1f2937',
  },
  rowHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
    gap: 8,
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    fontSize: 12,
    fontWeight: '700',
    color: '#111827',
    textTransform: 'capitalize',
  },
  badge_beginner: { backgroundColor: '#dcfce7' },
  badge_intermediate: { backgroundColor: '#fef3c7' },
  badge_advanced: { backgroundColor: '#fee2e2' },
  kikuyu: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    flex: 1,
    flexWrap: 'wrap',
  },
  english: {
    fontSize: 16,
    color: '#374151',
    marginTop: 2,
    flexWrap: 'wrap',
  },
  notes: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 8,
    flexWrap: 'wrap',
  },
  sep: {
    height: 12,
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
  header: {
    backgroundColor: '#fff',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  darkHeader: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
  },
  positionIndicator: {
    alignItems: 'center',
    marginBottom: 12,
    paddingVertical: 8,
    backgroundColor: '#f9fafb',
    borderRadius: 8,
  },
  positionText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  darkPositionIndicator: {
    backgroundColor: '#374151',
  },
  sortButtons: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  sortButton: {
    backgroundColor: '#f3f4f6',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#d1d5db',
  },
  sortButtonActive: {
    backgroundColor: '#2563eb',
    borderColor: '#2563eb',
  },
  sortButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#374151',
  },
  exportButton: {
    backgroundColor: '#10b981',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  darkExportButton: {
    backgroundColor: '#059669',
  },
  exportButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  actionButtons: {
    flexDirection: 'row',
    gap: 4,
  },
  iconButton: {
    padding: 4,
    borderRadius: 4,
    backgroundColor: '#f3f4f6',
  },
  darkIconButton: {
    backgroundColor: '#374151',
  },
  iconText: {
    fontSize: 16,
  },
  flaggedRow: {
    borderLeftWidth: 4,
    borderLeftColor: '#ef4444',
  },
  cardContainer: {
    flexDirection: 'column',
    gap: 8,
    marginBottom: 8,
  },
  miniCard: {
    flex: 1,
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  darkMiniCard: {
    backgroundColor: '#374151',
    borderColor: '#4b5563',
  },
  kikuyuCard: {
    backgroundColor: '#f0f9ff',
    borderColor: '#0ea5e9',
  },
  englishCard: {
    backgroundColor: '#f0fdf4',
    borderColor: '#22c55e',
  },
  miniCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  miniCardLabel: {
    fontSize: 10,
    fontWeight: '600',
    color: '#6b7280',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  miniCardText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    lineHeight: 22,
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 8,
  },
  difficultyBadge: {
    fontSize: 10,
    fontWeight: '600',
    color: '#6b7280',
    textTransform: 'capitalize',
    fontStyle: 'italic',
  },
  difficulty_beginner: {
    color: '#059669',
  },
  difficulty_intermediate: {
    color: '#d97706',
  },
  difficulty_advanced: {
    color: '#dc2626',
  },
  lastUpdated: {
    fontSize: 10,
    color: '#9ca3af',
    fontStyle: 'italic',
  },
  darkSortButton: {
    backgroundColor: '#374151',
    borderColor: '#4b5563',
  },
  darkSortButtonText: {
    color: '#d1d5db',
  },
});


