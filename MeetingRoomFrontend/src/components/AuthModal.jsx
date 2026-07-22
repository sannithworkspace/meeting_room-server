import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { motion, AnimatePresence } from 'framer-motion';
import { loginUser, registerUser, clearAuthError } from '../redux/slices/authSlice';
import { Mail, Lock, User, Briefcase, LogIn, UserPlus, Loader2, CheckCircle2, Building2 } from 'lucide-react';
import './AuthModal.css';

const loginSchema = Yup.object().shape({
  email: Yup.string().email('Invalid email').required('Email is required'),
  password: Yup.string().required('Password is required')
});

const registerSchema = Yup.object().shape({
  fullName: Yup.string().required('Full name is required').min(2, 'At least 2 characters'),
  email: Yup.string().email('Invalid email').required('Email is required'),
  password: Yup.string().required('Password is required').min(6, 'At least 6 characters'),
  department: Yup.string()
});

const AuthModal = ({ onClose }) => {
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const dispatch = useDispatch();
  const { loading, error, registerSuccess } = useSelector((state) => state.auth);

  const loginFormik = useFormik({
    initialValues: { email: '', password: '' },
    validationSchema: loginSchema,
    onSubmit: async (values) => {
      dispatch(clearAuthError());
      const result = await dispatch(loginUser(values));
      if (loginUser.fulfilled.match(result) && onClose) {
        onClose();
      }
    }
  });

  const registerFormik = useFormik({
    initialValues: { fullName: '', email: '', password: '', department: 'Engineering' },
    validationSchema: registerSchema,
    onSubmit: async (values) => {
      dispatch(clearAuthError());
      const result = await dispatch(registerUser(values));
      if (registerUser.fulfilled.match(result)) {
        setTimeout(() => {
          setTab('login');
          loginFormik.setFieldValue('email', values.email);
        }, 1500);
      }
    }
  });

  return (
    <div className="auth-page-container">
      {/* Left Column: Visual Video Banner */}
      <div className="auth-left-banner">
        <video autoPlay loop muted playsInline className="auth-bg-video">
          <source src="https://assets.mixkit.co/videos/preview/mixkit-business-people-meeting-in-boardroom-4890-large.mp4" type="video/mp4" />
        </video>
        <div className="auth-banner-overlay">
          <div className="auth-branding">
            <div className="auth-brand-logo">
              <Building2 size={28} />
            </div>
            <h1>MeetingRoom</h1>
            <p className="auth-badge">Enterprise Spaces</p>
          </div>
          <div className="auth-banner-footer">
            <h2>Next-Gen Shared Resource Scheduling</h2>
            <p>Coordinate conference rooms, huddle spaces, and boardrooms seamlessly across all enterprise offices.</p>
          </div>
        </div>
      </div>

      {/* Right Column: Clean Light-themed Card */}
      <div className="auth-right-panel">
        <AnimatePresence mode="wait">
          <motion.div
            key={tab}
            className="auth-card"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.25 }}
          >
            <div className="auth-form-header">
              <h2>{tab === 'login' ? 'Welcome Back' : 'Create Account'}</h2>
              <p>{tab === 'login' ? 'Please log in to manage your bookings' : 'Get access to meeting spaces instantly'}</p>
            </div>

            <div className="auth-tabs">
              <button
                className={`auth-tab-btn ${tab === 'login' ? 'active' : ''}`}
                onClick={() => { setTab('login'); dispatch(clearAuthError()); }}
              >
                Sign In
              </button>
              <button
                className={`auth-tab-btn ${tab === 'register' ? 'active' : ''}`}
                onClick={() => { setTab('register'); dispatch(clearAuthError()); }}
              >
                Sign Up
              </button>
            </div>

            {error && <div className="auth-error-alert">{error}</div>}
            
            {registerSuccess && (
              <div className="auth-success-alert">
                <CheckCircle2 size={16} /> Employee registered! Switching to Sign In...
              </div>
            )}

            {tab === 'login' ? (
              <form onSubmit={loginFormik.handleSubmit}>
                <div className="form-group">
                  <label className="form-label"><Mail size={14} /> Email Address</label>
                  <input
                    type="email"
                    name="email"
                    placeholder="name@company.com"
                    className="form-control"
                    value={loginFormik.values.email}
                    onChange={loginFormik.handleChange}
                    onBlur={loginFormik.handleBlur}
                  />
                  {loginFormik.touched.email && loginFormik.errors.email && (
                    <span className="form-error">{loginFormik.errors.email}</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label"><Lock size={14} /> Password</label>
                  <input
                    type="password"
                    name="password"
                    placeholder="••••••••"
                    className="form-control"
                    value={loginFormik.values.password}
                    onChange={loginFormik.handleChange}
                    onBlur={loginFormik.handleBlur}
                  />
                  {loginFormik.touched.password && loginFormik.errors.password && (
                    <span className="form-error">{loginFormik.errors.password}</span>
                  )}
                </div>

                <button type="submit" className="btn-auth-submit" disabled={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="spinner-icon" size={18} />
                      Authenticating...
                    </>
                  ) : (
                    <>
                      <LogIn size={18} />
                      Sign In
                    </>
                  )}
                </button>
              </form>
            ) : (
              <form onSubmit={registerFormik.handleSubmit}>
                <div className="form-group">
                  <label className="form-label"><User size={14} /> Full Name</label>
                  <input
                    type="text"
                    name="fullName"
                    placeholder="John Doe"
                    className="form-control"
                    value={registerFormik.values.fullName}
                    onChange={registerFormik.handleChange}
                    onBlur={registerFormik.handleBlur}
                  />
                  {registerFormik.touched.fullName && registerFormik.errors.fullName && (
                    <span className="form-error">{registerFormik.errors.fullName}</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label"><Mail size={14} /> Email Address</label>
                  <input
                    type="email"
                    name="email"
                    placeholder="john.doe@company.com"
                    className="form-control"
                    value={registerFormik.values.email}
                    onChange={registerFormik.handleChange}
                    onBlur={registerFormik.handleBlur}
                  />
                  {registerFormik.touched.email && registerFormik.errors.email && (
                    <span className="form-error">{registerFormik.errors.email}</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label"><Lock size={14} /> Password</label>
                  <input
                    type="password"
                    name="password"
                    placeholder="••••••••"
                    className="form-control"
                    value={registerFormik.values.password}
                    onChange={registerFormik.handleChange}
                    onBlur={registerFormik.handleBlur}
                  />
                  {registerFormik.touched.password && registerFormik.errors.password && (
                    <span className="form-error">{registerFormik.errors.password}</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label"><Briefcase size={14} /> Department</label>
                  <input
                    type="text"
                    name="department"
                    placeholder="Engineering"
                    className="form-control"
                    value={registerFormik.values.department}
                    onChange={registerFormik.handleChange}
                  />
                </div>

                <button type="submit" className="btn-auth-submit" disabled={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="spinner-icon" size={18} />
                      Creating Account...
                    </>
                  ) : (
                    <>
                      <UserPlus size={18} />
                      Register Account
                    </>
                  )}
                </button>
              </form>
            )}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
};

export default AuthModal;
