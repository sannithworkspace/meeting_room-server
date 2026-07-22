import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles } from 'lucide-react';
import SearchForm from './SearchForm';
import './Hero.css';

const Hero = () => {
  return (
    <section className="hero-section">
      {/* Background Video */}
      <video autoPlay loop muted playsInline className="hero-bg-video">
        <source src="https://assets.mixkit.co/videos/preview/mixkit-business-people-meeting-in-boardroom-4890-large.mp4" type="video/mp4" />
      </video>
      <div className="hero-video-overlay"></div>

      <div className="hero-grid-container">
        {/* Left Column: Heading, description, marketing pills */}
        <motion.div
          className="hero-left-content"
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div className="hero-pill">
            <Sparkles size={14} /> Next-Gen Workspace Management
          </div>

          <h1 className="hero-title">
            Book Premium <span className="gradient-text">Meeting Spaces</span> Effortlessly.
          </h1>

          <p className="hero-subtitle">
            Instantly reserve collaborative boardrooms, focus huddles, and presentation rooms powered by enterprise microservices.
          </p>
        </motion.div>

        {/* Right Column: SearchForm Card */}
        <motion.div
          className="hero-right-form"
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, delay: 0.15 }}
        >
          <SearchForm />
        </motion.div>
      </div>
    </section>
  );
};

export default Hero;
