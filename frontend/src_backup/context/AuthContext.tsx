import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../services/api';

interface User {
  id: string;
  email: string;
  name: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      console.log('AuthContext: Starting login for', email);
      const response = await api.login(email, password);
      console.log('AuthContext: Login response received:', JSON.stringify(response, null, 2));
      
      if (!response || !response.data) {
        console.error('AuthContext: Invalid response structure', response);
        throw new Error('Invalid response from server');
      }
      
      const { accessToken, user: userData } = response.data;
      console.log('AuthContext: Extracted accessToken length:', accessToken?.length);
      console.log('AuthContext: Extracted user:', JSON.stringify(userData, null, 2));
      
      if (!accessToken || !userData) {
        console.error('AuthContext: Missing accessToken or user data');
        throw new Error('Missing authentication data');
      }
      
      api.setToken(accessToken);
      console.log('AuthContext: Token set in API service');
      
      setToken(accessToken);
      console.log('AuthContext: Token state updated');
      
      setUser(userData);
      console.log('AuthContext: User state updated - navigation should trigger now');
    } catch (error) {
      console.error('AuthContext: Login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
      console.log('AuthContext: Loading state set to false');
    }
  };

  const register = async (email: string, password: string, name: string) => {
    setIsLoading(true);
    try {
      await api.register(email, password, name);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    api.setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
