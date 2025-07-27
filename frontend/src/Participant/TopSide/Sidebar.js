/**
 * Sidebar.js
 * 
 * A fixed navigation sidebar component for participants to access different sections:
 * Profile, Contest, Team, Project, and Rating pages.
 * Highlights the current active page and prevents access if not logged in.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import './Sidebar.css';

function Sidebar() {
  const location = useLocation();
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

  return (
    <aside className="participant-sidebar">
      <nav>
        <ul>
          <li>
            <NavLink
              to={`/profile/${email}`}
              className={({ isActive }) => (isActive ? "active-link" : "")}
              onClick={(e) => handleLinkClick(e, `/profile/${email}`)}
            >
              {activePath.startsWith("/profile") && <span className="participant-arrow-icon">▶</span>}
              <span role="img" aria-label="profile">👤</span> Profile
            </NavLink>
          </li>
          <li>
            <NavLink
              to={`/contest/${email}`}
              className={({ isActive }) => (isActive ? "active-link" : "")}
              onClick={(e) => handleLinkClick(e, `/contest/${email}`)}
            >
              {activePath.startsWith("/contest") && <span className="participant-arrow-icon">▶</span>}
              <span role="img" aria-label="contest">🏆</span> Contest
            </NavLink>
          </li>
          <li>
            <NavLink
              to={`/teams/${email}`}
              className={({ isActive }) => (isActive ? "active-link" : "")}
              onClick={(e) => handleLinkClick(e, `/teams/${email}`)}
            >
              {activePath.startsWith("/teams") && <span className="participant-arrow-icon">▶</span>}
              <span role="img" aria-label="team">👥</span> Team
            </NavLink>

          </li>
          <li>
            <NavLink
              to={`/project/${email}`}
              className={({ isActive }) => (isActive ? "active-link" : "")}
              onClick={(e) => handleLinkClick(e, `/project/${email}`)}
            >
              {activePath.startsWith("/project") && <span className="participant-arrow-icon">▶</span>}
              <span role="img" aria-label="project">📁</span> Project
            </NavLink>
          </li>
          <li>
            <NavLink
              to={`/rating/${email}`}
              className={({ isActive }) => (isActive ? "active-link" : "")}
              onClick={(e) => handleLinkClick(e, `/rating/${email}`)}
            >
              {activePath.startsWith("/rating") && <span className="participant-arrow-icon">▶</span>}
              <span role="img" aria-label="rating">⭐</span> Rating
            </NavLink>
          </li>
        </ul>
      </nav>
    </aside>
  );
}

export default Sidebar;
