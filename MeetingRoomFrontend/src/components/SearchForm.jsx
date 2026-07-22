import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { searchAvailableRooms, setSearchParams } from '../redux/slices/roomSlice';
import { Calendar, Clock, Users, Search, Loader2 } from 'lucide-react';
import './SearchForm.css';

const validationSchema = Yup.object().shape({
  date: Yup.string()
    .required('Date is required')
    .test('not-past-date', 'Date cannot be in the past', (val) => {
      if (!val) return true;
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // Correct for timezone offset when parsing yyyy-mm-dd
      const [year, month, day] = val.split('-').map(Number);
      const selected = new Date(year, month - 1, day);
      selected.setHours(0, 0, 0, 0);
      
      return selected >= today;
    }),
  startTime: Yup.string()
    .required('Start time is required')
    .test('not-past-time', 'Start time cannot be in the past', function(startTime) {
      const { date } = this.parent;
      if (!date || !startTime) return true;
      
      const todayLocalStr = new Date().toLocaleDateString('en-CA'); // yyyy-mm-dd format in local time
      if (date === todayLocalStr) {
        const now = new Date();
        const [hours, minutes] = startTime.split(':').map(Number);
        const selectedTime = new Date();
        selectedTime.setHours(hours, minutes, 0, 0);
        return selectedTime > now;
      }
      return true;
    }),
  endTime: Yup.string()
    .required('End time is required')
    .test('is-after', 'End time must be after start time', function(endTime) {
      const { startTime } = this.parent;
      if (!startTime || !endTime) return true;
      return endTime > startTime;
    }),
  capacity: Yup.number().positive('Must be positive').nullable()
});

const SearchForm = () => {
  const dispatch = useDispatch();
  const { loading, searchParams } = useSelector((state) => state.rooms);

  const todayStr = new Date().toLocaleDateString('en-CA');

  const formik = useFormik({
    initialValues: {
      date: searchParams.date || todayStr,
      startTime: searchParams.startTime || '10:00',
      endTime: searchParams.endTime || '11:00',
      capacity: searchParams.capacity || ''
    },
    validationSchema,
    onSubmit: (values) => {
      // Format time strings to standard HH:mm:ss for backend API compatibility
      const formattedStartTime = values.startTime.length === 5 ? `${values.startTime}:00` : values.startTime;
      const formattedEndTime = values.endTime.length === 5 ? `${values.endTime}:00` : values.endTime;

      const payload = {
        date: values.date,
        startTime: formattedStartTime,
        endTime: formattedEndTime,
        capacity: values.capacity ? parseInt(values.capacity, 10) : null
      };

      dispatch(setSearchParams(payload));
      dispatch(searchAvailableRooms(payload));
    }
  });

  return (
    <form className="search-form-card glass-panel" onSubmit={formik.handleSubmit}>
      <div className="search-form-grid">
        {/* Date Field */}
        <div className="form-group">
          <label className="form-label">
            <Calendar size={14} /> Date
          </label>
          <div className="form-input-wrapper">
            <Calendar className="form-input-icon" size={16} />
            <input
              type="date"
              name="date"
              min={todayStr}
              className="form-control"
              value={formik.values.date}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          {formik.touched.date && formik.errors.date && (
            <span className="form-error">{formik.errors.date}</span>
          )}
        </div>

        {/* Start Time */}
        <div className="form-group">
          <label className="form-label">
            <Clock size={14} /> Start Time
          </label>
          <div className="form-input-wrapper">
            <Clock className="form-input-icon" size={16} />
            <input
              type="time"
              name="startTime"
              step="1800"
              className="form-control"
              value={formik.values.startTime}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          {formik.touched.startTime && formik.errors.startTime && (
            <span className="form-error">{formik.errors.startTime}</span>
          )}
        </div>

        {/* End Time */}
        <div className="form-group">
          <label className="form-label">
            <Clock size={14} /> End Time
          </label>
          <div className="form-input-wrapper">
            <Clock className="form-input-icon" size={16} />
            <input
              type="time"
              name="endTime"
              step="1800"
              className="form-control"
              value={formik.values.endTime}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          {formik.touched.endTime && formik.errors.endTime && (
            <span className="form-error">{formik.errors.endTime}</span>
          )}
        </div>

        {/* Capacity */}
        <div className="form-group">
          <label className="form-label">
            <Users size={14} /> Participants
          </label>
          <div className="form-input-wrapper">
            <Users className="form-input-icon" size={16} />
            <input
              type="number"
              name="capacity"
              placeholder="Min capacity (e.g. 6)"
              className="form-control"
              value={formik.values.capacity}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          {formik.touched.capacity && formik.errors.capacity && (
            <span className="form-error">{formik.errors.capacity}</span>
          )}
        </div>

        {/* Submit Search Button */}
        <div className="form-group">
          <button type="submit" className="btn-search" disabled={loading}>
            {loading ? (
              <>
                <Loader2 className="spinner-icon" size={18} />
                Checking...
              </>
            ) : (
              <>
                <Search size={18} />
                Find Available Rooms
              </>
            )}
          </button>
        </div>
      </div>
    </form>
  );
};

export default SearchForm;
