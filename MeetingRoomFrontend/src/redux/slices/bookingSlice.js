import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axiosClient from '../../api/axiosClient';

export const createBooking = createAsyncThunk(
  'bookings/createBooking',
  async (bookingData, { rejectWithValue }) => {
    try {
      const response = await axiosClient.post('/bookings', bookingData);
      return response.data.data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create booking reservation.';
      return rejectWithValue(msg);
    }
  }
);

export const fetchUserUpcomingBookings = createAsyncThunk(
  'bookings/fetchUserUpcomingBookings',
  async (employeeName, { rejectWithValue }) => {
    try {
      const response = await axiosClient.get(`/bookings/employee/${encodeURIComponent(employeeName)}/upcoming?page=0&size=50`);
      return response.data.data.content || [];
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to fetch user bookings');
    }
  }
);

export const cancelBooking = createAsyncThunk(
  'bookings/cancelBooking',
  async (bookingId, { rejectWithValue, dispatch, getState }) => {
    try {
      const response = await axiosClient.put(`/bookings/${bookingId}/cancel`);
      const user = getState().auth.user;
      if (user?.fullName) {
        dispatch(fetchUserUpcomingBookings(user.fullName));
      }
      return response.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to cancel booking');
    }
  }
);

const bookingSlice = createSlice({
  name: 'bookings',
  initialState: {
    userBookings: [],
    lastConfirmedBooking: null,
    loading: false,
    error: null,
    bookingSuccess: false
  },
  reducers: {
    clearLastConfirmedBooking: (state) => {
      state.lastConfirmedBooking = null;
      state.bookingSuccess = false;
    },
    clearBookingError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Create Booking
      .addCase(createBooking.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.bookingSuccess = false;
      })
      .addCase(createBooking.fulfilled, (state, action) => {
        state.loading = false;
        state.lastConfirmedBooking = action.payload;
        state.bookingSuccess = true;
      })
      .addCase(createBooking.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // User Upcoming Bookings
      .addCase(fetchUserUpcomingBookings.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchUserUpcomingBookings.fulfilled, (state, action) => {
        state.loading = false;
        state.userBookings = action.payload;
      })
      .addCase(fetchUserUpcomingBookings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Cancel Booking
      .addCase(cancelBooking.pending, (state) => {
        state.loading = true;
      })
      .addCase(cancelBooking.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(cancelBooking.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearLastConfirmedBooking, clearBookingError } = bookingSlice.actions;
export default bookingSlice.reducer;
