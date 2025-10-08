import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  useColorScheme,
  Clipboard,
  Share,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Flashcard } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';
import LoadingSpinner from '../components/LoadingSpinner';

interface FlagReason {
  [cardId: string]: string;
}

export default function FlaggedTranslationsScreen() {
  const [flaggedItems, setFlaggedItems] = useState<Flashcard[]>([]);
  const [flagReasons, setFlagReasons] = useState<FlagReason>({});
  const [loading, setLoading] = useState(true);
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    loadFlaggedItems();
  }, []);

  const loadFlaggedItems = async () => {
    try {
      const storedFlagged = await AsyncStorage.getItem('flaggedItems');
      const storedReasons = await AsyncStorage.getItem('flagReasons');

      if (storedFlagged) {
        const flaggedIds = JSON.parse(storedFlagged);
        const allItems: Flashcard[] = [];

        // Load all categories to find flagged items
        const categories = ['vocabulary', 'proverbs', 'conjugations', 'grammar', 'general', 'phrases'];
        for (const category of categories) {
          try {
            const categoryData = await dataLoader.loadCategory(category as any);
            const allCards = dataLoader.getCardsByDifficulty(categoryData, ['beginner', 'intermediate', 'advanced']);
            allItems.push(...allCards);
          } catch (error) {
            console.error(`Error loading category ${category}:`, error);
          }
        }

        // Filter to only flagged items
        const flagged = allItems.filter(item => flaggedIds.includes(item.id));
        setFlaggedItems(flagged);
      }

      // Load reasons
      if (storedReasons) {
        setFlagReasons(JSON.parse(storedReasons));
      }
    } catch (error) {
      console.error('Error loading flagged items:', error);
    } finally {
      setLoading(false);
    }
  };

  const removeFlag = async (id: string) => {
    try {
      const storedFlagged = await AsyncStorage.getItem('flaggedItems');
      if (storedFlagged) {
        const flaggedIds = JSON.parse(storedFlagged);
        const updatedIds = flaggedIds.filter((itemId: string) => itemId !== id);
        await AsyncStorage.setItem('flaggedItems', JSON.stringify(updatedIds));
        setFlaggedItems(prev => prev.filter(item => item.id !== id));

        // Also remove the reason
        const newReasons = { ...flagReasons };
        delete newReasons[id];
        setFlagReasons(newReasons);
        await AsyncStorage.setItem('flagReasons', JSON.stringify(newReasons));
      }
    } catch (error) {
      console.error('Error removing flag:', error);
      Alert.alert('Error', 'Failed to remove flag');
    }
  };

  const addReason = (id: string) => {
    Alert.prompt(
      'Add Reason',
      'Why is this translation flagged?',
      [
        {
          text: 'Cancel',
          style: 'cancel',
        },
        {
          text: 'Save',
          onPress: async (reason) => {
            if (reason && reason.trim()) {
              const newReasons = { ...flagReasons, [id]: reason.trim() };
              setFlagReasons(newReasons);
              try {
                await AsyncStorage.setItem('flagReasons', JSON.stringify(newReasons));
              } catch (error) {
                console.error('Error saving reason:', error);
                Alert.alert('Error', 'Failed to save reason');
              }
            }
          },
        },
      ],
      'plain-text',
      flagReasons[id] || ''
    );
  };

  const clearAllFlags = () => {
    Alert.alert(
      'Clear All Flags',
      'This will remove all flagged translations and their reasons. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            await AsyncStorage.removeItem('flaggedItems');
            await AsyncStorage.removeItem('flagReasons');
            setFlaggedItems([]);
            setFlagReasons({});
            Alert.alert('Success', 'All flags have been cleared');
          },
        },
      ]
    );
  };

  const extractBatchNumber = (id: string): string => {
    // Extract batch number from ID format: ek_b[batch]_[number]
    const match = id.match(/ek_b(\d+)_/);
    return match ? match[1] : 'unknown';
  };

  const formatForExport = (items: Flashcard[]) => {
    return items.map(item => ({
      id: item.id,
      batch: extractBatchNumber(item.id),
      kikuyu: item.kikuyu,
      english: item.english,
      difficulty: item.difficulty,
      category: item.category,
      cultural_notes: item.cultural_notes || '',
      source: item.source?.origin || '',
      flag_reason: flagReasons[item.id] || '',
    }));
  };

  const copyToClipboard = () => {
    if (flaggedItems.length === 0) {
      Alert.alert('No Items', 'No flagged translations to copy');
      return;
    }

    const exportData = formatForExport(flaggedItems);
    const text = exportData.map(item =>
      `[Batch ${item.batch}] ${item.id}\n${item.kikuyu} - ${item.english}\nDifficulty: ${item.difficulty} | Category: ${item.category}${item.flag_reason ? `\nReason: ${item.flag_reason}` : ''}`
    ).join('\n\n');

    Clipboard.setString(text);
    Alert.alert('Copied!', `${flaggedItems.length} flagged translations copied to clipboard`);
  };

  const shareAsEmail = async () => {
    if (flaggedItems.length === 0) {
      Alert.alert('No Items', 'No flagged translations to share');
      return;
    }

    const exportData = formatForExport(flaggedItems);
    const emailBody = `Flagged Kikuyu Translations (${flaggedItems.length} items):\n\n` +
      exportData.map(item =>
        `• [Batch ${item.batch}] ${item.id}\n  ${item.kikuyu} - ${item.english}\n  Difficulty: ${item.difficulty} | Category: ${item.category}\n  Source: ${item.source}\n  Notes: ${item.cultural_notes}${item.flag_reason ? `\n  ⚠️ Flag Reason: ${item.flag_reason}` : ''}\n`
      ).join('\n');

    try {
      await Share.share({
        message: emailBody,
        title: 'Flagged Kikuyu Translations',
      });
    } catch (error) {
      console.error('Error sharing:', error);
      Alert.alert('Error', 'Failed to share translations');
    }
  };

  const saveToFile = async () => {
    if (flaggedItems.length === 0) {
      Alert.alert('No Items', 'No flagged translations to save');
      return;
    }

    const exportData = formatForExport(flaggedItems);
    const jsonString = JSON.stringify(exportData, null, 2);
    
    try {
      await Share.share({
        message: jsonString,
        title: 'Flagged Translations Export',
      });
    } catch (error) {
      console.error('Error saving file:', error);
      Alert.alert('Error', 'Failed to save file');
    }
  };

  const renderItem = ({ item }: { item: Flashcard }) => {
    const reason = flagReasons[item.id];

    return (
      <View style={[styles.itemCard, isDark && styles.darkItemCard]}>
        <View style={styles.itemHeader}>
          <View style={styles.itemInfo}>
            <Text style={[styles.kikuyuText, isDark && styles.darkText]}>{item.kikuyu}</Text>
            <Text style={[styles.englishText, isDark && styles.darkTextSecondary]}>{item.english}</Text>
          </View>
          <TouchableOpacity
            style={[styles.removeButton, isDark && styles.darkRemoveButton]}
            onPress={() => removeFlag(item.id)}
          >
            <Text style={styles.removeButtonText}>✕</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.itemMeta}>
          <Text style={[styles.metaText, styles[`difficulty_${item.difficulty}`]]}>
            {item.difficulty}
          </Text>
          <Text style={[styles.metaText, isDark && styles.darkTextSecondary]}>
            {item.category}
          </Text>
        </View>

        {item.cultural_notes && (
          <Text style={[styles.notesText, isDark && styles.darkTextSecondary]}>
            {item.cultural_notes}
          </Text>
        )}

        {/* Flag Reason Section */}
        {reason ? (
          <View style={[styles.reasonContainer, isDark && styles.darkReasonContainer]}>
            <View style={styles.reasonHeader}>
              <Text style={[styles.reasonLabel, isDark && styles.darkTextSecondary]}>⚠️ Reason:</Text>
              <TouchableOpacity onPress={() => addReason(item.id)}>
                <Text style={styles.editReasonText}>Edit</Text>
              </TouchableOpacity>
            </View>
            <Text style={[styles.reasonText, isDark && styles.darkText]}>{reason}</Text>
          </View>
        ) : (
          <TouchableOpacity
            style={[styles.addReasonButton, isDark && styles.darkAddReasonButton]}
            onPress={() => addReason(item.id)}
          >
            <Text style={styles.addReasonText}>+ Add Reason</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  if (loading) {
    return <LoadingSpinner message="Loading flagged items..." />;
  }

  return (
    <View style={[styles.container, isDark && styles.darkBg]}>
      <View style={[styles.header, isDark && styles.darkHeader]}>
        <Text style={[styles.headerTitle, isDark && styles.darkText]}>
          Flagged Translations ({flaggedItems.length})
        </Text>
        <Text style={[styles.headerSubtitle, isDark && styles.darkTextSecondary]}>
          Review and export flagged items
        </Text>
      </View>

      {flaggedItems.length === 0 ? (
        <View style={styles.emptyState}>
          <Text style={[styles.emptyTitle, isDark && styles.darkText]}>No Flagged Items</Text>
          <Text style={[styles.emptySubtitle, isDark && styles.darkTextSecondary]}>
            Flag translations in the study list to see them here
          </Text>
        </View>
      ) : (
        <>
          <View style={[styles.actionsContainer, isDark && styles.darkActionsContainer]}>
            <TouchableOpacity
              style={[styles.actionButton, styles.copyButton, isDark && styles.darkActionButton]}
              onPress={copyToClipboard}
            >
              <Text style={styles.actionButtonText}>📋 Copy to Clipboard</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={[styles.actionButton, styles.shareButton, isDark && styles.darkShareButton]}
              onPress={shareAsEmail}
            >
              <Text style={styles.actionButtonText}>📧 Share as Email</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={[styles.actionButton, styles.saveButton, isDark && styles.darkSaveButton]}
              onPress={saveToFile}
            >
              <Text style={styles.actionButtonText}>💾 Save to File</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={[styles.actionButton, styles.clearButton, isDark && styles.darkClearButton]}
              onPress={clearAllFlags}
            >
              <Text style={[styles.actionButtonText, styles.clearButtonText]}>🗑️ Clear All</Text>
            </TouchableOpacity>
          </View>

          <FlatList
            data={flaggedItems}
            renderItem={renderItem}
            keyExtractor={(item) => item.id}
            contentContainerStyle={styles.listContainer}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
          />
        </>
      )}
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
  loadingText: {
    textAlign: 'center',
    marginTop: 50,
    fontSize: 16,
    color: '#6b7280',
  },
  header: {
    backgroundColor: '#fff',
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  darkHeader: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#6b7280',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  actionsContainer: {
    backgroundColor: '#fff',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  darkActionsContainer: {
    backgroundColor: '#1f2937',
    borderBottomColor: '#374151',
  },
  actionButton: {
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
    alignItems: 'center',
  },
  copyButton: {
    backgroundColor: '#3b82f6',
  },
  shareButton: {
    backgroundColor: '#10b981',
  },
  saveButton: {
    backgroundColor: '#8b5cf6',
  },
  clearButton: {
    backgroundColor: '#fee2e2',
    borderWidth: 1,
    borderColor: '#ef4444',
  },
  darkActionButton: {
    backgroundColor: '#2563eb',
  },
  darkShareButton: {
    backgroundColor: '#059669',
  },
  darkSaveButton: {
    backgroundColor: '#7c3aed',
  },
  darkClearButton: {
    backgroundColor: '#7f1d1d',
    borderColor: '#dc2626',
  },
  actionButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  clearButtonText: {
    color: '#ef4444',
  },
  listContainer: {
    padding: 16,
  },
  itemCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  darkItemCard: {
    backgroundColor: '#1f2937',
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  itemInfo: {
    flex: 1,
    marginRight: 12,
  },
  kikuyuText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 4,
  },
  englishText: {
    fontSize: 16,
    color: '#374151',
  },
  removeButton: {
    backgroundColor: '#fee2e2',
    borderRadius: 16,
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
  },
  darkRemoveButton: {
    backgroundColor: '#7f1d1d',
  },
  removeButtonText: {
    color: '#ef4444',
    fontSize: 16,
    fontWeight: 'bold',
  },
  itemMeta: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 8,
  },
  metaText: {
    fontSize: 12,
    fontWeight: '600',
    textTransform: 'capitalize',
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
  notesText: {
    fontSize: 14,
    color: '#6b7280',
    fontStyle: 'italic',
  },
  separator: {
    height: 12,
  },
  darkText: {
    color: '#f3f4f6',
  },
  darkTextSecondary: {
    color: '#9ca3af',
  },
  addReasonButton: {
    marginTop: 8,
    paddingVertical: 6,
    paddingHorizontal: 12,
    backgroundColor: '#eff6ff',
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#3b82f6',
    alignSelf: 'flex-start',
  },
  darkAddReasonButton: {
    backgroundColor: '#1e3a8a',
    borderColor: '#60a5fa',
  },
  addReasonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#2563eb',
  },
  reasonContainer: {
    marginTop: 12,
    padding: 12,
    backgroundColor: '#fef3c7',
    borderRadius: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#f59e0b',
  },
  darkReasonContainer: {
    backgroundColor: '#78350f',
    borderLeftColor: '#fbbf24',
  },
  reasonHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  reasonLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#92400e',
  },
  editReasonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#2563eb',
  },
  reasonText: {
    fontSize: 14,
    color: '#92400e',
    lineHeight: 20,
  },
});
