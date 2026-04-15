import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import { RootStackParamList } from '../navigation/AppNavigator';
import { WiproColors, WiproBorderRadius, WiproShadow } from '../theme/WiproTheme';

type RegisterScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Register'>;
};

const RegisterScreen: React.FC<RegisterScreenProps> = ({ navigation }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { register, isLoading } = useAuth();

  const handleRegister = async () => {
    if (!name || !email || !password) {
      setError('Please fill all fields');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    try {
      setError('');
      await register(email, password, name);
      Alert.alert('Success', 'Account created! Please login.');
      navigation.navigate('Login');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
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
            <Text style={styles.cardTitle}>Create Account</Text>
            <Text style={styles.subtitle}>Join the Order Management System</Text>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>Full Name</Text>
              <TextInput
                style={styles.input}
                placeholder="Enter your full name"
                placeholderTextColor={WiproColors.gray[400]}
                value={name}
                onChangeText={setName}
              />
            </View>

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
                placeholder="Min 8 characters"
                placeholderTextColor={WiproColors.gray[400]}
                value={password}
                onChangeText={setPassword}
                secureTextEntry
              />
            </View>

            <TouchableOpacity style={styles.button} onPress={handleRegister} disabled={isLoading}>
              {isLoading ? (
                <ActivityIndicator color={WiproColors.white} />
              ) : (
                <Text style={styles.buttonText}>Create Account</Text>
              )}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => navigation.goBack()}>
              <Text style={styles.link}>Already have an account? <Text style={styles.linkBold}>Sign In</Text></Text>
            </TouchableOpacity>
          </View>
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
});

export default RegisterScreen;
