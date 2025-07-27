/**
 * @file Sidebar.js
 * @description
 * Sidebar component for Organizer role.
 * Provides navigation links to:
 *  - Organizer Dashboard
 *  - Organizer Profile
 *  - Organizer Contest Management
 * 
 * Features:
 *  - Highlights the active route.
 *  - Verifies token existence before navigating.
 *  - Protects Profile navigation separately (using navigate).
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import './Sidebar.css';

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const [activePath, setActivePath] = useState(location.pathname);
  const email = localStorage.getItem("email");

  const handleLinkClick = (event, path) => {
    const token = localStorage.getItem('token');
    if (!token) {
      event.preventDefault();
      alert('You are not authorized. Please log in first.');
      return;
    }
    setActivePath(path);
  };

  const handleProfileClick = (event) => {
    event.preventDefault();
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authorized. Please log in first.");
      return;
    }
    if (email) {
      navigate(`/OrganizerProfile/${email}`);
      setActivePath(`/OrganizerProfile/${email}`);
    }
  };

  return (
    <aside className="sidebar">
      <nav>
        <ul>
          <li>
            <NavLink
              to={`/OrganizerDashboard/${email}`}
              className={({ isActive }) => isActive ? "active-link" : ""}
              onClick={(e) => handleLinkClick(e, `/OrganizerDashboard/${email}`)}
            >
              {activePath.startsWith("/OrganizerDashboard") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Dashboard">📊</span> Dashboard
            </NavLink>
          </li>
          <li>
            <a
              href={`/OrganizerProfile/${email}`}
              onClick={handleProfileClick}
              className={activePath.startsWith("/OrganizerProfile") ? "active-link" : ""}
            >
              {activePath.startsWith("/OrganizerProfile") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Profile">👤</span> Profile
            </a>
          </li>
          <li>
            <NavLink
              to={`/OrganizerContestList/${email}`}
              className={({ isActive }) => isActive ? "active-link" : ""}
              onClick={(e) => handleLinkClick(e, `/OrganizerContestList/${email}`)}
            >
              {activePath.startsWith("/OrganizerContestList") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Contest">🏆</span> Contest
            </NavLink>
          </li>
        </ul>
      </nav>
    </aside>
  );
}

export default Sidebar;
