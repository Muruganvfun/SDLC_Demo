import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import api from '../services/api';
import { RootStackParamList } from '../navigation/AppNavigator';

type OrderDetailScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'OrderDetail'>;
  route: RouteProp<RootStackParamList, 'OrderDetail'>;
};

interface OrderDetail {
  id: string;
  status: string;
  totalAmount: number;
  shippingAddress: string;
  items: Array<{
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  payment?: { transactionId: string; status: string };
  shipment?: { trackingNumber: string; carrier: string; status: string };
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

const OrderDetailScreen: React.FC<OrderDetailScreenProps> = ({ navigation, route }) => {
  const { orderId } = route.params;
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchOrder = async () => {
    try {
      const response = await api.getOrder(orderId);
      setOrder(response.data);
    } catch (err) {
      console.error('Failed to fetch order:', err);
      Alert.alert('Error', 'Failed to load order details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrder();
  }, [orderId]);

  const handleCancel = async () => {
    Alert.alert('Cancel Order', 'Are you sure you want to cancel this order?', [
      { text: 'No', style: 'cancel' },
      {
        text: 'Yes', style: 'destructive', onPress: async () => {
          setActionLoading(true);
          try {
            await api.cancelOrder(orderId);
            fetchOrder();
            Alert.alert('Success', 'Order cancelled');
          } catch (err: any) {
            Alert.alert('Error', err.response?.data?.message || 'Failed to cancel order');
          } finally {
            setActionLoading(false);
          }
        }
      },
    ]);
  };

  const handlePay = async () => {
    setActionLoading(true);
    try {
      await api.payOrder(orderId);
      fetchOrder();
      Alert.alert('Success', 'Payment processed successfully!');
    } catch (err: any) {
      Alert.alert('Error', err.response?.data?.message || 'Payment failed');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#4F46E5" />
      </View>
    );
  }

  if (!order) {
    return (
      <View style={styles.centered}>
        <Text>Order not found</Text>
      </View>
    );
  }

  const canCancel = order.status === 'PENDING' || order.status === 'CONFIRMED';
  const canPay = order.status === 'CONFIRMED';

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.orderId}>Order #{order.id.slice(0, 8)}</Text>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(order.status) }]}>
          <Text style={styles.statusText}>{order.status}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Items</Text>
        {order.items.map((item, index) => (
          <View key={index} style={styles.itemRow}>
            <View style={styles.itemInfo}>
              <Text style={styles.itemName}>{item.productName}</Text>
              <Text style={styles.itemQty}>Qty: {item.quantity} x ${item.unitPrice.toFixed(2)}</Text>
            </View>
            <Text style={styles.itemSubtotal}>${item.subtotal.toFixed(2)}</Text>
          </View>
        ))}
        <View style={styles.totalRow}>
          <Text style={styles.totalLabel}>Total</Text>
          <Text style={styles.totalAmount}>${order.totalAmount.toFixed(2)}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Shipping Address</Text>
        <Text style={styles.address}>{order.shippingAddress || 'Not specified'}</Text>
      </View>

      {order.payment && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Payment</Text>
          <Text style={styles.infoText}>Transaction: {order.payment.transactionId}</Text>
          <Text style={styles.infoText}>Status: {order.payment.status}</Text>
        </View>
      )}

      {order.shipment && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Shipping</Text>
          <Text style={styles.infoText}>Carrier: {order.shipment.carrier}</Text>
          <Text style={styles.infoText}>Tracking: {order.shipment.trackingNumber}</Text>
          <Text style={styles.infoText}>Status: {order.shipment.status}</Text>
        </View>
      )}

      <View style={styles.actions}>
        {canPay && (
          <TouchableOpacity style={styles.payButton} onPress={handlePay} disabled={actionLoading}>
            {actionLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Pay Now</Text>}
          </TouchableOpacity>
        )}
        {canCancel && (
          <TouchableOpacity style={styles.cancelButton} onPress={handleCancel} disabled={actionLoading}>
            <Text style={styles.cancelButtonText}>Cancel Order</Text>
          </TouchableOpacity>
        )}
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16, backgroundColor: '#fff' },
  orderId: { fontSize: 20, fontWeight: 'bold', color: '#1F2937' },
  statusBadge: { paddingHorizontal: 16, paddingVertical: 6, borderRadius: 16 },
  statusText: { color: '#fff', fontWeight: 'bold' },
  section: { backgroundColor: '#fff', marginTop: 12, padding: 16 },
  sectionTitle: { fontSize: 16, fontWeight: 'bold', color: '#374151', marginBottom: 12 },
  itemRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: '#E5E7EB' },
  itemInfo: { flex: 1 },
  itemName: { fontSize: 16, fontWeight: '500' },
  itemQty: { fontSize: 14, color: '#6B7280' },
  itemSubtotal: { fontSize: 16, fontWeight: 'bold', color: '#4F46E5' },
  totalRow: { flexDirection: 'row', justifyContent: 'space-between', paddingTop: 12, marginTop: 8 },
  totalLabel: { fontSize: 18, fontWeight: 'bold' },
  totalAmount: { fontSize: 24, fontWeight: 'bold', color: '#4F46E5' },
  address: { fontSize: 14, color: '#6B7280', lineHeight: 22 },
  infoText: { fontSize: 14, color: '#6B7280', marginBottom: 4 },
  actions: { padding: 16, gap: 12 },
  payButton: { backgroundColor: '#10B981', padding: 16, borderRadius: 8, alignItems: 'center' },
  buttonText: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
  cancelButton: { backgroundColor: '#fff', padding: 16, borderRadius: 8, alignItems: 'center', borderWidth: 2, borderColor: '#EF4444' },
  cancelButtonText: { color: '#EF4444', fontSize: 18, fontWeight: 'bold' },
});

export default OrderDetailScreen;
