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

  const roomsToDisplay = searched ? availableRooms : allRooms;

  return (
    <section className="room-grid-section">
      <div className="room-grid-header">
        <h2 className="room-grid-title">
          {searched ? 'Available Meeting Rooms' : 'All Enterprise Meeting Rooms'}
        </h2>
        <span className="room-count-badge">
          {roomsToDisplay.length} Rooms {searched ? 'Available' : 'Total'}
        </span>
      </div>

      {roomsToDisplay.length > 0 ? (
        <div className="room-grid">
          {roomsToDisplay.map((room) => (
            <RoomCard key={room.id} room={room} onSelectRoom={onSelectRoom} />
          ))}
        </div>
      ) : (
        <div className="empty-state glass-panel">
          <div className="empty-state-icon">
            <Frown size={32} />
          </div>
          <h3 className="empty-state-title">No Rooms Found</h3>
          <p className="empty-state-subtitle">
            {searched
              ? 'No rooms are free for your selected date, time slot, and capacity. Try adjusting your search filters.'
              : 'No meeting rooms are registered yet. Ask an admin to create a new room.'}
          </p>
        </div>
      )}
    </section>
  );
};

export default RoomGrid;
