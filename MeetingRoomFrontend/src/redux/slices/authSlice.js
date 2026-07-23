import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axiosClient from '../../api/axiosClient';

const initialToken = localStorage.getItem('token') || null;
const initialUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await axiosClient.post('/auth/login', credentials);
      const data = response.data.data;
      localStorage.setItem('token', data.accessToken);
      localStorage.setItem('user', JSON.stringify({
        userId: data.userId,
        email: data.email,
        fullName: data.fullName,
        roles: data.roles
      }));
      return data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed. Invalid credentials.';
      return rejectWithValue(msg);
    }
  }
);

export const registerUser = createAsyncThunk(
  'auth/registerUser',
  async (userData, { rejectWithValue }) => {
    try {
      const response = await axiosClient.post('/users/register', userData);
      return response.data.message;
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed.';
      return rejectWithValue(msg);
    }
  }
);

export const verifyOtp = createAsyncThunk(
  'auth/verifyOtp',
  async ({ email, otp }, { rejectWithValue }) => {
    try {
      const response = await axiosClient.post(`/users/verify-otp?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`);
      return response.data.message;
    } catch (err) {
      const msg = err.response?.data?.message || 'OTP verification failed.';
      return rejectWithValue(msg);
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token: initialToken,
    user: initialUser,
    loading: false,
    error: null,
    registerSuccess: false,
  },
  reducers: {
    logout: (state) => {
      state.token = null;
      state.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    clearAuthError: (state) => {
      state.error = null;
      state.registerSuccess = false;
    }
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.accessToken;
        state.user = {
          userId: action.payload.userId,
          email: action.payload.email,
          fullName: action.payload.fullName,
          roles: action.payload.roles
        };
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Register
      .addCase(registerUser.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.registerSuccess = false;
      })
      .addCase(registerUser.fulfilled, (state) => {
        state.loading = false;
        state.registerSuccess = true;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Verify OTP
      .addCase(verifyOtp.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyOtp.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(verifyOtp.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { logout, clearAuthError } = authSlice.actions;
export default authSlice.reducer;
