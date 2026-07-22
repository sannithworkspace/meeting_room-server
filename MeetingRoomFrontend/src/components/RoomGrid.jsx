import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAllRooms } from '../redux/slices/roomSlice';
import RoomCard from './RoomCard';
import { Building2, Frown } from 'lucide-react';
import './RoomGrid.css';

const RoomGrid = ({ onSelectRoom }) => {
  const dispatch = useDispatch();
  const { allRooms, availableRooms, searched, loading } = useSelector((state) => state.rooms);

  useEffect(() => {
    if (allRooms.length === 0) {
      dispatch(fetchAllRooms());
    }
  }, [dispatch, allRooms.length]);

  if (!searched) {
    return (
      <section className="room-grid-section">
        <div className="empty-state glass-panel">
          <div className="empty-state-icon">
            <Building2 size={32} />
          </div>
          <h3 className="empty-state-title">Find Available Rooms</h3>
          <p className="empty-state-subtitle">
            Enter your preferred meeting details in the form above to discover available rooms.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className="room-grid-section">
      <div className="room-grid-header">
        <h2 className="room-grid-title">Available Meeting Rooms</h2>
        <span className="room-count-badge">
          {availableRooms.length} Rooms Available
        </span>
      </div>

      {availableRooms.length > 0 ? (
        <div className="room-grid">
          {availableRooms.map((room) => (
            <RoomCard key={room.id} room={room} onSelectRoom={onSelectRoom} />
          ))}
        </div>
      ) : (
        <div className="empty-state glass-panel">
          <div className="empty-state-icon">
            <Frown size={32} />
          </div>
          <h3 className="empty-state-title">No Rooms Available</h3>
          <p className="empty-state-subtitle">
            No rooms match your search filters for this time slot. Try adjusting the date or capacity requirements.
          </p>
        </div>
      )}
    </section>
  );
};

export default RoomGrid;
