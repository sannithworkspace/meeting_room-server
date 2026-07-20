import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Check, Calendar, Clock, Building, User, Hash } from 'lucide-react';
import './ConfirmationModal.css';

const ConfirmationModal = ({ booking, onClose }) => {
  if (!booking) return null;

  return (
    <AnimatePresence>
      <div className="modal-overlay">
        <motion.div
          className="confirmation-card glass-panel"
          initial={{ opacity: 0, scale: 0.85 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.85 }}
          transition={{ duration: 0.25 }}
        >
          <div className="success-icon-badge">
            <Check size={36} />
          </div>

          <h2 className="confirmation-title">Booking Confirmed!</h2>
          <p className="confirmation-subtitle">
            Your meeting room reservation has been created successfully with zero collisions.
          </p>

          <div className="receipt-box">
            <div className="receipt-row">
              <span className="receipt-label"><Hash size={13} style={{ display: 'inline', marginRight: 4 }} /> Booking ID</span>
              <span className="receipt-value" style={{ color: 'var(--accent-cyan)' }}>#{booking.id}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label">Meeting Title</span>
              <span className="receipt-value">{booking.meetingTitle}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label"><Building size={13} style={{ display: 'inline', marginRight: 4 }} /> Room</span>
              <span className="receipt-value">{booking.roomName}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label"><User size={13} style={{ display: 'inline', marginRight: 4 }} /> Reserved By</span>
              <span className="receipt-value">{booking.employeeName}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label"><Calendar size={13} style={{ display: 'inline', marginRight: 4 }} /> Date</span>
              <span className="receipt-value">{booking.bookingDate}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label"><Clock size={13} style={{ display: 'inline', marginRight: 4 }} /> Time Slot</span>
              <span className="receipt-value">{booking.startTime} - {booking.endTime}</span>
            </div>

            <div className="receipt-row">
              <span className="receipt-label">Status</span>
              <span className="receipt-value" style={{ color: 'var(--accent-emerald)' }}>{booking.status}</span>
            </div>
          </div>

          <button className="btn-done" onClick={onClose}>
            Done & View History
          </button>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default ConfirmationModal;
