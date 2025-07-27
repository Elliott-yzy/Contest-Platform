/**
 * @file Footer.js
 * @description 
 * This component renders the footer section of the homepage.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */

import React from 'react';
import './Footer.css';
import one from './1.png';  // Left semicircle character image
import two from './2.png';  // Right decoration image

function Footer() {
  return (
    <footer className="footer">
      {/* Left decoration image */}
      <img src={one} alt="Decoration Left" className="footer__half-circle-image" />

      {/* Text area (center) */}
      <div className="footer__text-area">
        <div className="footer__columns">
          {/* Icon + label for each column */}
          <div className="footer__column">
            <div className="footer__icon-title">
              <span className="footer__icon">🏠</span>
              <span className="footer__text">Questora</span>
            </div>
            <ul>
              <li>Home</li>
              <li>About Us</li>
              <li>Contact</li>
            </ul>
          </div>

          <div className="footer__column">
            <div className="footer__icon-title">
              <span className="footer__icon">🏆</span>
              <span className="footer__text">Contests</span>
            </div>
            <ul>
              <li>Ongoing</li>
              <li>Upcoming</li>
              <li>Archives</li>
            </ul>
          </div>

          <div className="footer__column">
            <div className="footer__icon-title">
              <span className="footer__icon">📚</span>
              <span className="footer__text">Resources</span>
            </div>
            <ul>
              <li>How to Participate</li>
              <li>FAQs</li>
              <li>Help Center</li>
            </ul>
          </div>
        </div>

        {/* Social media icons */}
        <div className="footer__social">
          <div className="footer__icons">
            <a href="https://twitter.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-x-twitter"></i>
            </a>
            <a href="https://github.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-github"></i>
            </a>
            <a href="https://youtube.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-youtube"></i>
            </a>
            <a href="https://instagram.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-instagram"></i>
            </a>
            <a href="https://discord.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-discord"></i>
            </a>
            <a href="https://reddit.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-reddit"></i>
            </a>
            <a href="https://tiktok.com" target="_blank" rel="noopener noreferrer">
              <i className="fab fa-tiktok"></i>
            </a>
          </div>
        </div>

        {/* Bottom navigation */}
        <div className="footer__bottom">
          <ul className="footer__links">
            <li><a href="/privacy-policy">Privacy Policy</a></li>
            <li><a href="/terms-of-use">Terms of Use</a></li>
            <li><a href="/contributor-license">Contributor License Agreement</a></li>
            <li><a href="/sitemap">Sitemap</a></li>
          </ul>
          <p>© 2025 COMPW27 • A Web3 Project</p>
        </div>
      </div>

      {/* Right decoration image */}
      <img src={two} alt="Decoration Right" className="footer__right-image" />
    </footer>
  );
}

export default Footer;
