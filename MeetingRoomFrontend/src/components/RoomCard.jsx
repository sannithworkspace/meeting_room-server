import React from 'react';
import { motion } from 'framer-motion';
import { Users, Tv, Presentation, Video, Wind, ArrowRight } from 'lucide-react';
import './RoomCard.css';

const defaultRoomImage = 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80';

const facilityIcons = {
  PROJECTOR: <Tv size={13} />,
  WHITEBOARD: <Presentation size={13} />,
  VIDEO_CONFERENCE: <Video size={13} />,
  AIR_CONDITIONING: <Wind size={13} />
};

const facilityLabels = {
  PROJECTOR: 'Projector',
  WHITEBOARD: 'Whiteboard',
  VIDEO_CONFERENCE: 'Video Conf',
  AIR_CONDITIONING: 'Air Con'
};

const RoomCard = ({ room, onSelectRoom }) => {
  const imageUrl = room.imageUrls && room.imageUrls.length > 0
    ? Array.from(room.imageUrls)[0]
    : defaultRoomImage;

  const facilities = room.availableFacilities ? Array.from(room.availableFacilities) : [];

  return (
    <motion.div
      className="room-card glass-panel"
      whileHover={{ y: -6 }}
      transition={{ duration: 0.25 }}
    >
      <div className="room-card-image-container">
        <img src={imageUrl} alt={room.roomName} className="room-card-image" />
        <span className="room-floor-badge">Floor {room.floorNumber}</span>
      </div>

      <div className="room-card-body">
        <div>
          <div className="room-card-header">
            <h3 className="room-title">{room.roomName}</h3>
            <div className="room-capacity-pill">
              <Users size={14} />
              <span>{room.seatingCapacity} seats</span>
            </div>
          </div>

          <div className="room-facilities-list" style={{ marginTop: '1rem' }}>
            {facilities.map((fac) => (
              <span key={fac} className="facility-tag">
                {facilityIcons[fac] || null}
                {facilityLabels[fac] || fac}
              </span>
            ))}
          </div>
        </div>

        {onSelectRoom && (
          <button className="btn-book-now" onClick={() => onSelectRoom(room)}>
            <span>Book Room</span>
            <ArrowRight size={16} />
          </button>
        )}
      </div>
    </motion.div>
  );
};

export default RoomCard;
