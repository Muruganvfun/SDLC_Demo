import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import { WiproColors } from '../theme/WiproTheme';

import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import ProductListScreen from '../screens/ProductListScreen';
import CreateOrderScreen from '../screens/CreateOrderScreen';
import OrderListScreen from '../screens/OrderListScreen';
import OrderDetailScreen from '../screens/OrderDetailScreen';

export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  ProductList: undefined;
  CreateOrder: undefined;
  OrderList: undefined;
  OrderDetail: { orderId: string };
};

const AuthStack = createNativeStackNavigator<RootStackParamList>();
const AppStack = createNativeStackNavigator<RootStackParamList>();

const WiproHeader = ({ title, showBack, navigation, rightComponent }: any) => (
  <View style={headerStyles.container}>
    <View style={headerStyles.leftSection}>
      {showBack && (
        <TouchableOpacity onPress={() => navigation.goBack()} style={headerStyles.backButton}>
          <Text style={headerStyles.backText}>←</Text>
        </TouchableOpacity>
      )}
      <View style={headerStyles.logoContainer}>
        <View style={headerStyles.wiproLogo}>
          <Text style={headerStyles.wiproText}>wipro</Text>
        </View>
        <Text style={headerStyles.studioText}>OMS Studio</Text>
      </View>
    </View>
    <View style={headerStyles.rightSection}>
      {rightComponent}
    </View>
  </View>
);

const headerStyles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: WiproColors.background,
    borderBottomWidth: 1,
    borderBottomColor: WiproColors.gray[200],
  },
  leftSection: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  backButton: {
    padding: 8,
    marginRight: 8,
  },
  backText: {
    fontSize: 20,
    color: WiproColors.primary,
    fontWeight: '600',
  },
  logoContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  wiproLogo: {
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  wiproText: {
    fontSize: 18,
    fontWeight: '700',
    color: WiproColors.primary,
    letterSpacing: 1,
  },
  studioText: {
    fontSize: 18,
    fontWeight: '600',
    color: WiproColors.black,
  },
  rightSection: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 15,
  },
});

const AuthNavigator: React.FC = () => {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: WiproColors.background },
        headerTintColor: WiproColors.primary,
        headerTitleStyle: { fontWeight: '600', color: WiproColors.black },
        headerShadowVisible: false,
      }}
    >
      <AuthStack.Screen 
        name="Login" 
        component={LoginScreen} 
        options={{ 
          title: 'OMS Studio',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
      <AuthStack.Screen 
        name="Register" 
        component={RegisterScreen} 
        options={{ 
          title: 'Create Account',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
    </AuthStack.Navigator>
  );
};

const MainNavigator: React.FC = () => {
  return (
    <AppStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: WiproColors.background },
        headerTintColor: WiproColors.primary,
        headerTitleStyle: { fontWeight: '600', color: WiproColors.black },
        headerShadowVisible: false,
      }}
    >
      <AppStack.Screen 
        name="ProductList" 
        component={ProductListScreen} 
        options={{ 
          title: 'OMS Studio - Products',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
      <AppStack.Screen 
        name="CreateOrder" 
        component={CreateOrderScreen} 
        options={{ 
          title: 'Create Order',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
      <AppStack.Screen 
        name="OrderList" 
        component={OrderListScreen} 
        options={{ 
          title: 'My Orders',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
      <AppStack.Screen 
        name="OrderDetail" 
        component={OrderDetailScreen} 
        options={{ 
          title: 'Order Details',
          headerLeft: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 10 }}>
              <Text style={{ fontSize: 16, fontWeight: '700', color: WiproColors.primary }}>wipro</Text>
            </View>
          ),
        }} 
      />
    </AppStack.Navigator>
  );
};

const AppNavigator: React.FC = () => {
  const { user } = useAuth();
  
  console.log('AppNavigator: user is', user ? 'logged in' : 'null');

  if (user) {
    return <MainNavigator />;
  }
  
  return <AuthNavigator />;
};

export default AppNavigator;
