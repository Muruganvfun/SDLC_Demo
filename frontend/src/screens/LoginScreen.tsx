import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import { RootStackParamList } from '../navigation/AppNavigator';
import { WiproColors, WiproBorderRadius, WiproShadow } from '../theme/WiproTheme';

type LoginScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Login'>;
};

const LoginScreen: React.FC<LoginScreenProps> = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login, isLoading } = useAuth();

  const handleLogin = async () => {
    if (!email || !password) {
      setError('Please enter email and password');
      return;
    }

    try {
      setError('');
      console.log('Attempting login with:', email);
      await login(email, password);
      console.log('Login successful');
    } catch (err: any) {
      console.error('Login error in screen:', err);
      setError(err.response?.data?.message || err.message || 'Login failed');
    }
  };

  return (
    <KeyboardAvoidingView 
      style={styles.container} 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView 
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.formWrapper}>
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Welcome Back</Text>
            <Text style={styles.subtitle}>Sign in to your account</Text>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>Email</Text>
              <TextInput
                style={styles.input}
                placeholder="Enter your email"
                placeholderTextColor={WiproColors.gray[400]}
                value={email}
                onChangeText={setEmail}
                keyboardType="email-address"
                autoCapitalize="none"
              />
            </View>

            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>Password</Text>
              <TextInput
                style={styles.input}
                placeholder="Enter your password"
                placeholderTextColor={WiproColors.gray[400]}
                value={password}
                onChangeText={setPassword}
                secureTextEntry
              />
            </View>

            <TouchableOpacity style={styles.button} onPress={handleLogin} disabled={isLoading}>
              {isLoading ? (
                <ActivityIndicator color={WiproColors.white} />
              ) : (
                <Text style={styles.buttonText}>Sign In</Text>
              )}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => navigation.navigate('Register')}>
              <Text style={styles.link}>Don't have an account? <Text style={styles.linkBold}>Register</Text></Text>
            </TouchableOpacity>
          </View>

          <Text style={styles.hint}>Demo: admin@oms.com / admin123</Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: WiproColors.background,
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 20,
  },
  formWrapper: {
    width: '100%',
    maxWidth: 450,
    alignSelf: 'center',
  },
  card: {
    backgroundColor: WiproColors.white,
    borderRadius: WiproBorderRadius.xl,
    padding: 32,
    ...WiproShadow.card,
  },
  cardTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: WiproColors.black,
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    textAlign: 'center',
    marginBottom: 24,
    color: WiproColors.gray[500],
  },
  inputContainer: {
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: WiproColors.gray[700],
    marginBottom: 8,
  },
  input: {
    backgroundColor: WiproColors.white,
    borderWidth: 1,
    borderColor: WiproColors.gray[300],
    borderRadius: WiproBorderRadius.md,
    padding: 14,
    fontSize: 16,
    color: WiproColors.black,
  },
  button: {
    backgroundColor: WiproColors.primary,
    padding: 16,
    borderRadius: WiproBorderRadius.full,
    alignItems: 'center',
    marginTop: 8,
    ...WiproShadow.medium,
  },
  buttonText: {
    color: WiproColors.white,
    fontSize: 16,
    fontWeight: '600',
  },
  link: {
    color: WiproColors.gray[600],
    textAlign: 'center',
    marginTop: 20,
    fontSize: 14,
  },
  linkBold: {
    color: WiproColors.primary,
    fontWeight: '600',
  },
  error: {
    color: WiproColors.error,
    textAlign: 'center',
    marginBottom: 16,
    fontSize: 14,
    backgroundColor: '#FEF2F2',
    padding: 10,
    borderRadius: WiproBorderRadius.sm,
  },
  hint: {
    color: WiproColors.gray[400],
    textAlign: 'center',
    marginTop: 24,
    fontSize: 12,
  },
});

export default LoginScreen;
