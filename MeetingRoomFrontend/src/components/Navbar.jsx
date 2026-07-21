import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../redux/slices/authSlice';
import { Building2, CalendarCheck, ShieldCheck, LogIn, LogOut, User, Menu, X } from 'lucide-react';
import './Navbar.css';

const Navbar = ({ activeTab, setActiveTab, onOpenAuth }) => {
  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const isSuperOrAdmin = user?.roles?.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN');

  const handleNavClick = (tab) => {
    setActiveTab(tab);
    setMobileMenuOpen(false);
  };

  return (
    <header className="navbar-container">
      <div className="navbar-inner glass-panel">
        <div className="navbar-brand" onClick={() => handleNavClick('explore')}>
          <div className="brand-icon">
            <Building2 size={22} />
          </div>
          <span>MeetingRoom</span>
          <span className="brand-badge">Enterprise</span>
        </div>

        <button 
          className="mobile-menu-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle mobile navigation"
        >
          {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <nav className={`navbar-links ${mobileMenuOpen ? 'mobile-open' : ''}`}>
          <button
            className={`nav-item ${activeTab === 'explore' ? 'active' : ''}`}
            onClick={() => handleNavClick('explore')}
          >
            <Building2 size={16} />
            Explore Rooms
          </button>

          {user && (
            <button
              className={`nav-item ${activeTab === 'my-bookings' ? 'active' : ''}`}
              onClick={() => handleNavClick('my-bookings')}
            >
              <CalendarCheck size={16} />
              My Bookings
            </button>
          )}

          {isSuperOrAdmin && (
            <button
              className={`nav-item ${activeTab === 'admin' ? 'active' : ''}`}
              onClick={() => handleNavClick('admin')}
            >
              <ShieldCheck size={16} />
              Admin Portal
            </button>
          )}

          {user ? (
            <div className="user-nav-actions">
              <div className="user-pill">
                <User size={14} />
                <span>{user.fullName || user.email}</span>
              </div>
              <button
                className="btn-outline-danger"
                onClick={() => {
                  dispatch(logout());
                  setMobileMenuOpen(false);
                }}
                title="Logout"
              >
                <LogOut size={16} />
                <span className="logout-text">Logout</span>
              </button>
            </div>
          ) : (
            <button 
              className="btn-primary-gradient" 
              onClick={() => {
                onOpenAuth();
                setMobileMenuOpen(false);
              }}
            >
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
