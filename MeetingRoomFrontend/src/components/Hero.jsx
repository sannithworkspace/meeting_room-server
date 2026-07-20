import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles } from 'lucide-react';
import './Hero.css';

const Hero = () => {
  return (
    <section className="hero-section">
      <motion.div
        className="hero-content"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <div className="hero-pill">
          <Sparkles size={14} /> Next-Gen Workspace Management
        </div>

        <h1 className="hero-title">
          Reserve Smart <span className="gradient-text">Meeting Spaces</span> Instantly.
        </h1>

        <p className="hero-subtitle">
          Real-time availability, zero booking collisions, and instant room slot reservations powered by enterprise Spring Cloud microservices.
        </p>
      </motion.div>
    </section>
  );
};

export default Hero;
