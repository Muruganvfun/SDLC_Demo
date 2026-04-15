import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useFocusEffect } from '@react-navigation/native';
import api from '../services/api';
import { RootStackParamList } from '../navigation/AppNavigator';

type OrderListScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'OrderList'>;
};

interface OrderSummary {
  id: string;
  status: string;
  totalAmount: number;
  itemCount: number;
  createdAt: string;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return '#F59E0B';
    case 'CONFIRMED': return '#3B82F6';
    case 'PAID': return '#8B5CF6';
    case 'SHIPPED': return '#10B981';
    case 'DELIVERED': return '#059669';
    case 'CANCELLED': return '#EF4444';
    default: return '#6B7280';
  }
};

const OrderListScreen: React.FC<OrderListScreenProps> = ({ navigation }) => {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchOrders = async () => {
    try {
      const response = await api.getOrders();
      setOrders(response.data?.content || []);
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      fetchOrders();
    }, [])
  );

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const renderOrder = ({ item }: { item: OrderSummary }) => (
    <TouchableOpacity
      style={styles.orderCard}
      onPress={() => navigation.navigate('OrderDetail', { orderId: item.id })}
    >
      <View style={styles.orderHeader}>
        <Text style={styles.orderId}>Order #{item.id.slice(0, 8)}</Text>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(item.status) }]}>
          <Text style={styles.statusText}>{item.status}</Text>
        </View>
      </View>
      <View style={styles.orderDetails}>
        <Text style={styles.orderInfo}>{item.itemCount} item(s)</Text>
        <Text style={styles.orderDate}>{formatDate(item.createdAt)}</Text>
      </View>
      <Text style={styles.orderTotal}>${item.totalAmount.toFixed(2)}</Text>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#4F46E5" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={orders}
        renderItem={renderOrder}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); fetchOrders(); }} />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>No orders yet</Text>
            <TouchableOpacity style={styles.shopButton} onPress={() => navigation.navigate('ProductList')}>
              <Text style={styles.shopButtonText}>Start Shopping</Text>
            </TouchableOpacity>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  list: { padding: 16 },
  orderCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  orderHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  orderId: { fontSize: 16, fontWeight: 'bold', color: '#1F2937' },
  statusBadge: { paddingHorizontal: 12, paddingVertical: 4, borderRadius: 12 },
  statusText: { color: '#fff', fontSize: 12, fontWeight: 'bold' },
  orderDetails: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  orderInfo: { fontSize: 14, color: '#6B7280' },
  orderDate: { fontSize: 14, color: '#6B7280' },
  orderTotal: { fontSize: 20, fontWeight: 'bold', color: '#4F46E5', textAlign: 'right' },
  emptyContainer: { alignItems: 'center', paddingTop: 48 },
  emptyText: { fontSize: 18, color: '#6B7280', marginBottom: 16 },
  shopButton: { backgroundColor: '#4F46E5', paddingVertical: 12, paddingHorizontal: 24, borderRadius: 8 },
  shopButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});

export default OrderListScreen;
