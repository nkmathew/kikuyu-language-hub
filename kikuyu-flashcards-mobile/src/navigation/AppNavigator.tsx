import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { CategoryType, DifficultyLevel } from '../types/flashcard';

// Screens
import HomeScreen from '../screens/HomeScreen';
import CategoryScreen from '../screens/CategoryScreen';
import FlashcardScreen from '../screens/FlashcardScreen';
import ProgressScreen from '../screens/ProgressScreen';

export type RootStackParamList = {
  Home: undefined;
  Category: { category: CategoryType };
  Flashcard: { category: CategoryType; difficulties: DifficultyLevel[] };
};

export type BottomTabParamList = {
  HomeTab: undefined;
  Progress: undefined;
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
    </Stack.Navigator>
  );
}

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={{
          tabBarActiveTintColor: '#2563eb',
          tabBarInactiveTintColor: '#6b7280',
          headerShown: false,
        }}
      >
        <Tab.Screen
          name="HomeTab"
          component={HomeStack}
          options={{ title: 'Learn' }}
        />
        <Tab.Screen
          name="Progress"
          component={ProgressScreen}
          options={{ title: 'Progress', headerShown: true }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
}
