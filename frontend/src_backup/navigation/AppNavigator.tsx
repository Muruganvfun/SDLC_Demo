import React, { useEffect } from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';

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

const AuthNavigator: React.FC = () => {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#4F46E5' },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ title: 'Login' }} />
      <AuthStack.Screen name="Register" component={RegisterScreen} options={{ title: 'Register' }} />
    </AuthStack.Navigator>
  );
};

const MainNavigator: React.FC = () => {
  return (
    <AppStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#4F46E5' },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <AppStack.Screen name="ProductList" component={ProductListScreen} options={{ title: 'Products' }} />
      <AppStack.Screen name="CreateOrder" component={CreateOrderScreen} options={{ title: 'Create Order' }} />
      <AppStack.Screen name="OrderList" component={OrderListScreen} options={{ title: 'My Orders' }} />
      <AppStack.Screen name="OrderDetail" component={OrderDetailScreen} options={{ title: 'Order Details' }} />
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
