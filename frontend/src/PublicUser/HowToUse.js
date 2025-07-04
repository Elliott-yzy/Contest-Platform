/**
 * HowToUse.js
 * 
 * This component provides instructions on how to use the Questora platform for different user roles.
 * It explains the platform features for:
 * - Public Users: Browsing contests, searching, viewing details, registering, and voting.
 * - Participants: Creating profiles, joining contests, submitting projects, receiving feedback, and celebrating achievements.
 * - Organizers: Creating contests, managing participants, reviewing submissions, and publishing results.
 * 
 * Role: Public User
 * Developer: Beiqi Dai
 */


import React from 'react';
import Navbar from '../Homepages/Navbar';
import Footer from '../Homepages/Footer';
import './HowToUse.css';

function HowToUse() {
  return (
    <>
      <Navbar />
      <div className="how-to-use-container">
        <div className="how-to-use-wrapper">

          {/* Top white box: Title + subtitle */}
          <div className="how-to-use-header-card">
            <h1 className="how-to-use-title">❤️ How to Use Questora</h1>
            <p className="how-to-use-subtitle">
              Your all-in-one platform for exploring, creating, managing, and participating in exciting project contests.
            </p>
          </div>

          {/* Public Users Section */}
          <div className="how-to-use-card">
            <h2 className="how-to-use-section-title">👥 Public Users</h2>
            <ul className="how-to-use-list">
              <li><strong>Browse Contests:</strong> Easily explore all contests without logging in.</li>
              <li><strong>Search & Filter:</strong> Find contests by <em>name</em>, <em>status</em>, or <em>category</em>.</li>
              <li><strong>View Details:</strong> Access contest requirements, timelines, and awards.</li>
              <li><strong>Register:</strong> Create an account to join contests and vote for submissions.</li>
            </ul>
          </div>

          {/* Participants Section */}
          <div className="how-to-use-card">
            <h2 className="how-to-use-section-title">🧑‍💻 Participants</h2>
            <ul className="how-to-use-list">
              <li><strong>Create Your Profile:</strong> Set up your interests and skills.</li>
              <li><strong>Join Contests:</strong> Quickly register and join contests.</li>
              <li><strong>Submit Projects:</strong> Upload your best work and showcase your talent.</li>
              <li><strong>Receive Notifications:</strong> Stay updated on deadlines and announcements.</li>
              <li><strong>View Ratings & Feedback:</strong> Learn and improve through reviews.</li>
              <li><strong>Celebrate Your Success:</strong> Win certificates and badges.</li>
              <li><strong>Vote for Others:</strong> Engage and support community creativity.</li>
            </ul>
          </div>

          {/* Organizers Section */}
          <div className="how-to-use-card">
            <h2 className="how-to-use-section-title">🏆 Organizers</h2>
            <ul className="how-to-use-list">
              <li><strong>Create Contests:</strong> Set up rules, themes, and judging criteria.</li>
              <li><strong>Manage Participants:</strong> Monitor contest activity and submissions.</li>
              <li><strong>Review Projects:</strong> Approve, reject, and provide feedback easily.</li>
              <li><strong>Publish Results:</strong> Announce winners and awards effortlessly.</li>
            </ul>
          </div>

        </div>
      </div>

      <Footer />
    </>
  );
}

export default HowToUse;
