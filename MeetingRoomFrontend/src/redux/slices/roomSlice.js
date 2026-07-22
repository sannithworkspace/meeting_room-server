import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axiosClient from '../../api/axiosClient';

export const fetchAllRooms = createAsyncThunk(
  'rooms/fetchAllRooms',
  async (_, { rejectWithValue }) => {
    try {
      const response = await axiosClient.get('/rooms?page=0&size=100');
      return response.data.data.content || [];
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to fetch rooms');
    }
  }
);

export const searchAvailableRooms = createAsyncThunk(
  'rooms/searchAvailableRooms',
  async ({ date, startTime, endTime, capacity }, { rejectWithValue }) => {
    try {
      let url = `/bookings/available-rooms?date=${date}&startTime=${startTime}&endTime=${endTime}`;
      if (capacity) {
        url += `&requiredCapacity=${capacity}`;
      }
      const response = await axiosClient.get(url);
      return response.data.data || [];
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to search available rooms');
    }
  }
);

export const createRoom = createAsyncThunk(
  'rooms/createRoom',
  async (roomData, { rejectWithValue, dispatch }) => {
    try {
      const response = await axiosClient.post('/rooms', roomData);
      dispatch(fetchAllRooms());
      return response.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to create room');
    }
  }
);

export const updateRoom = createAsyncThunk(
  'rooms/updateRoom',
  async ({ roomId, roomData }, { rejectWithValue, dispatch }) => {
    try {
      const response = await axiosClient.put(`/rooms/${roomId}`, roomData);
      dispatch(fetchAllRooms());
      return response.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to update room');
    }
  }
);

export const deleteRoom = createAsyncThunk(
  'rooms/deleteRoom',
  async (roomId, { rejectWithValue, dispatch }) => {
    try {
      await axiosClient.delete(`/rooms/${roomId}`);
      dispatch(fetchAllRooms());
      return roomId;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to delete room');
    }
  }
);

const roomSlice = createSlice({
  name: 'rooms',
  initialState: {
    allRooms: [],
    availableRooms: [],
    searched: false,
    loading: false,
    error: null,
    searchParams: {
      date: '',
      startTime: '',
      endTime: '',
      capacity: ''
    }
  },
  reducers: {
    setSearchParams: (state, action) => {
      state.searchParams = { ...state.searchParams, ...action.payload };
    },
    resetSearch: (state) => {
      state.availableRooms = [];
      state.searched = false;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch All Rooms
      .addCase(fetchAllRooms.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchAllRooms.fulfilled, (state, action) => {
        state.loading = false;
        state.allRooms = action.payload;
      })
      .addCase(fetchAllRooms.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Search Available Rooms
      .addCase(searchAvailableRooms.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchAvailableRooms.fulfilled, (state, action) => {
        state.loading = false;
        state.availableRooms = action.payload;
        state.searched = true;
      })
      .addCase(searchAvailableRooms.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { setSearchParams, resetSearch } = roomSlice.actions;
export default roomSlice.reducer;
