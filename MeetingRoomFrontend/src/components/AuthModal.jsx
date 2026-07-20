import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { motion, AnimatePresence } from 'framer-motion';
import { loginUser, registerUser, clearAuthError } from '../redux/slices/authSlice';
import { X, Mail, Lock, User, Briefcase, LogIn, UserPlus, Loader2, CheckCircle2 } from 'lucide-react';
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
      if (loginUser.fulfilled.match(result)) {
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
    <AnimatePresence>
      <div className="modal-overlay">
        <motion.div
          className="auth-card glass-panel"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ duration: 0.25 }}
        >
          <div className="modal-header" style={{ padding: '0 0 1.25rem 0', marginBottom: '1.25rem' }}>
            <h3 className="modal-title">
              {tab === 'login' ? 'Sign In to Platform' : 'Create Employee Account'}
            </h3>
            <button className="btn-close-modal" onClick={onClose}>
              <X size={20} />
            </button>
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

          {error && <div className="modal-error-alert" style={{ marginBottom: '1.25rem' }}>{error}</div>}
          {registerSuccess && (
            <div className="room-summary-banner" style={{ marginBottom: '1.25rem', borderColor: 'var(--accent-emerald)', background: 'rgba(16, 185, 129, 0.1)' }}>
              <div style={{ color: 'var(--accent-emerald)', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 6 }}>
                <CheckCircle2 size={16} /> Employee registered! Switching to Sign In...
              </div>
            </div>
          )}

          {tab === 'login' ? (
            <form onSubmit={loginFormik.handleSubmit}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label"><Mail size={14} /> Email Address</label>
                <input
                  type="email"
                  name="email"
                  placeholder="admin@meetingroom.com"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={loginFormik.values.email}
                  onChange={loginFormik.handleChange}
                  onBlur={loginFormik.handleBlur}
                />
                {loginFormik.touched.email && loginFormik.errors.email && (
                  <span className="form-error">{loginFormik.errors.email}</span>
                )}
              </div>

              <div className="form-group" style={{ marginBottom: '1.25rem' }}>
                <label className="form-label"><Lock size={14} /> Password</label>
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
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
              <div className="form-group" style={{ marginBottom: '0.85rem' }}>
                <label className="form-label"><User size={14} /> Full Name</label>
                <input
                  type="text"
                  name="fullName"
                  placeholder="John Doe"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={registerFormik.values.fullName}
                  onChange={registerFormik.handleChange}
                  onBlur={registerFormik.handleBlur}
                />
                {registerFormik.touched.fullName && registerFormik.errors.fullName && (
                  <span className="form-error">{registerFormik.errors.fullName}</span>
                )}
              </div>

              <div className="form-group" style={{ marginBottom: '0.85rem' }}>
                <label className="form-label"><Mail size={14} /> Email Address</label>
                <input
                  type="email"
                  name="email"
                  placeholder="john.doe@company.com"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={registerFormik.values.email}
                  onChange={registerFormik.handleChange}
                  onBlur={registerFormik.handleBlur}
                />
                {registerFormik.touched.email && registerFormik.errors.email && (
                  <span className="form-error">{registerFormik.errors.email}</span>
                )}
              </div>

              <div className="form-group" style={{ marginBottom: '0.85rem' }}>
                <label className="form-label"><Lock size={14} /> Password</label>
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
                  value={registerFormik.values.password}
                  onChange={registerFormik.handleChange}
                  onBlur={registerFormik.handleBlur}
                />
                {registerFormik.touched.password && registerFormik.errors.password && (
                  <span className="form-error">{registerFormik.errors.password}</span>
                )}
              </div>

              <div className="form-group" style={{ marginBottom: '1.25rem' }}>
                <label className="form-label"><Briefcase size={14} /> Department</label>
                <input
                  type="text"
                  name="department"
                  placeholder="Engineering"
                  className="form-control"
                  style={{ paddingLeft: '1rem' }}
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
      </div>
    </AnimatePresence>
  );
};

export default AuthModal;
