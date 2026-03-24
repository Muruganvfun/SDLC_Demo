import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useFocusEffect } from '@react-navigation/native';
import api from '../services/api';
import { RootStackParamList } from '../navigation/AppNavigator';
import { WiproColors, WiproBorderRadius, WiproShadow } from '../theme/WiproTheme';

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
    case 'PENDING': return WiproColors.warning;
    case 'CONFIRMED': return WiproColors.primary;
    case 'PAID': return '#8B5CF6';
    case 'SHIPPED': return WiproColors.success;
    case 'DELIVERED': return '#059669';
    case 'CANCELLED': return WiproColors.error;
    default: return WiproColors.gray[500];
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
  container: { flex: 1, backgroundColor: WiproColors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: WiproColors.background },
  list: { padding: 16 },
  orderCard: {
    backgroundColor: WiproColors.white,
    borderRadius: WiproBorderRadius.lg,
    padding: 16,
    marginBottom: 12,
    ...WiproShadow.card,
  },
  orderHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  orderId: { fontSize: 16, fontWeight: '600', color: WiproColors.black },
  statusBadge: { paddingHorizontal: 12, paddingVertical: 4, borderRadius: WiproBorderRadius.full },
  statusText: { color: WiproColors.white, fontSize: 12, fontWeight: 'bold' },
  orderDetails: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  orderInfo: { fontSize: 14, color: WiproColors.gray[500] },
  orderDate: { fontSize: 14, color: WiproColors.gray[500] },
  orderTotal: { fontSize: 20, fontWeight: 'bold', color: WiproColors.primary, textAlign: 'right' },
  emptyContainer: { alignItems: 'center', paddingTop: 48 },
  emptyText: { fontSize: 18, color: WiproColors.gray[500], marginBottom: 16 },
  shopButton: { backgroundColor: WiproColors.primary, paddingVertical: 12, paddingHorizontal: 24, borderRadius: WiproBorderRadius.full },
  shopButtonText: { color: WiproColors.white, fontWeight: '600', fontSize: 16 },
});

export default OrderListScreen;
