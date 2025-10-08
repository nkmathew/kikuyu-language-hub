import React from 'react';
import { NavigationContainer, DarkTheme, DefaultTheme } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useColorScheme, Text } from 'react-native';
import { CategoryType, DifficultyLevel } from '../types/flashcard';

// Screens
import HomeScreen from '../screens/HomeScreen';
import CategoryScreen from '../screens/CategoryScreen';
import FlashcardScreen from '../screens/FlashcardScreen';
import ProgressScreen from '../screens/ProgressScreen';
import StudyListScreen from '../screens/StudyListScreen';
import SettingsScreen from '../screens/SettingsScreen';

export type RootStackParamList = {
  Home: undefined;
  Category: { category: CategoryType };
  Flashcard: { category: CategoryType; difficulties: DifficultyLevel[] };
  StudyList: { category: CategoryType; difficulties: DifficultyLevel[] };
};

export type BottomTabParamList = {
  HomeTab: undefined;
  Progress: undefined;
  Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<BottomTabParamList>();

function HomeStack() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: {
          backgroundColor: '#2563eb',
        },
        headerTintColor: '#fff',
        headerTitleStyle: {
          fontWeight: 'bold',
        },
      }}
    >
      <Stack.Screen
        name="Home"
        component={HomeScreen}
        options={{ title: 'Kikuyu Flashcards' }}
      />
      <Stack.Screen
        name="Category"
        component={CategoryScreen}
        options={({ route }) => ({ title: route.params.category.charAt(0).toUpperCase() + route.params.category.slice(1) })}
      />
      <Stack.Screen
        name="Flashcard"
        component={FlashcardScreen}
        options={{ title: 'Study' }}
      />
      <Stack.Screen
        name="StudyList"
        component={StudyListScreen}
        options={{ title: 'Study List' }}
      />
    </Stack.Navigator>
  );
}

export default function AppNavigator() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark' || true;
  return (
    <NavigationContainer theme={isDark ? DarkTheme : DefaultTheme}>
      <Tab.Navigator
        screenOptions={{
          tabBarActiveTintColor: isDark ? '#60a5fa' : '#2563eb',
          tabBarInactiveTintColor: isDark ? '#9ca3af' : '#6b7280',
          tabBarStyle: {
            backgroundColor: isDark ? '#111827' : '#ffffff',
            borderTopColor: isDark ? '#374151' : '#e5e7eb',
          },
          headerShown: false,
        }}
      >
        <Tab.Screen
          name="HomeTab"
          component={HomeStack}
          options={{ 
            title: 'Learn',
            tabBarIcon: ({ color, size }) => (
              <Text style={{ fontSize: size, color }}>📚</Text>
            )
          }}
        />
        <Tab.Screen
          name="Progress"
          component={ProgressScreen}
          options={{ 
            title: 'Progress', 
            headerShown: true,
            tabBarIcon: ({ color, size }) => (
              <Text style={{ fontSize: size, color }}>📊</Text>
            )
          }}
        />
        <Tab.Screen
          name="Settings"
          component={SettingsScreen}
          options={{ 
            title: 'Settings', 
            headerShown: true,
            tabBarIcon: ({ color, size }) => (
              <Text style={{ fontSize: size, color }}>⚙️</Text>
            )
          }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
}
