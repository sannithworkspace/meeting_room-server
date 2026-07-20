import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { createRoom, deleteRoom } from '../redux/slices/roomSlice';
import { ShieldCheck, Plus, Trash2, Building, Users, Image as ImageIcon, Loader2 } from 'lucide-react';
import './AdminDashboard.css';

const roomSchema = Yup.object().shape({
  roomName: Yup.string().required('Room name is required').min(2, 'Min 2 chars'),
  floorNumber: Yup.number().required('Floor number is required').integer('Integer required'),
  seatingCapacity: Yup.number().required('Capacity is required').positive('Must be positive').integer('Integer required'),
  imageUrls: Yup.string()
});

const AdminDashboard = () => {
  const dispatch = useDispatch();
  const { allRooms, loading, error } = useSelector((state) => state.rooms);

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

      const result = await dispatch(createRoom(roomData));
      if (createRoom.fulfilled.match(result)) {
        resetForm();
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

  return (
    <div className="admin-container">
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <ShieldCheck style={{ color: 'var(--accent-cyan)' }} size={24} /> Admin Portal
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
          Create and manage enterprise meeting room details and facility assets.
        </p>
      </div>

      <div className="admin-grid">
        {/* Create Room Form */}
        <form className="admin-form-card glass-panel" onSubmit={formik.handleSubmit}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Add New Meeting Room</h3>

          {error && <div className="modal-error-alert">{error}</div>}

          <div className="form-group">
            <label className="form-label"><Building size={14} /> Room Name *</label>
            <input
              type="text"
              name="roomName"
              placeholder="e.g. Innovation Lab"
              className="form-control"
              style={{ paddingLeft: '1rem' }}
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
              <label className="form-label">Floor Number</label>
              <input
                type="number"
                name="floorNumber"
                className="form-control"
                style={{ paddingLeft: '1rem' }}
                value={formik.values.floorNumber}
                onChange={formik.handleChange}
              />
            </div>
            <div className="form-group">
              <label className="form-label"><Users size={14} /> Seating</label>
              <input
                type="number"
                name="seatingCapacity"
                className="form-control"
                style={{ paddingLeft: '1rem' }}
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
              style={{ paddingLeft: '1rem' }}
              value={formik.values.imageUrls}
              onChange={formik.handleChange}
            />
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
                  {fac.replace('_', ' ')}
                </label>
              ))}
            </div>
          </div>

          <button type="submit" className="btn-search" disabled={loading} style={{ marginTop: '0.5rem' }}>
            {loading ? <Loader2 className="spinner-icon" size={18} /> : <Plus size={18} />}
            Create Room
          </button>
        </form>

        {/* Existing Rooms Table */}
        <div className="admin-table-card glass-panel">
          <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1rem' }}>
            Registered Meeting Rooms ({allRooms.length})
          </h3>

          <div style={{ overflowX: 'auto' }}>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Room Name</th>
                  <th>Floor</th>
                  <th>Capacity</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {allRooms.map(room => (
                  <tr key={room.id}>
                    <td>#{room.id}</td>
                    <td style={{ fontWeight: 700 }}>{room.roomName}</td>
                    <td>Floor {room.floorNumber}</td>
                    <td>{room.seatingCapacity} Seats</td>
                    <td>
                      <button
                        className="btn-outline-danger"
                        style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
                        onClick={() => dispatch(deleteRoom(room.id))}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
