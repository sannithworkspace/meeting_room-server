import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAllRooms } from '../redux/slices/roomSlice';
import RoomCard from './RoomCard';
import { Building2, Frown, Loader2 } from 'lucide-react';
import './RoomGrid.css';

const OfficeSpaces = () => {
  const dispatch = useDispatch();
  const { allRooms, loading } = useSelector((state) => state.rooms);

  useEffect(() => {
    dispatch(fetchAllRooms());
  }, [dispatch]);

  return (
    <section className="room-grid-section">
      <div className="room-grid-header">
        <h2 className="room-grid-title">Office Spaces & Layout</h2>
        <span className="room-count-badge">
          {allRooms.length} Rooms Total
        </span>
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <Loader2 className="spinner-icon" size={32} style={{ color: 'var(--accent-blue)' }} />
        </div>
      ) : allRooms.length > 0 ? (
        <div className="room-grid">
          {allRooms.map((room) => (
            <RoomCard key={room.id} room={room} />
          ))}
        </div>
      ) : (
        <div className="empty-state glass-panel">
          <div className="empty-state-icon">
            <Frown size={32} />
          </div>
          <h3 className="empty-state-title">No Rooms Found</h3>
          <p className="empty-state-subtitle">
            There are currently no office spaces registered in the meeting system.
          </p>
        </div>
      )}
    </section>
  );
};

export default OfficeSpaces;
