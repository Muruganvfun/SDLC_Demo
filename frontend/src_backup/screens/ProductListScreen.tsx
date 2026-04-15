import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { RootStackParamList } from '../navigation/AppNavigator';

type ProductListScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'ProductList'>;
};

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  inStock: boolean;
}

const ProductListScreen: React.FC<ProductListScreenProps> = ({ navigation }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { logout, user } = useAuth();
  const { items, addItem, total } = useCart();

  const fetchProducts = async () => {
    try {
      setError(null);
      console.log('Fetching products...');
      const response = await api.getProducts();
      console.log('Products response:', response);
      const productList = response.data?.content || [];
      console.log('Setting products:', productList.length);
      setProducts(productList);
    } catch (err: any) {
      console.error('Failed to fetch products:', err);
      setError(err.message || 'Failed to load products');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: () => (
        <View style={styles.headerRight}>
          <TouchableOpacity onPress={() => navigation.navigate('CreateOrder')} style={styles.headerButton}>
            <Text style={styles.headerButtonText}>Cart ({items.length})</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('OrderList')} style={styles.headerButton}>
            <Text style={styles.headerButtonText}>Orders</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={logout} style={styles.headerButton}>
            <Text style={styles.headerButtonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      ),
    });
  }, [navigation, logout, items.length]);

  const renderProduct = ({ item }: { item: Product }) => (
    <View style={styles.productCard}>
      <View style={styles.productInfo}>
        <Text style={styles.productName}>{item.name}</Text>
        <Text style={styles.productDescription} numberOfLines={2}>{item.description}</Text>
        <Text style={styles.productPrice}>${item.price.toFixed(2)}</Text>
        <Text style={[styles.stockStatus, { color: item.inStock !== false ? '#10B981' : '#EF4444' }]}>
          {item.inStock !== false ? 'In Stock' : 'Out of Stock'}
        </Text>
      </View>
      <TouchableOpacity
        style={[styles.addButton, item.inStock === false && styles.addButtonDisabled]}
        onPress={() => addItem({ productId: item.id, productName: item.name, price: item.price })}
        disabled={item.inStock === false}
      >
        <Text style={styles.addButtonText}>Add</Text>
      </TouchableOpacity>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#4F46E5" />
        <Text style={{ marginTop: 10, color: '#666' }}>Loading products...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text style={{ color: '#EF4444', fontSize: 16, marginBottom: 10 }}>Error: {error}</Text>
        <TouchableOpacity style={styles.addButton} onPress={() => { setLoading(true); fetchProducts(); }}>
          <Text style={styles.addButtonText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.greeting}>Welcome, {user?.name}</Text>

      <FlatList
        data={products}
        renderItem={renderProduct}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); fetchProducts(); }} />
        }
        ListEmptyComponent={<Text style={styles.emptyText}>No products available</Text>}
      />

      {items.length > 0 && (
        <View style={styles.cartBar}>
          <View>
            <Text style={styles.cartText}>{items.length} item(s) in cart</Text>
            <Text style={styles.cartTotal}>Total: ${total.toFixed(2)}</Text>
          </View>
          <TouchableOpacity style={styles.checkoutButton} onPress={() => navigation.navigate('CreateOrder')}>
            <Text style={styles.checkoutButtonText}>Checkout</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  greeting: { padding: 16, fontSize: 18, fontWeight: '600', color: '#374151' },
  list: { padding: 16 },
  productCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  productInfo: { flex: 1 },
  productName: { fontSize: 18, fontWeight: 'bold', color: '#1F2937', marginBottom: 4 },
  productDescription: { fontSize: 14, color: '#6B7280', marginBottom: 8 },
  productPrice: { fontSize: 20, fontWeight: 'bold', color: '#4F46E5' },
  stockStatus: { fontSize: 12, marginTop: 4 },
  addButton: {
    backgroundColor: '#4F46E5',
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  addButtonDisabled: { backgroundColor: '#9CA3AF' },
  addButtonText: { color: '#fff', fontWeight: 'bold' },
  emptyText: { textAlign: 'center', color: '#6B7280', marginTop: 32 },
  cartBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#E5E7EB',
  },
  cartText: { fontSize: 16, fontWeight: '600', color: '#374151' },
  cartTotal: { fontSize: 14, color: '#6B7280' },
  checkoutButton: { backgroundColor: '#10B981', paddingVertical: 12, paddingHorizontal: 24, borderRadius: 8 },
  checkoutButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  headerRight: { flexDirection: 'row' },
  headerButton: { marginLeft: 16 },
  headerButtonText: { color: '#fff', fontSize: 14 },
});

export default ProductListScreen;
