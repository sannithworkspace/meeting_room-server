import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../redux/slices/authSlice';
import { Building2, CalendarCheck, ShieldCheck, LogIn, LogOut, User } from 'lucide-react';
import './Navbar.css';

const Navbar = ({ activeTab, setActiveTab, onOpenAuth }) => {
  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();

  const isSuperOrAdmin = user?.roles?.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN');

  return (
    <header className="navbar-container">
      <div className="navbar-inner glass-panel">
        <div className="navbar-brand" onClick={() => setActiveTab('explore')}>
          <div className="brand-icon">
            <Building2 size={22} />
          </div>
          <span>MeetingRoom</span>
          <span className="brand-badge">Enterprise</span>
        </div>

        <nav className="navbar-links">
          <button
            className={`nav-item ${activeTab === 'explore' ? 'active' : ''}`}
            onClick={() => setActiveTab('explore')}
          >
            <Building2 size={16} />
            Explore Rooms
          </button>

          {user && (
            <button
              className={`nav-item ${activeTab === 'my-bookings' ? 'active' : ''}`}
              onClick={() => setActiveTab('my-bookings')}
            >
              <CalendarCheck size={16} />
              My Bookings
            </button>
          )}

          {isSuperOrAdmin && (
            <button
              className={`nav-item ${activeTab === 'admin' ? 'active' : ''}`}
              onClick={() => setActiveTab('admin')}
            >
              <ShieldCheck size={16} />
              Admin Portal
            </button>
          )}

          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div className="user-pill">
                <User size={14} />
                <span>{user.fullName || user.email}</span>
              </div>
              <button
                className="btn-outline-danger"
                onClick={() => dispatch(logout())}
                title="Logout"
              >
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <button className="btn-primary-gradient" onClick={onOpenAuth}>
              <LogIn size={16} />
              Sign In
            </button>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Navbar;
