import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, useColorScheme } from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { Flashcard } from '../types/flashcard';
import { dataLoader } from '../lib/dataLoader';

type StudyListNavigationProp = NativeStackNavigationProp<RootStackParamList, 'StudyList'>;
type StudyListRouteProp = RouteProp<RootStackParamList, 'StudyList'>;

interface Props {
  navigation: StudyListNavigationProp;
  route: StudyListRouteProp;
}

export default function StudyListScreen({ route }: Props) {
  const { category, difficulties } = route.params;
  const [items, setItems] = useState<Flashcard[]>([]);
  const [flipped, setFlipped] = useState<Record<string, boolean>>({});
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;

  useEffect(() => {
    (async () => {
      const categoryData = await dataLoader.loadCategory(category);
      const selected = dataLoader.getCardsByDifficulty(categoryData, difficulties);
      setItems(selected);
    })();
  }, [category, difficulties]);

  const toggleFlip = (id: string) => {
    setFlipped(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const renderItem = ({ item }: { item: Flashcard }) => {
    const isFlipped = flipped[item.id];
    return (
      <TouchableOpacity onPress={() => toggleFlip(item.id)} activeOpacity={0.8}>
        <View style={[styles.row, isDark && styles.darkRow]}>
          <View style={styles.rowHeader}>
            <Text style={[styles.badge, styles[`badge_${item.difficulty}`]]}>{item.difficulty}</Text>
            <Text style={[styles.kikuyu, isDark && styles.darkText]}>{item.kikuyu}</Text>
          </View>
          <Text style={[styles.english, isDark && styles.darkTextSecondary]}>
            {isFlipped ? item.english : 'Tap to reveal'}
          </Text>
          {item.cultural_notes && (
            <Text style={[styles.notes, isDark && styles.darkTextSecondary]} numberOfLines={isFlipped ? 4 : 1}>
              {item.cultural_notes}
            </Text>
          )}
        </View>
      </TouchableOpacity>
    );
  };

  return (
    <View style={[styles.container, isDark && styles.darkBg]}>
      <FlatList
        data={items}
        keyExtractor={i => i.id}
        renderItem={renderItem}
        ItemSeparatorComponent={() => <View style={styles.sep} />}
        contentContainerStyle={{ padding: 16, paddingBottom: 24 }}
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
  },
  english: {
    fontSize: 16,
    color: '#374151',
    marginTop: 2,
  },
  notes: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 8,
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
});


