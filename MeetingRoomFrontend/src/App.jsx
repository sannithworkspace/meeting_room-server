import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import Navbar from './components/Navbar';
import Hero from './components/Hero';
import RoomGrid from './components/RoomGrid';
import BookingModal from './components/BookingModal';
import ConfirmationModal from './components/ConfirmationModal';
import BookingHistory from './components/BookingHistory';
import AuthModal from './components/AuthModal';
import AdminDashboard from './components/AdminDashboard';
import { clearLastConfirmedBooking } from './redux/slices/bookingSlice';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('explore'); // 'explore' | 'my-bookings' | 'admin'
  const [selectedRoom, setSelectedRoom] = useState(null);

  const dispatch = useDispatch();
  const { lastConfirmedBooking, bookingSuccess } = useSelector((state) => state.bookings);
  const { user } = useSelector((state) => state.auth);

  useEffect(() => {
    if (bookingSuccess) {
      setSelectedRoom(null);
    }
  }, [bookingSuccess]);

  const handleCloseConfirmation = () => {
    dispatch(clearLastConfirmedBooking());
    setActiveTab('my-bookings');
  };

  // Enforce login redirection lock: if user is not authenticated, show only the Auth page
  if (!user) {
    return <AuthModal onClose={() => {}} />;
  }

  return (
    <div className="app-container">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenAuth={() => {}}
      />

      <main className="app-main">
        {activeTab === 'explore' && (
          <>
            <Hero />
            <RoomGrid onSelectRoom={(room) => setSelectedRoom(room)} />
          </>
        )}

        {activeTab === 'my-bookings' && (
          <BookingHistory />
        )}

        {activeTab === 'admin' && (
          <AdminDashboard />
        )}
      </main>

      {/* Booking Form Modal */}
      {selectedRoom && (
        <BookingModal
          room={selectedRoom}
          onClose={() => setSelectedRoom(null)}
          onOpenAuth={() => setSelectedRoom(null)}
        />
      )}

      {/* Confirmation Receipt Modal */}
      {lastConfirmedBooking && (
        <ConfirmationModal
          booking={lastConfirmedBooking}
          onClose={handleCloseConfirmation}
        />
      )}

      <footer>
        <p>© 2026 MeetingRoom Enterprise Microservices System. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
