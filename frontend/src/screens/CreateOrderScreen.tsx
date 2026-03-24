import React, { useState } from 'react';
import { View, Text, TextInput, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, Modal } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import api from '../services/api';
import { useCart } from '../context/CartContext';
import { RootStackParamList } from '../navigation/AppNavigator';
import { WiproColors, WiproBorderRadius, WiproShadow } from '../theme/WiproTheme';

type CreateOrderScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'CreateOrder'>;
};

const PAYMENT_METHODS = [
  { id: 'CREDIT_CARD', name: 'Credit Card', icon: '💳' },
  { id: 'DEBIT_CARD', name: 'Debit Card', icon: '💳' },
  { id: 'PAYPAL', name: 'PayPal', icon: '🅿️' },
  { id: 'BANK_TRANSFER', name: 'Bank Transfer', icon: '🏦' },
];

const CreateOrderScreen: React.FC<CreateOrderScreenProps> = ({ navigation }) => {
  const { items, updateQuantity, removeItem, clearCart, total } = useCart();
  const [loading, setLoading] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedPayment, setSelectedPayment] = useState(PAYMENT_METHODS[0]);
  const [orderStep, setOrderStep] = useState<'cart' | 'processing' | 'success'>('cart');
  const [createdOrderId, setCreatedOrderId] = useState<string | null>(null);
  const [address, setAddress] = useState({
    street: '123 Main St',
    city: 'New York',
    state: 'NY',
    zipCode: '10001',
    country: 'USA',
  });

  const handlePlaceOrder = async () => {
    if (items.length === 0) {
      Alert.alert('Error', 'Your cart is empty');
      return;
    }
    setShowPaymentModal(true);
  };

  const processPayment = async () => {
    setShowPaymentModal(false);
    setLoading(true);
    setOrderStep('processing');

    try {
      const orderItems = items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      }));

      // Step 1: Create Order
      console.log('Creating order with items:', JSON.stringify(orderItems));
      console.log('Shipping address:', JSON.stringify(address));
      const orderResponse = await api.createOrder(orderItems, address);
      console.log('Order response:', JSON.stringify(orderResponse));
      
      if (!orderResponse.data?.id) {
        throw new Error('Order creation failed - no order ID returned');
      }
      
      const orderId = orderResponse.data.id;
      setCreatedOrderId(orderId);
      console.log('Order created successfully:', orderId);

      // Step 2: Process Payment
      console.log('Processing payment for order:', orderId, 'with method:', selectedPayment.id);
      const paymentResponse = await api.payOrder(orderId, selectedPayment.id);
      console.log('Payment response:', JSON.stringify(paymentResponse));
      console.log('Payment successful!');

      // Success - update state
      clearCart();
      setOrderStep('success');
      console.log('Order flow completed successfully');
    } catch (err: any) {
      console.error('Order/Payment error:', err);
      console.error('Error response:', err.response?.data);
      console.error('Error status:', err.response?.status);
      setOrderStep('cart');
      const errorMessage = err.response?.data?.message || err.message || 'Failed to process order';
      Alert.alert('Order Error', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Success Screen
  if (orderStep === 'success') {
    return (
      <View style={styles.successContainer}>
        <Text style={styles.successIcon}>✅</Text>
        <Text style={styles.successTitle}>Order Placed Successfully!</Text>
        <Text style={styles.successText}>Your order has been confirmed and payment processed.</Text>
        <Text style={styles.successOrderId}>Order ID: {createdOrderId}</Text>
        <Text style={styles.successPayment}>Paid with: {selectedPayment.icon} {selectedPayment.name}</Text>
        <TouchableOpacity 
          style={styles.viewOrdersButton} 
          onPress={() => navigation.navigate('OrderList')}
        >
          <Text style={styles.viewOrdersText}>View My Orders</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.continueShoppingButton} 
          onPress={() => {
            setOrderStep('cart');
            setCreatedOrderId(null);
            navigation.navigate('ProductList');
          }}
        >
          <Text style={styles.continueShoppingText}>Continue Shopping</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Processing Screen
  if (orderStep === 'processing') {
    return (
      <View style={styles.processingContainer}>
        <ActivityIndicator size="large" color="#4F46E5" />
        <Text style={styles.processingText}>Processing your order...</Text>
        <Text style={styles.processingSubtext}>Please wait while we confirm your payment</Text>
      </View>
    );
  }

  const renderCartItem = ({ item }: { item: typeof items[0] }) => (
    <View style={styles.cartItem}>
      <View style={styles.itemInfo}>
        <Text style={styles.itemName}>{item.productName}</Text>
        <Text style={styles.itemPrice}>${item.price.toFixed(2)} each</Text>
      </View>
      <View style={styles.quantityControls}>
        <TouchableOpacity style={styles.qtyButton} onPress={() => updateQuantity(item.productId, item.quantity - 1)}>
          <Text style={styles.qtyButtonText}>-</Text>
        </TouchableOpacity>
        <Text style={styles.quantity}>{item.quantity}</Text>
        <TouchableOpacity style={styles.qtyButton} onPress={() => updateQuantity(item.productId, item.quantity + 1)}>
          <Text style={styles.qtyButtonText}>+</Text>
        </TouchableOpacity>
      </View>
      <Text style={styles.subtotal}>${(item.price * item.quantity).toFixed(2)}</Text>
      <TouchableOpacity onPress={() => removeItem(item.productId)}>
        <Text style={styles.removeButton}>X</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.sectionTitle}>Cart Items</Text>
      <FlatList
        data={items}
        renderItem={renderCartItem}
        keyExtractor={(item) => item.productId}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>Your cart is empty</Text>
            <TouchableOpacity 
              style={styles.shopButton} 
              onPress={() => navigation.navigate('ProductList')}
            >
              <Text style={styles.shopButtonText}>Browse Products</Text>
            </TouchableOpacity>
          </View>
        }
        style={styles.cartList}
      />

      <View style={styles.addressSection}>
        <Text style={styles.sectionTitle}>Shipping Address</Text>
        <TextInput style={styles.input} placeholder="Street" value={address.street} onChangeText={(v) => setAddress({ ...address, street: v })} />
        <View style={styles.row}>
          <TextInput style={[styles.input, styles.halfInput]} placeholder="City" value={address.city} onChangeText={(v) => setAddress({ ...address, city: v })} />
          <TextInput style={[styles.input, styles.halfInput]} placeholder="State" value={address.state} onChangeText={(v) => setAddress({ ...address, state: v })} />
        </View>
        <View style={styles.row}>
          <TextInput style={[styles.input, styles.halfInput]} placeholder="Zip Code" value={address.zipCode} onChangeText={(v) => setAddress({ ...address, zipCode: v })} />
          <TextInput style={[styles.input, styles.halfInput]} placeholder="Country" value={address.country} onChangeText={(v) => setAddress({ ...address, country: v })} />
        </View>
      </View>

      <View style={styles.totalSection}>
        <Text style={styles.totalLabel}>Total:</Text>
        <Text style={styles.totalAmount}>${total.toFixed(2)}</Text>
      </View>

      <TouchableOpacity
        style={[styles.placeOrderButton, items.length === 0 && styles.buttonDisabled]}
        onPress={handlePlaceOrder}
        disabled={loading || items.length === 0}
      >
        {loading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.placeOrderText}>Place Order</Text>
        )}
      </TouchableOpacity>

      {/* Payment Method Modal */}
      <Modal
        visible={showPaymentModal}
        transparent={true}
        animationType="slide"
        onRequestClose={() => setShowPaymentModal(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Select Payment Method</Text>
            
            {PAYMENT_METHODS.map((method) => (
              <TouchableOpacity
                key={method.id}
                style={[
                  styles.paymentOption,
                  selectedPayment.id === method.id && styles.paymentOptionSelected
                ]}
                onPress={() => setSelectedPayment(method)}
              >
                <Text style={styles.paymentIcon}>{method.icon}</Text>
                <Text style={styles.paymentName}>{method.name}</Text>
                {selectedPayment.id === method.id && (
                  <Text style={styles.checkmark}>✓</Text>
                )}
              </TouchableOpacity>
            ))}

            <View style={styles.modalSummary}>
              <Text style={styles.modalSummaryLabel}>Total Amount:</Text>
              <Text style={styles.modalSummaryAmount}>${total.toFixed(2)}</Text>
            </View>

            <TouchableOpacity style={styles.payNowButton} onPress={processPayment}>
              <Text style={styles.payNowText}>Pay ${total.toFixed(2)} with {selectedPayment.name}</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.cancelButton} onPress={() => setShowPaymentModal(false)}>
              <Text style={styles.cancelText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: WiproColors.background, padding: 16 },
  sectionTitle: { fontSize: 18, fontWeight: '600', marginBottom: 12, color: WiproColors.black },
  cartList: { maxHeight: 200 },
  cartItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: WiproColors.white,
    padding: 12,
    borderRadius: WiproBorderRadius.md,
    marginBottom: 8,
    ...WiproShadow.card,
  },
  itemInfo: { flex: 1 },
  itemName: { fontSize: 16, fontWeight: '600', color: WiproColors.black },
  itemPrice: { fontSize: 12, color: WiproColors.gray[500] },
  quantityControls: { flexDirection: 'row', alignItems: 'center', marginHorizontal: 8 },
  qtyButton: { backgroundColor: WiproColors.gray[200], width: 28, height: 28, borderRadius: 14, alignItems: 'center', justifyContent: 'center' },
  qtyButtonText: { fontSize: 18, fontWeight: 'bold', color: WiproColors.black },
  quantity: { marginHorizontal: 12, fontSize: 16, fontWeight: '600', color: WiproColors.black },
  subtotal: { fontSize: 16, fontWeight: 'bold', color: WiproColors.primary, marginRight: 8, width: 60, textAlign: 'right' },
  removeButton: { color: WiproColors.error, fontWeight: 'bold', fontSize: 16 },
  emptyContainer: { alignItems: 'center', padding: 20 },
  emptyText: { textAlign: 'center', color: WiproColors.gray[500], marginBottom: 16 },
  shopButton: { backgroundColor: WiproColors.primary, paddingVertical: 12, paddingHorizontal: 24, borderRadius: WiproBorderRadius.full },
  shopButtonText: { color: WiproColors.white, fontWeight: '600', fontSize: 16 },
  addressSection: { marginTop: 16 },
  input: { backgroundColor: WiproColors.white, borderWidth: 1, borderColor: WiproColors.gray[300], borderRadius: WiproBorderRadius.md, padding: 12, marginBottom: 8, color: WiproColors.black },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  halfInput: { width: '48%' },
  totalSection: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, paddingVertical: 16, borderTopWidth: 1, borderTopColor: WiproColors.gray[200] },
  totalLabel: { fontSize: 20, fontWeight: 'bold', color: WiproColors.black },
  totalAmount: { fontSize: 24, fontWeight: 'bold', color: WiproColors.primary },
  placeOrderButton: { backgroundColor: WiproColors.primary, padding: 16, borderRadius: WiproBorderRadius.full, alignItems: 'center', marginTop: 16, ...WiproShadow.medium },
  buttonDisabled: { backgroundColor: WiproColors.gray[300] },
  placeOrderText: { color: WiproColors.white, fontSize: 18, fontWeight: '600' },
  // Modal styles
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center' },
  modalContent: { backgroundColor: WiproColors.white, borderRadius: WiproBorderRadius.xl, padding: 24, width: '90%', maxWidth: 400 },
  modalTitle: { fontSize: 22, fontWeight: 'bold', textAlign: 'center', marginBottom: 20, color: WiproColors.black },
  paymentOption: { flexDirection: 'row', alignItems: 'center', padding: 16, borderWidth: 2, borderColor: WiproColors.gray[200], borderRadius: WiproBorderRadius.md, marginBottom: 12 },
  paymentOptionSelected: { borderColor: WiproColors.primary, backgroundColor: '#EDF2F9' },
  paymentIcon: { fontSize: 24, marginRight: 12 },
  paymentName: { fontSize: 16, fontWeight: '600', flex: 1, color: WiproColors.black },
  checkmark: { fontSize: 20, color: WiproColors.primary, fontWeight: 'bold' },
  modalSummary: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 16, borderTopWidth: 1, borderTopColor: WiproColors.gray[200], marginTop: 8 },
  modalSummaryLabel: { fontSize: 16, color: WiproColors.gray[500] },
  modalSummaryAmount: { fontSize: 24, fontWeight: 'bold', color: WiproColors.primary },
  payNowButton: { backgroundColor: WiproColors.primary, padding: 16, borderRadius: WiproBorderRadius.full, alignItems: 'center', marginTop: 16 },
  payNowText: { color: WiproColors.white, fontSize: 16, fontWeight: 'bold' },
  cancelButton: { padding: 12, alignItems: 'center', marginTop: 8 },
  cancelText: { color: WiproColors.gray[500], fontSize: 16 },
  // Processing styles
  processingContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: WiproColors.background },
  processingText: { fontSize: 20, fontWeight: '600', marginTop: 24, color: WiproColors.black },
  processingSubtext: { fontSize: 14, color: WiproColors.gray[500], marginTop: 8 },
  // Success styles
  successContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: WiproColors.background, padding: 24 },
  successIcon: { fontSize: 64, marginBottom: 16 },
  successTitle: { fontSize: 24, fontWeight: 'bold', color: WiproColors.success, marginBottom: 8 },
  successText: { fontSize: 16, color: WiproColors.gray[500], textAlign: 'center', marginBottom: 16 },
  successOrderId: { fontSize: 14, color: WiproColors.black, marginBottom: 8 },
  successPayment: { fontSize: 14, color: WiproColors.gray[500], marginBottom: 24 },
  viewOrdersButton: { backgroundColor: WiproColors.primary, paddingVertical: 16, paddingHorizontal: 32, borderRadius: WiproBorderRadius.full, marginBottom: 12 },
  viewOrdersText: { color: WiproColors.white, fontSize: 16, fontWeight: 'bold' },
  continueShoppingButton: { paddingVertical: 12, paddingHorizontal: 32 },
  continueShoppingText: { color: WiproColors.primary, fontSize: 16, fontWeight: '600' },
});

export default CreateOrderScreen;
