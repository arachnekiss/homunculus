import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View, Text, Platform } from 'react-native';

// Import screens
import HomeScreen from './screens/HomeScreen';
import SimpleHomeScreen from './screens/SimpleHomeScreen';
import MainScreen from './screens/MainScreen';
import CharacterScreen from './screens/CharacterScreen';
import CameraScreen from './screens/CameraScreen';
import SettingsScreen from './screens/SettingsScreen';

// Global API URL configuration
// 현재 환경에 따라 API URL 설정
const isWeb = Platform.OS === 'web';
global.API_URL = isWeb ? 'http://localhost:5000' : 'http://localhost:5000';

const Stack = createStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <StatusBar style="light" />
      <Stack.Navigator 
        initialRouteName="Home"
        screenOptions={{
          headerStyle: {
            backgroundColor: '#6A11CB',
          },
          headerTintColor: '#fff',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
        }}
      >
        <Stack.Screen 
          name="Home" 
          component={MainScreen} 
          options={{ title: 'AnimeAI' }} 
        />
        <Stack.Screen 
          name="Character" 
          component={CharacterScreen} 
          options={{ title: 'Interact' }} 
        />
        <Stack.Screen 
          name="Camera" 
          component={CameraScreen} 
          options={{ title: 'Camera' }} 
        />
        <Stack.Screen 
          name="Settings" 
          component={SettingsScreen} 
          options={{ title: 'Settings' }} 
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f0f0',
  },
});
