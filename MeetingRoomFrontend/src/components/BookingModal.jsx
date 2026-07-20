import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { motion, AnimatePresence } from 'framer-motion';
import { createBooking, clearBookingError } from '../redux/slices/bookingSlice';
import { X, Calendar, Clock, User, Mail, Users, FileText, CheckCircle2, Loader2 } from 'lucide-react';
import './BookingModal.css';

const validationSchema = Yup.object().shape({
  meetingTitle: Yup.string().required('Meeting title is required').min(3, 'At least 3 characters'),
  employeeName: Yup.string().required('Employee name is required'),
  employeeEmail: Yup.string().email('Invalid email').nullable(),
  bookingDate: Yup.string().required('Booking date is required'),
  startTime: Yup.string().required('Start time is required'),
  endTime: Yup.string()
    .required('End time is required')
    .test('is-after', 'End time must be after start time', function(endTime) {
      const { startTime } = this.parent;
      if (!startTime || !endTime) return true;
      return endTime > startTime;
    }),
  numberOfParticipants: Yup.number()
    .required('Number of participants is required')
    .positive('Must be positive')
    .integer('Must be whole number'),
  meetingDescription: Yup.string().max(500, 'Max 500 characters')
});

const BookingModal = ({ room, onClose, onOpenAuth }) => {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { searchParams } = useSelector((state) => state.rooms);
  const { loading, error } = useSelector((state) => state.bookings);

  const todayStr = new Date().toISOString().split('T')[0];

  const formik = useFormik({
    initialValues: {
      meetingTitle: '',
      employeeName: user?.fullName || '',
      employeeEmail: user?.email || '',
      bookingDate: searchParams.date || todayStr,
      startTime: searchParams.startTime || '10:00:00',
      endTime: searchParams.endTime || '11:00:00',
      numberOfParticipants: searchParams.capacity || 2,
      meetingDescription: ''
    },
    validationSchema,
    onSubmit: (values) => {
      if (!user) {
        onOpenAuth();
        return;
      }

      const formattedStartTime = values.startTime.length === 5 ? `${values.startTime}:00` : values.startTime;
      const formattedEndTime = values.endTime.length === 5 ? `${values.endTime}:00` : values.endTime;

      const payload = {
        meetingTitle: values.meetingTitle,
        employeeName: values.employeeName,
        employeeEmail: values.employeeEmail,
        roomId: room.id,
        bookingDate: values.bookingDate,
        startTime: formattedStartTime,
        endTime: formattedEndTime,
        numberOfParticipants: parseInt(values.numberOfParticipants, 10),
        meetingDescription: values.meetingDescription
      };

      dispatch(clearBookingError());
      dispatch(createBooking(payload));
    }
  });

  if (!room) return null;

  return (
    <AnimatePresence>
      <div className="modal-overlay">
        <motion.div
          className="modal-card glass-panel"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ duration: 0.25 }}
        >
          <div className="modal-header">
            <h3 className="modal-title">Reserve Meeting Room</h3>
            <button className="btn-close-modal" onClick={onClose}>
              <X size={20} />
            </button>
          </div>

          <form onSubmit={formik.handleSubmit}>
            <div className="modal-body">
              {/* Room Banner */}
              <div className="room-summary-banner">
                <div>
                  <div className="room-summary-name">{room.roomName}</div>
                  <div className="room-summary-meta">Floor {room.floorNumber} • Max Capacity: {room.seatingCapacity} Seats</div>
                </div>
                <div style={{ color: 'var(--accent-cyan)', fontWeight: 700, fontSize: '0.9rem' }}>
                  Available
                </div>
              </div>

              {error && <div className="modal-error-alert">{error}</div>}

              {/* Meeting Title */}
              <div className="form-group">
                <label className="form-label"><FileText size={14} /> Meeting Title *</label>
                <input
                  type="text"
                  name="meetingTitle"
                  placeholder="e.g. Q3 Architecture Review"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={formik.values.meetingTitle}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                />
                {formik.touched.meetingTitle && formik.errors.meetingTitle && (
                  <span className="form-error">{formik.errors.meetingTitle}</span>
                )}
              </div>

              {/* Employee Name & Email */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label"><User size={14} /> Employee Name *</label>
                  <input
                    type="text"
                    name="employeeName"
                    className="form-control"
                    style={{ paddingLeft: '1rem' }}
                    value={formik.values.employeeName}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                  {formik.touched.employeeName && formik.errors.employeeName && (
                    <span className="form-error">{formik.errors.employeeName}</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label"><Mail size={14} /> Email</label>
                  <input
                    type="email"
                    name="employeeEmail"
                    className="form-control"
                    style={{ paddingLeft: '1rem' }}
                    value={formik.values.employeeEmail}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                </div>
              </div>

              {/* Date & Time Slot */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label"><Calendar size={14} /> Date</label>
                  <input
                    type="date"
                    name="bookingDate"
                    min={todayStr}
                    className="form-control"
                    style={{ paddingLeft: '0.75rem', paddingRight: '0.5rem' }}
                    value={formik.values.bookingDate}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label"><Clock size={14} /> Start Time</label>
                  <input
                    type="time"
                    name="startTime"
                    step="1800"
                    className="form-control"
                    style={{ paddingLeft: '0.75rem', paddingRight: '0.5rem' }}
                    value={formik.values.startTime}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label"><Clock size={14} /> End Time</label>
                  <input
                    type="time"
                    name="endTime"
                    step="1800"
                    className="form-control"
                    style={{ paddingLeft: '0.75rem', paddingRight: '0.5rem' }}
                    value={formik.values.endTime}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                </div>
              </div>

              {/* Participants */}
              <div className="form-group">
                <label className="form-label"><Users size={14} /> Number of Participants *</label>
                <input
                  type="number"
                  name="numberOfParticipants"
                  max={room.seatingCapacity}
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={formik.values.numberOfParticipants}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                />
                {formik.touched.numberOfParticipants && formik.errors.numberOfParticipants && (
                  <span className="form-error">{formik.errors.numberOfParticipants}</span>
                )}
              </div>

              {/* Confirm Booking CTA */}
              <button type="submit" className="btn-confirm-booking" disabled={loading}>
                {loading ? (
                  <>
                    <Loader2 className="spinner-icon" size={20} />
                    Reserving Room...
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={20} />
                    Confirm Reservation
                  </>
                )}
              </button>
            </div>
          </form>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default BookingModal;
