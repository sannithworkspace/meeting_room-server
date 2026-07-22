import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { createRoom, updateRoom, deleteRoom, fetchAllRooms } from '../redux/slices/roomSlice';
import { fetchAllSystemBookings, fetchBookingMetrics, cancelBooking } from '../redux/slices/bookingSlice';
import { ShieldCheck, Plus, Edit, Trash2, Building, Users, Image as ImageIcon, Loader2, Calendar, PieChart, Download, AlertTriangle, X, Info } from 'lucide-react';
import axiosClient from '../api/axiosClient';
import './AdminDashboard.css';

const roomSchema = Yup.object().shape({
  roomName: Yup.string().required('Room name is required').min(2, 'Min 2 chars'),
  floorNumber: Yup.number().required('Floor number is required').integer('Integer required'),
  seatingCapacity: Yup.number().required('Capacity is required').positive('Must be positive').integer('Integer required'),
  imageUrls: Yup.string().nullable()
});

const AdminDashboard = () => {
  const dispatch = useDispatch();
  const { allRooms, loading: roomLoading, error: roomError } = useSelector((state) => state.rooms);
  const { systemBookings, metrics, loading: bookingLoading, error: bookingError } = useSelector((state) => state.bookings);

  const [adminTab, setAdminTab] = useState('rooms'); // 'rooms' | 'bookings' | 'reports'
  const [editingRoomId, setEditingRoomId] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  
  // Modal states for cancellation
  const [cancellingBooking, setCancellingBooking] = useState(null); // booking object
  const [cancelReason, setCancelReason] = useState('');
  const [cancelReasonError, setCancelReasonError] = useState('');

  // Booking filters
  const [bookingFilterStatus, setBookingFilterStatus] = useState('ALL');

  useEffect(() => {
    dispatch(fetchAllRooms());
    dispatch(fetchAllSystemBookings());
    dispatch(fetchBookingMetrics());
  }, [dispatch]);

  const formik = useFormik({
    initialValues: {
      roomName: '',
      floorNumber: 1,
      seatingCapacity: 10,
      facilities: ['PROJECTOR', 'WHITEBOARD', 'VIDEO_CONFERENCE', 'AIR_CONDITIONING'],
      imageUrls: ''
    },
    validationSchema: roomSchema,
    onSubmit: async (values, { resetForm }) => {
      const roomData = {
        roomName: values.roomName,
        floorNumber: parseInt(values.floorNumber, 10),
        seatingCapacity: parseInt(values.seatingCapacity, 10),
        availableFacilities: values.facilities,
        imageUrls: values.imageUrls ? [values.imageUrls] : []
      };

      setUploadingImage(true);
      try {
        if (editingRoomId) {
          const result = await dispatch(updateRoom({ roomId: editingRoomId, roomData }));
          if (updateRoom.fulfilled.match(result)) {
            // Upload to S3 if a file was selected
            if (selectedFile) {
              const formData = new FormData();
              formData.append('file', selectedFile);
              await axiosClient.post(`/rooms/${editingRoomId}/image`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
              });
            }
            setEditingRoomId(null);
            setSelectedFile(null);
            const fileInput = document.getElementById('room-image-upload-input');
            if (fileInput) fileInput.value = '';
            resetForm();
            dispatch(fetchAllRooms());
          }
        } else {
          const result = await dispatch(createRoom(roomData));
          if (createRoom.fulfilled.match(result)) {
            const createdRoom = result.payload;
            // Upload S3 image using the new room's ID!
            if (selectedFile && createdRoom && createdRoom.id) {
              const formData = new FormData();
              formData.append('file', selectedFile);
              await axiosClient.post(`/rooms/${createdRoom.id}/image`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
              });
            }
            setSelectedFile(null);
            const fileInput = document.getElementById('room-image-upload-input');
            if (fileInput) fileInput.value = '';
            resetForm();
            dispatch(fetchAllRooms());
          }
        }
      } catch (err) {
        console.error("Failed to save room details or upload image", err);
      } finally {
        setUploadingImage(false);
      }
    }
  });

  const handleFacilityToggle = (fac) => {
    const current = formik.values.facilities;
    if (current.includes(fac)) {
      formik.setFieldValue('facilities', current.filter(f => f !== fac));
    } else {
      formik.setFieldValue('facilities', [...current, fac]);
    }
  };

  const handleEditClick = (room) => {
    setEditingRoomId(room.id);
    formik.setValues({
      roomName: room.roomName,
      floorNumber: room.floorNumber,
      seatingCapacity: room.seatingCapacity,
      facilities: room.availableFacilities ? Array.from(room.availableFacilities) : [],
      imageUrls: room.imageUrls && room.imageUrls.length > 0 ? Array.from(room.imageUrls)[0] : ''
    });
  };

  const handleCancelEdit = () => {
    setEditingRoomId(null);
    formik.resetForm();
  };

  // Open Cancel Booking Modal
  const openCancelModal = (booking) => {
    setCancellingBooking(booking);
    setCancelReason('');
    setCancelReasonError('');
  };

  // Submit cancellation to backend
  const handleConfirmCancelBooking = async () => {
    if (!cancelReason.trim()) {
      setCancelReasonError('A valid cancellation reason is required');
      return;
    }
    const result = await dispatch(cancelBooking({ bookingId: cancellingBooking.id, reason: cancelReason }));
    if (cancelBooking.fulfilled.match(result)) {
      setCancellingBooking(null);
    }
  };

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !editingRoomId) return;

    const formData = new FormData();
    formData.append('file', file);
    setUploadingImage(true);

    try {
      const response = await axiosClient.post(`/rooms/${editingRoomId}/image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      if (response.data.success) {
        dispatch(fetchAllRooms());
        formik.setFieldValue('imageUrls', response.data.data.imageUrls[0]);
      }
    } catch (err) {
      console.error("Upload to S3 failed", err);
    } finally {
      setUploadingImage(false);
    }
  };

  // Generate Report download
  const handleGenerateReport = () => {
    let csvContent = "data:text/csv;charset=utf-8,ID,Meeting Title,Employee Name,Room Name,Date,Start,End,Status,Cancellation Reason\n";
    systemBookings.forEach(b => {
      csvContent += `${b.id},"${b.meetingTitle}","${b.employeeName}","${b.roomName}",${b.bookingDate},${b.startTime},${b.endTime},${b.status},"${b.cancellationReason || ''}"\n`;
    });
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `meetingroom_bookings_report_${new Date().toLocaleDateString('en-CA')}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const filteredBookings = systemBookings.filter(b => {
    if (bookingFilterStatus === 'ALL') return true;
    return b.status === bookingFilterStatus;
  });

  return (
    <div className="admin-container">
      <div className="admin-dashboard-header" style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <ShieldCheck style={{ color: 'var(--accent-blue)' }} size={26} /> Admin Management Portal
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
            Supervise room inventory, monitor all reservations, and view resource utilization analytics.
          </p>
        </div>

        <div className="admin-tabs">
          <button 
            className={`admin-tab-btn ${adminTab === 'rooms' ? 'active' : ''}`}
            onClick={() => setAdminTab('rooms')}
          >
            <Building size={16} /> Rooms Inventory
          </button>
          <button 
            className={`admin-tab-btn ${adminTab === 'bookings' ? 'active' : ''}`}
            onClick={() => setAdminTab('bookings')}
          >
            <Calendar size={16} /> All Reservations
          </button>
          <button 
            className={`admin-tab-btn ${adminTab === 'reports' ? 'active' : ''}`}
            onClick={() => setAdminTab('reports')}
          >
            <PieChart size={16} /> Analytics & Reports
          </button>
        </div>
      </div>

      {adminTab === 'rooms' && (
        <div className="admin-grid">
          {/* Create/Edit Room Form */}
          <form className="admin-form-card" onSubmit={formik.handleSubmit}>
            <h2>{editingRoomId ? 'Update Meeting Room' : 'Add New Meeting Room'}</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              {editingRoomId ? `Editing details for Room ID #${editingRoomId}` : 'Add a new space to the global office pool'}
            </p>

            {roomError && <div className="modal-error-alert">{roomError}</div>}

            <div className="form-group">
              <label className="form-label"><Building size={14} /> Room Name *</label>
              <input
                type="text"
                name="roomName"
                placeholder="e.g. Boardroom East"
                className="form-control"
                value={formik.values.roomName}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
              />
              {formik.touched.roomName && formik.errors.roomName && (
                <span className="form-error">{formik.errors.roomName}</span>
              )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Floor Number *</label>
                <input
                  type="number"
                  name="floorNumber"
                  className="form-control"
                  value={formik.values.floorNumber}
                  onChange={formik.handleChange}
                />
              </div>
              <div className="form-group">
                <label className="form-label"><Users size={14} /> Seating Capacity *</label>
                <input
                  type="number"
                  name="seatingCapacity"
                  className="form-control"
                  value={formik.values.seatingCapacity}
                  onChange={formik.handleChange}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label"><ImageIcon size={14} /> Image URL (Optional)</label>
              <input
                type="url"
                name="imageUrls"
                placeholder="https://images.unsplash.com/..."
                className="form-control"
                value={formik.values.imageUrls}
                onChange={formik.handleChange}
              />
              <div style={{ marginTop: '0.65rem' }}>
                <label className="form-label" style={{ fontSize: '0.78rem', fontWeight: 600 }}><ImageIcon size={12} /> Or Upload Local Image (AWS S3):</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => setSelectedFile(e.target.files[0])}
                  disabled={uploadingImage}
                  className="form-control"
                  style={{ padding: '0.4rem', fontSize: '0.85rem' }}
                  id="room-image-upload-input"
                />
                {uploadingImage && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--accent-blue)', fontSize: '0.78rem', fontWeight: 700, marginTop: '0.35rem' }}>
                    <Loader2 className="spinner-icon" size={12} /> Processing AWS S3 image upload...
                  </div>
                )}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Available Facilities</label>
              <div className="checkbox-group">
                {['PROJECTOR', 'WHITEBOARD', 'VIDEO_CONFERENCE', 'AIR_CONDITIONING'].map(fac => (
                  <label key={fac} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formik.values.facilities.includes(fac)}
                      onChange={() => handleFacilityToggle(fac)}
                    />
                    {fac.replace(/_/g, ' ')}
                  </label>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button type="submit" className="btn-search" disabled={roomLoading} style={{ flex: 1 }}>
                {roomLoading ? <Loader2 className="spinner-icon" size={18} /> : (editingRoomId ? <Edit size={16} /> : <Plus size={16} />)}
                {editingRoomId ? 'Update Room' : 'Create Room'}
              </button>
              {editingRoomId && (
                <button type="button" className="btn-outline-danger" onClick={handleCancelEdit} style={{ padding: '0 1rem' }}>
                  Cancel
                </button>
              )}
            </div>
          </form>

          {/* Existing Rooms Table */}
          <div className="admin-table-card">
            <h2>Registered Rooms ({allRooms.length})</h2>
            <div style={{ overflowX: 'auto' }}>
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Room Name</th>
                    <th>Floor</th>
                    <th>Capacity</th>
                    <th>Facilities</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {allRooms.map(room => (
                    <tr key={room.id}>
                      <td>#{room.id}</td>
                      <td style={{ fontWeight: 800 }}>{room.roomName}</td>
                      <td>Floor {room.floorNumber}</td>
                      <td>{room.seatingCapacity} Seats</td>
                      <td style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        {room.availableFacilities ? Array.from(room.availableFacilities).join(', ') : 'None'}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            className="nav-item"
                            style={{ padding: '0.35rem 0.5rem', background: '#f1f5f9', color: 'var(--accent-blue)' }}
                            onClick={() => handleEditClick(room)}
                            title="Edit Room"
                          >
                            <Edit size={14} />
                          </button>
                          <button
                            className="btn-outline-danger"
                            style={{ padding: '0.35rem 0.5rem' }}
                            onClick={() => dispatch(deleteRoom(room.id))}
                            title="Delete Room"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {adminTab === 'bookings' && (
        <div className="admin-table-card" style={{ maxWidth: '1280px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
            <h2>All Corporate Bookings ({filteredBookings.length})</h2>
            
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-secondary)' }}>Filter Status:</span>
              <select 
                className="form-control" 
                style={{ width: '160px', padding: '0.4rem 0.60rem' }}
                value={bookingFilterStatus}
                onChange={(e) => setBookingFilterStatus(e.target.value)}
              >
                <option value="ALL">All Bookings</option>
                <option value="UPCOMING">Upcoming</option>
                <option value="ONGOING">Ongoing</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>
          </div>

          {bookingError && <div className="modal-error-alert">{bookingError}</div>}

          <div style={{ overflowX: 'auto' }}>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Meeting Info</th>
                  <th>Employee</th>
                  <th>Room Location</th>
                  <th>Schedule</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredBookings.map(b => (
                  <tr key={b.id}>
                    <td>#{b.id}</td>
                    <td>
                      <div style={{ fontWeight: 800 }}>{b.meetingTitle}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{b.meetingDescription || 'No description'}</div>
                    </td>
                    <td>
                      <div>{b.employeeName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{b.employeeEmail}</div>
                    </td>
                    <td>{b.roomName}</td>
                    <td>
                      <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{b.bookingDate}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{b.startTime.slice(0, 5)} - {b.endTime.slice(0, 5)}</div>
                    </td>
                    <td>
                      <span className={`status-badge status-${b.status.toLowerCase()}`}>
                        {b.status}
                      </span>
                    </td>
                    <td>
                      {b.status === 'CANCELLED' && b.cancellationReason ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-rose)', fontSize: '0.75rem', fontWeight: 600, maxWidth: '180px' }} title={b.cancellationReason}>
                          <Info size={12} /> {b.cancellationReason}
                        </div>
                      ) : (b.status === 'UPCOMING' || b.status === 'ONGOING') ? (
                        <button
                          className="btn-outline-danger"
                          style={{ padding: '0.35rem 0.6rem', fontSize: '0.75rem' }}
                          onClick={() => openCancelModal(b)}
                        >
                          Cancel
                        </button>
                      ) : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {adminTab === 'reports' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {/* Metrics summary cards */}
          {metrics && (
            <div className="metrics-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem' }}>
              <div className="metric-card" style={{ background: '#f8fafc', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-light)' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 700 }}>TOTAL BOOKINGS</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.5rem', color: 'var(--text-primary)' }}>{metrics.totalBookings}</div>
              </div>
              <div className="metric-card" style={{ background: '#eff6ff', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-light)' }}>
                <div style={{ color: 'var(--accent-blue)', fontSize: '0.85rem', fontWeight: 700 }}>UPCOMING</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.5rem', color: 'var(--accent-blue)' }}>{metrics.upcomingBookings}</div>
              </div>
              <div className="metric-card" style={{ background: '#ecfdf5', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-light)' }}>
                <div style={{ color: 'var(--accent-green)', fontSize: '0.85rem', fontWeight: 700 }}>ONGOING</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.5rem', color: 'var(--accent-green)' }}>{metrics.ongoingBookings}</div>
              </div>
              <div className="metric-card" style={{ background: '#f0fdf4', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-light)' }}>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', fontWeight: 700 }}>COMPLETED</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.5rem', color: 'var(--text-secondary)' }}>{metrics.completedBookings}</div>
              </div>
              <div className="metric-card" style={{ background: '#fef2f2', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-light)' }}>
                <div style={{ color: 'var(--accent-rose)', fontSize: '0.85rem', fontWeight: 700 }}>CANCELLED</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, marginTop: '0.5rem', color: 'var(--accent-rose)' }}>{metrics.cancelledBookings}</div>
              </div>
            </div>
          )}

          <div className="admin-grid">
            {/* Utilization Rate Charts */}
            <div className="admin-table-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                <h2>Room Utilization Analytics</h2>
                <button className="btn-primary-gradient" onClick={handleGenerateReport}>
                  <Download size={16} /> Export Bookings CSV
                </button>
              </div>

              {metrics && metrics.roomUtilization && Object.keys(metrics.roomUtilization).length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  {Object.entries(metrics.roomUtilization).map(([room, count]) => {
                    const percentage = metrics.totalBookings > 0 ? ((count / metrics.totalBookings) * 100).toFixed(0) : 0;
                    return (
                      <div key={room}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.88rem', fontWeight: 700, marginBottom: '0.35rem' }}>
                          <span>{room}</span>
                          <span>{count} bookings ({percentage}%)</span>
                        </div>
                        <div style={{ height: '10px', background: '#f1f5f9', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                          <div style={{ width: `${percentage}%`, height: '100%', background: 'var(--accent-blue)', borderRadius: 'var(--radius-full)' }}></div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  No bookings metrics recorded yet. Start booking rooms to see utilization rates!
                </div>
              )}
            </div>

            {/* Analytics Info Card */}
            <div className="admin-form-card" style={{ justifyContent: 'center' }}>
              <div style={{ textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '1rem', alignItems: 'center' }}>
                <ShieldCheck size={48} style={{ color: 'var(--accent-blue)' }} />
                <h3>Enterprise Audit Logs</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: '1.5' }}>
                  This dashboard tracks all conference rooms and collaborative spaces inside the local office. Ensure correct time schedules are maintained.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Cancellation Reason Modal Popup */}
      {cancellingBooking && (
        <div className="modal-overlay">
          <div className="modal-card" style={{ maxWidth: '440px' }}>
            <div className="modal-header">
              <h3 className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-rose)' }}>
                <AlertTriangle size={18} /> Cancel Booking Reservation
              </h3>
              <button className="btn-close-modal" onClick={() => setCancellingBooking(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                You are cancelling the meeting <strong>"{cancellingBooking.meetingTitle}"</strong> by <strong>{cancellingBooking.employeeName}</strong>.
              </p>
              <div className="form-group" style={{ marginTop: '0.5rem' }}>
                <label className="form-label" style={{ fontWeight: 700 }}>Cancellation Reason *</label>
                <textarea
                  className="form-control"
                  style={{ minHeight: '80px', padding: '0.75rem', fontFamily: 'inherit' }}
                  placeholder="e.g. Schedule conflicts, team meeting relocated..."
                  value={cancelReason}
                  onChange={(e) => { setCancelReason(e.target.value); setCancelReasonError(''); }}
                />
                {cancelReasonError && <span className="form-error">{cancelReasonError}</span>}
              </div>
              <button 
                type="button" 
                className="btn-confirm-booking" 
                style={{ background: 'var(--accent-rose)', color: '#fff', boxShadow: 'none' }}
                onClick={handleConfirmCancelBooking}
              >
                Confirm Cancellation
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
