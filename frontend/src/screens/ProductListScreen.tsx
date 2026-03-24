import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { RootStackParamList } from '../navigation/AppNavigator';
import { WiproColors, WiproBorderRadius, WiproShadow } from '../theme/WiproTheme';

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
      <View style={styles.productImagePlaceholder}>
        <Text style={styles.productImageText}>{item.name.charAt(0)}</Text>
      </View>
      <View style={styles.productInfo}>
        <Text style={styles.productName}>{item.name}</Text>
        <Text style={styles.productDescription} numberOfLines={2}>{item.description}</Text>
        <View style={styles.productFooter}>
          <Text style={styles.productPrice}>${item.price.toFixed(2)}</Text>
          <Text style={[styles.stockStatus, { color: item.inStock !== false ? WiproColors.success : WiproColors.error }]}>
            {item.inStock !== false ? '● In Stock' : '● Out of Stock'}
          </Text>
        </View>
      </View>
      <TouchableOpacity
        style={[styles.addButton, item.inStock === false && styles.addButtonDisabled]}
        onPress={() => addItem({ productId: item.id, productName: item.name, price: item.price })}
        disabled={item.inStock === false}
      >
        <Text style={styles.addButtonText}>+ Add</Text>
      </TouchableOpacity>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={WiproColors.primary} />
        <Text style={styles.loadingText}>Loading products...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>Error: {error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={() => { setLoading(true); fetchProducts(); }}>
          <Text style={styles.retryButtonText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.welcomeSection}>
        <Text style={styles.welcomeText}>Welcome back,</Text>
        <Text style={styles.userName}>{user?.name}</Text>
      </View>

      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Available Products</Text>
        <Text style={styles.sectionSubtitle}>{products.length} items</Text>
      </View>

      <FlatList
        data={products}
        renderItem={renderProduct}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl 
            refreshing={refreshing} 
            onRefresh={() => { setRefreshing(true); fetchProducts(); }}
            tintColor={WiproColors.primary}
          />
        }
        ListEmptyComponent={<Text style={styles.emptyText}>No products available</Text>}
      />

      {items.length > 0 && (
        <View style={styles.cartBar}>
          <View style={styles.cartInfo}>
            <Text style={styles.cartText}>{items.length} item(s) in cart</Text>
            <Text style={styles.cartTotal}>Total: ${total.toFixed(2)}</Text>
          </View>
          <TouchableOpacity style={styles.checkoutButton} onPress={() => navigation.navigate('CreateOrder')}>
            <Text style={styles.checkoutButtonText}>Checkout →</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: WiproColors.background,
  },
  centered: { 
    flex: 1, 
    justifyContent: 'center', 
    alignItems: 'center',
    backgroundColor: WiproColors.background,
  },
  loadingText: {
    marginTop: 12,
    color: WiproColors.gray[500],
    fontSize: 14,
  },
  errorText: {
    color: WiproColors.error,
    fontSize: 16,
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: WiproColors.primary,
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: WiproBorderRadius.full,
  },
  retryButtonText: {
    color: WiproColors.white,
    fontWeight: '600',
  },
  welcomeSection: {
    padding: 20,
    paddingBottom: 10,
  },
  welcomeText: {
    fontSize: 14,
    color: WiproColors.gray[500],
  },
  userName: {
    fontSize: 24,
    fontWeight: '600',
    color: WiproColors.black,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingBottom: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: WiproColors.black,
  },
  sectionSubtitle: {
    fontSize: 14,
    color: WiproColors.gray[500],
  },
  list: { 
    padding: 16,
    paddingTop: 8,
  },
  productCard: {
    backgroundColor: WiproColors.white,
    borderRadius: WiproBorderRadius.lg,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    ...WiproShadow.card,
  },
  productImagePlaceholder: {
    width: 60,
    height: 60,
    borderRadius: WiproBorderRadius.sm,
    backgroundColor: WiproColors.gray[100],
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  productImageText: {
    fontSize: 24,
    fontWeight: '600',
    color: WiproColors.primary,
  },
  productInfo: { 
    flex: 1,
  },
  productName: { 
    fontSize: 16, 
    fontWeight: '600', 
    color: WiproColors.black, 
    marginBottom: 4,
  },
  productDescription: { 
    fontSize: 13, 
    color: WiproColors.gray[500], 
    marginBottom: 8,
    lineHeight: 18,
  },
  productFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  productPrice: { 
    fontSize: 18, 
    fontWeight: '700', 
    color: WiproColors.primary,
  },
  stockStatus: { 
    fontSize: 12,
  },
  addButton: {
    backgroundColor: WiproColors.primary,
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: WiproBorderRadius.full,
    marginLeft: 12,
  },
  addButtonDisabled: { 
    backgroundColor: WiproColors.gray[300],
  },
  addButtonText: { 
    color: WiproColors.white, 
    fontWeight: '600',
    fontSize: 14,
  },
  emptyText: { 
    textAlign: 'center', 
    color: WiproColors.gray[500], 
    marginTop: 32,
    fontSize: 16,
  },
  cartBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: WiproColors.white,
    padding: 16,
    paddingHorizontal: 20,
    borderTopWidth: 1,
    borderTopColor: WiproColors.gray[200],
    ...WiproShadow.medium,
  },
  cartInfo: {},
  cartText: { 
    fontSize: 16, 
    fontWeight: '600', 
    color: WiproColors.black,
  },
  cartTotal: { 
    fontSize: 14, 
    color: WiproColors.gray[500],
    marginTop: 2,
  },
  checkoutButton: { 
    backgroundColor: WiproColors.primary, 
    paddingVertical: 14, 
    paddingHorizontal: 24, 
    borderRadius: WiproBorderRadius.full,
  },
  checkoutButtonText: { 
    color: WiproColors.white, 
    fontWeight: '600', 
    fontSize: 16,
  },
  headerRight: { 
    flexDirection: 'row',
    gap: 12,
  },
  headerButton: { 
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: WiproColors.primary,
    borderRadius: WiproBorderRadius.full,
  },
  headerButtonText: { 
    color: WiproColors.white, 
    fontSize: 13,
    fontWeight: '500',
  },
});

export default ProductListScreen;
