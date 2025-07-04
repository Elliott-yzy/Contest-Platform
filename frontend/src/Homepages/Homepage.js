/**
 * @file HomePage.js
 * @description 
 * This component serves as the main landing page layout for the platform.
 * It includes:
 *  - A navigation bar at the top.
 *  - A scrollable container with multiple full-screen sections (Hero, Top Values, Footer).
 *  - Left-side pagination dots for quick navigation between sections.
 * 
 * Users can:
 *  - Scroll through the sections vertically.
 *  - Click on pagination dots to smoothly navigate to specific sections.
 * 
 * The scroll position is tracked to highlight the active pagination dot dynamically.
 * Layout and scrolling behavior are managed using React state, refs, and CSS styling.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React, { useRef, useState } from 'react';
import Navbar from './Navbar';
import Hero from './Hero';
import TopValues from './TopValues';
import Footer from './Footer';
import './HomePage.css';

const HomePage = () => {
  const scrollContainerRef = useRef(null);
  const [activeSection, setActiveSection] = useState(0);

  // Define section components (reorder or add/remove sections)
  const sections = [
    { component: <Hero />, key: "hero" },
    { component: <TopValues />, key: "topvalues" },
    // { component: <Tours />, key: "tours" },
    { component: <Footer />, key: "footer" },
  ];

  // Listen for scroll and update the active section
  const handleScroll = () => {
    if (scrollContainerRef.current) {
      const scrollTop = scrollContainerRef.current.scrollTop;
      const sectionHeight = window.innerHeight;
      const index = Math.round(scrollTop / sectionHeight);
      setActiveSection(index);
    }
  };

  // Scroll to the corresponding section when a dot is clicked
  const scrollToSection = (index) => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTo({
        top: index * window.innerHeight,
        behavior: 'smooth',
      });
    }
  };

  return (
    <>
      <Navbar />
      <div
        className="scroll-container"
        ref={scrollContainerRef}
        onScroll={handleScroll}
      >
        {sections.map((section, idx) => (
          <section key={section.key} className="scroll-section">
            {section.component}
          </section>
        ))}
      </div>

      {/* Left-side pagination dots */}
      <div className="pagination-dots left">
        {sections.map((_, idx) => (
          <div
            key={idx}
            className={`dot ${activeSection === idx ? 'active' : ''}`}
            onClick={() => scrollToSection(idx)}
          />
        ))}
      </div>
    </>
  );
};

export default HomePage;
