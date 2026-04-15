import axios, { AxiosInstance, AxiosError } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';
console.log('API Base URL:', API_BASE_URL);

class ApiService {
  private client: AxiosInstance;
  private token: string | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    });

    this.client.interceptors.request.use((config) => {
      if (this.token) {
        config.headers.Authorization = `Bearer ${this.token}`;
      }
      return config;
    });

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          this.token = null;
        }
        return Promise.reject(error);
      }
    );
  }

  setToken(token: string | null) {
    this.token = token;
  }

  async register(email: string, password: string, name: string) {
    const response = await this.client.post('/auth/register', { email, password, name });
    return response.data;
  }

  async login(email: string, password: string) {
    const response = await this.client.post('/auth/login', { email, password });
    if (response.data.data?.accessToken) {
      this.token = response.data.data.accessToken;
    }
    return response.data;
  }

  async getProducts(page = 0, size = 20) {
    const response = await this.client.get('/products', { params: { page, size } });
    return response.data;
  }

  async getProduct(id: string) {
    const response = await this.client.get(`/products/${id}`);
    return response.data;
  }

  async createOrder(items: Array<{ productId: string; quantity: number }>, shippingAddress: any) {
    const response = await this.client.post('/orders', { items, shippingAddress });
    return response.data;
  }

  async getOrders(page = 0, size = 20) {
    const response = await this.client.get('/orders', { params: { page, size } });
    return response.data;
  }

  async getOrder(id: string) {
    const response = await this.client.get(`/orders/${id}`);
    return response.data;
  }

  async cancelOrder(id: string) {
    const response = await this.client.post(`/orders/${id}/cancel`);
    return response.data;
  }

  async payOrder(id: string, paymentMethod = 'CREDIT_CARD') {
    const response = await this.client.post(`/orders/${id}/pay`, { paymentMethod });
    return response.data;
  }
}

export const api = new ApiService();
export default api;
