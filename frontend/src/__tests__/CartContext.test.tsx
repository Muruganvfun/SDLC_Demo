import React from 'react';
import { renderHook, act } from '@testing-library/react-native';
import { CartProvider, useCart } from '../context/CartContext';

describe('CartContext', () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <CartProvider>{children}</CartProvider>
  );

  describe('initial state', () => {
    it('should have empty cart initially', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      expect(result.current.items).toEqual([]);
      expect(result.current.total).toBe(0);
    });
  });

  describe('addItem', () => {
    it('should add new item to cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      expect(result.current.items).toHaveLength(1);
      expect(result.current.items[0]).toEqual({
        productId: '123',
        productName: 'Test Product',
        price: 29.99,
        quantity: 1,
      });
    });

    it('should increment quantity if item already exists', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      expect(result.current.items).toHaveLength(1);
      expect(result.current.items[0].quantity).toBe(2);
    });

    it('should add multiple different items', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Product 1',
          price: 29.99,
        });
      });

      act(() => {
        result.current.addItem({
          productId: '456',
          productName: 'Product 2',
          price: 49.99,
        });
      });

      expect(result.current.items).toHaveLength(2);
    });
  });

  describe('removeItem', () => {
    it('should remove item from cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      act(() => {
        result.current.removeItem('123');
      });

      expect(result.current.items).toHaveLength(0);
    });

    it('should not affect other items when removing one', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({ productId: '123', productName: 'Product 1', price: 29.99 });
        result.current.addItem({ productId: '456', productName: 'Product 2', price: 49.99 });
      });

      act(() => {
        result.current.removeItem('123');
      });

      expect(result.current.items).toHaveLength(1);
      expect(result.current.items[0].productId).toBe('456');
    });
  });

  describe('updateQuantity', () => {
    it('should update item quantity', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      act(() => {
        result.current.updateQuantity('123', 5);
      });

      expect(result.current.items[0].quantity).toBe(5);
    });

    it('should remove item when quantity is set to 0 or less', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 29.99,
        });
      });

      act(() => {
        result.current.updateQuantity('123', 0);
      });

      expect(result.current.items).toHaveLength(0);
    });
  });

  describe('clearCart', () => {
    it('should remove all items from cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({ productId: '123', productName: 'Product 1', price: 29.99 });
        result.current.addItem({ productId: '456', productName: 'Product 2', price: 49.99 });
      });

      act(() => {
        result.current.clearCart();
      });

      expect(result.current.items).toHaveLength(0);
      expect(result.current.total).toBe(0);
    });
  });

  describe('total calculation', () => {
    it('should calculate total correctly', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({ productId: '123', productName: 'Product 1', price: 29.99 });
        result.current.addItem({ productId: '456', productName: 'Product 2', price: 49.99 });
      });

      expect(result.current.total).toBeCloseTo(79.98, 2);
    });

    it('should update total when quantity changes', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          productId: '123',
          productName: 'Test Product',
          price: 10.00,
        });
      });

      act(() => {
        result.current.updateQuantity('123', 3);
      });

      expect(result.current.total).toBe(30.00);
    });
  });
});
