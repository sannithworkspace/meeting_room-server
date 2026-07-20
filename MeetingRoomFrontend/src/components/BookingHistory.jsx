import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUserUpcomingBookings, cancelBooking } from '../redux/slices/bookingSlice';
import { CalendarCheck, Calendar, Clock, Building, User, Trash2, Loader2, Search, Filter } from 'lucide-react';
import './BookingHistory.css';

const BookingHistory = () => {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { userBookings, loading } = useSelector((state) => state.bookings);

  const [cancellingId, setCancellingId] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [employeeIdentifier, setEmployeeIdentifier] = useState('');

  useEffect(() => {
    const defaultSearch = user?.fullName || user?.email || 'admin';
    setEmployeeIdentifier(defaultSearch);
    dispatch(fetchUserUpcomingBookings(defaultSearch));
  }, [dispatch, user]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (employeeIdentifier.trim()) {
      dispatch(fetchUserUpcomingBookings(employeeIdentifier.trim()));
    }
  };

  const handleCancel = async (id) => {
    setCancellingId(id);
    await dispatch(cancelBooking(id));
    setCancellingId(null);
  };

  const filteredBookings = userBookings.filter((b) => {
    // Status filter
    if (statusFilter !== 'ALL' && b.status !== statusFilter) {
      return false;
    }
    // Search query filter (title, room, employee)
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
          View your upcoming and past meeting room reservations, search by employee, and manage cancellations.
        </p>
      </div>

      {/* Filter & Search Bar */}
      <div className="history-filter-bar glass-panel">
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
          <div className="history-search-input">
            <User size={16} style={{ color: 'var(--accent-cyan)' }} />
            <input
              type="text"
              placeholder="Filter by Employee Name / Email..."
              value={employeeIdentifier}
              onChange={(e) => setEmployeeIdentifier(e.target.value)}
            />
          </div>
          <button type="submit" className="filter-tab-btn active" style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Search size={14} /> Fetch
          </button>
        </form>

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
          <Loader2 className="spinner-icon" size={36} style={{ color: 'var(--accent-cyan)' }} />
          <p style={{ marginTop: '0.75rem', color: 'var(--text-secondary)' }}>Fetching reservation history...</p>
        </div>
      ) : filteredBookings.length > 0 ? (
        <div className="history-grid">
          {filteredBookings.map((b) => (
            <div key={b.id} className="history-card glass-panel">
              <div className="history-card-header">
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--accent-cyan)', fontWeight: 700 }}>Booking #{b.id}</div>
                  <h3 className="history-meeting-title">{b.meetingTitle}</h3>
                </div>
                {getStatusBadge(b.status)}
              </div>

              <div className="history-details">
                <div className="history-detail-row">
                  <Building size={14} style={{ color: 'var(--accent-cyan)' }} />
                  <span>{b.roomName}</span>
                </div>
                <div className="history-detail-row">
                  <Calendar size={14} style={{ color: 'var(--accent-violet)' }} />
                  <span>{b.bookingDate}</span>
                </div>
                <div className="history-detail-row">
                  <Clock size={14} style={{ color: 'var(--accent-cyan)' }} />
                  <span>{b.startTime} - {b.endTime}</span>
                </div>
                <div className="history-detail-row">
                  <User size={14} />
                  <span>Reserved by: {b.employeeName} ({b.employeeEmail || 'N/A'})</span>
                </div>
              </div>

              {b.status === 'UPCOMING' && (
                <button
                  className="btn-cancel-booking"
                  onClick={() => handleCancel(b.id)}
                  disabled={cancellingId === b.id}
                >
                  {cancellingId === b.id ? (
                    <>
                      <Loader2 className="spinner-icon" size={14} />
                      Cancelling...
                    </>
                  ) : (
                    <>
                      <Trash2 size={14} />
                      Cancel Booking
                    </>
                  )}
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
            No bookings match your current filter or search criteria. Try changing the employee name, status filter, or search term.
          </p>
        </div>
      )}
    </section>
  );
};

export default BookingHistory;
