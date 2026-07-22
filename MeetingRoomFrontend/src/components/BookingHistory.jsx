import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUserUpcomingBookings, cancelBooking } from '../redux/slices/bookingSlice';
import { CalendarCheck, Calendar, Clock, Building, User, Trash2, Loader2, Search, AlertTriangle, X, Info } from 'lucide-react';
import './BookingHistory.css';

const BookingHistory = () => {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { userBookings, loading } = useSelector((state) => state.bookings);

  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [employeeIdentifier, setEmployeeIdentifier] = useState('');
  
  // Cancellation Modal States
  const [cancellingBooking, setCancellingBooking] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelReasonError, setCancelReasonError] = useState('');

  const isSuperOrAdmin = user?.roles?.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN');

  useEffect(() => {
    const defaultSearch = user?.fullName || user?.email || 'employee';
    setEmployeeIdentifier(defaultSearch);
    dispatch(fetchUserUpcomingBookings(defaultSearch));
  }, [dispatch, user]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (employeeIdentifier.trim()) {
      dispatch(fetchUserUpcomingBookings(employeeIdentifier.trim()));
    }
  };

  const handleCancelClick = (booking) => {
    setCancellingBooking(booking);
    setCancelReason('');
    setCancelReasonError('');
  };

  const handleConfirmCancel = async () => {
    if (!cancelReason.trim()) {
      setCancelReasonError('A valid cancellation reason is required');
      return;
    }
    const result = await dispatch(cancelBooking({ bookingId: cancellingBooking.id, reason: cancelReason }));
    if (cancelBooking.fulfilled.match(result)) {
      setCancellingBooking(null);
    }
  };

  const filteredBookings = userBookings.filter((b) => {
    if (statusFilter !== 'ALL' && b.status !== statusFilter) {
      return false;
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = b.meetingTitle?.toLowerCase().includes(q);
      const matchRoom = b.roomName?.toLowerCase().includes(q);
      const matchEmp = b.employeeName?.toLowerCase().includes(q);
      const matchId = String(b.id).includes(q);
      return matchTitle || matchRoom || matchEmp || matchId;
    }
    return true;
  });

  const getStatusBadge = (status) => {
    switch (status) {
      case 'UPCOMING':
        return <span className="status-badge status-upcoming">Upcoming</span>;
      case 'ONGOING':
        return <span className="status-badge status-ongoing">Ongoing</span>;
      case 'COMPLETED':
        return <span className="status-badge status-completed">Completed</span>;
      case 'CANCELLED':
        return <span className="status-badge status-cancelled">Cancelled</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  return (
    <section className="history-section">
      <div className="history-header">
        <h2 className="history-title">Meeting Room Reservation History</h2>
        <p className="history-subtitle">
          {isSuperOrAdmin 
            ? 'Search and manage booking reservations across all employee accounts.'
            : 'View and manage your upcoming huddle and conference room reservations.'}
        </p>
      </div>

      {/* Filter & Search Bar */}
      <div className="history-filter-bar glass-panel">
        {/* Only Admin / Super Admin can filter by other employee accounts */}
        {isSuperOrAdmin ? (
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div className="history-search-input">
              <User size={16} style={{ color: 'var(--accent-blue)' }} />
              <input
                type="text"
                placeholder="Search Employee name..."
                value={employeeIdentifier}
                onChange={(e) => setEmployeeIdentifier(e.target.value)}
              />
            </div>
            <button type="submit" className="filter-tab-btn active" style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              <Search size={14} /> Fetch
            </button>
          </form>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)', fontSize: '0.88rem', fontWeight: 700 }}>
            <User size={16} style={{ color: 'var(--accent-blue)' }} />
            <span>Reservations for {user?.fullName}</span>
          </div>
        )}

        <div className="history-search-input" style={{ width: '220px' }}>
          <Search size={16} style={{ color: 'var(--text-muted)' }} />
          <input
            type="text"
            placeholder="Search title, room, ID..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="filter-tabs">
          {['ALL', 'UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              className={`filter-tab-btn ${statusFilter === st ? 'active' : ''}`}
              onClick={() => setStatusFilter(st)}
            >
              {st === 'ALL' ? 'All' : st.charAt(0) + st.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {loading && userBookings.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem' }}>
          <Loader2 className="spinner-icon" size={36} style={{ color: 'var(--accent-blue)' }} />
          <p style={{ marginTop: '0.75rem', color: 'var(--text-secondary)' }}>Fetching reservation history...</p>
        </div>
      ) : filteredBookings.length > 0 ? (
        <div className="history-grid">
          {filteredBookings.map((b) => (
            <div key={b.id} className="history-card glass-panel">
              <div className="history-card-header">
                <div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--accent-blue)', fontWeight: 800 }}>Booking #{b.id}</div>
                  <h3 className="history-meeting-title">{b.meetingTitle}</h3>
                </div>
                {getStatusBadge(b.status)}
              </div>

              <div className="history-details">
                <div className="history-detail-row">
                  <Building size={14} style={{ color: 'var(--accent-blue)' }} />
                  <span>{b.roomName}</span>
                </div>
                <div className="history-detail-row">
                  <Calendar size={14} style={{ color: 'var(--accent-orange)' }} />
                  <span>{b.bookingDate}</span>
                </div>
                <div className="history-detail-row">
                  <Clock size={14} style={{ color: 'var(--accent-blue)' }} />
                  <span>{b.startTime.slice(0, 5)} - {b.endTime.slice(0, 5)}</span>
                </div>
                <div className="history-detail-row">
                  <User size={14} />
                  <span>Reserved by: {b.employeeName}</span>
                </div>
              </div>

              {b.status === 'CANCELLED' && b.cancellationReason && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--accent-rose)', fontSize: '0.8rem', fontWeight: 600, background: '#fef2f2', padding: '0.5rem 0.75rem', borderRadius: 'var(--radius-md)', border: '1px solid #fee2e2' }}>
                  <Info size={14} /> Reason: {b.cancellationReason}
                </div>
              )}

              {(b.status === 'UPCOMING' || b.status === 'ONGOING') && (
                <button
                  className="btn-cancel-booking"
                  onClick={() => handleCancelClick(b)}
                >
                  <Trash2 size={14} />
                  Cancel Reservation
                </button>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="empty-state glass-panel">
          <div className="empty-state-icon">
            <CalendarCheck size={32} />
          </div>
          <h3 className="empty-state-title">No Matching Reservations Found</h3>
          <p className="empty-state-subtitle">
            No bookings match your current filter or search criteria.
          </p>
        </div>
      )}

      {/* Cancellation Reason Modal Popup */}
      {cancellingBooking && (
        <div className="modal-overlay">
          <div className="modal-card" style={{ maxWidth: '440px' }}>
            <div className="modal-header">
              <h3 className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-rose)' }}>
                <AlertTriangle size={18} /> Cancel Meeting Booking
              </h3>
              <button className="btn-close-modal" onClick={() => setCancellingBooking(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                You are cancelling your booking for <strong>"{cancellingBooking.meetingTitle}"</strong>.
              </p>
              <div className="form-group" style={{ marginTop: '0.5rem' }}>
                <label className="form-label" style={{ fontWeight: 700 }}>Cancellation Reason *</label>
                <textarea
                  className="form-control"
                  style={{ minHeight: '80px', padding: '0.75rem', fontFamily: 'inherit' }}
                  placeholder="e.g. Relocated to another office space, schedule conflict..."
                  value={cancelReason}
                  onChange={(e) => { setCancelReason(e.target.value); setCancelReasonError(''); }}
                />
                {cancelReasonError && <span className="form-error">{cancelReasonError}</span>}
              </div>
              <button 
                type="button" 
                className="btn-confirm-booking" 
                style={{ background: 'var(--accent-rose)', color: '#fff', boxShadow: 'none' }}
                onClick={handleConfirmCancel}
              >
                Confirm Cancellation
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default BookingHistory;
