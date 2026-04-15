import React from 'react';
import { renderHook, act } from '@testing-library/react-native';
import { AuthProvider, useAuth } from '../context/AuthContext';

// Mock the api service
jest.mock('../services/api', () => ({
  __esModule: true,
  default: {
    login: jest.fn(),
    register: jest.fn(),
    setToken: jest.fn(),
  },
  api: {
    login: jest.fn(),
    register: jest.fn(),
    setToken: jest.fn(),
  },
}));

import api from '../services/api';

describe('AuthContext', () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  );

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('initial state', () => {
    it('should have null user and token initially', () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('login', () => {
    it('should update user and token on successful login', async () => {
      const mockUser = {
        id: '123',
        email: 'test@example.com',
        name: 'Test User',
        role: 'CUSTOMER',
      };

      (api.login as jest.Mock).mockResolvedValue({
        data: {
          accessToken: 'test-token',
          user: mockUser,
        },
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login('test@example.com', 'password123');
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.token).toBe('test-token');
      expect(api.setToken).toHaveBeenCalledWith('test-token');
    });

    it('should throw error on login failure', async () => {
      const mockError = new Error('Invalid credentials');
      (api.login as jest.Mock).mockRejectedValue(mockError);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await expect(
        act(async () => {
          await result.current.login('test@example.com', 'wrongpassword');
        })
      ).rejects.toThrow('Invalid credentials');

      expect(result.current.user).toBeNull();
    });

    it('should set isLoading during login', async () => {
      let resolveLogin: (value: any) => void;
      const loginPromise = new Promise((resolve) => {
        resolveLogin = resolve;
      });

      (api.login as jest.Mock).mockReturnValue(loginPromise);

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Start login
      let loginResult: Promise<void>;
      act(() => {
        loginResult = result.current.login('test@example.com', 'password123');
      });

      // Should be loading
      expect(result.current.isLoading).toBe(true);

      // Resolve login
      await act(async () => {
        resolveLogin!({
          data: {
            accessToken: 'token',
            user: { id: '1', email: 'test@example.com', name: 'Test', role: 'CUSTOMER' },
          },
        });
        await loginResult;
      });

      // Should not be loading anymore
      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('register', () => {
    it('should call api.register', async () => {
      (api.register as jest.Mock).mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.register('new@example.com', 'password123', 'New User');
      });

      expect(api.register).toHaveBeenCalledWith('new@example.com', 'password123', 'New User');
    });
  });

  describe('logout', () => {
    it('should clear user and token on logout', async () => {
      (api.login as jest.Mock).mockResolvedValue({
        data: {
          accessToken: 'test-token',
          user: { id: '1', email: 'test@example.com', name: 'Test', role: 'CUSTOMER' },
        },
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Login first
      await act(async () => {
        await result.current.login('test@example.com', 'password123');
      });

      expect(result.current.user).not.toBeNull();

      // Logout
      act(() => {
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(api.setToken).toHaveBeenCalledWith(null);
    });
  });
});
