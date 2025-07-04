/**
 * @file Sidebar.jsx
 * @description 
 * This component renders the sidebar navigation for the admin dashboard.
 * It allows admin users to:
 *  - Navigate to the Dashboard, Profile, Account Management, and Competitions Management pages.
 *  - Highlight the currently active page with a visual indicator.
 *  - Ensure that navigation is only allowed when the user is authenticated (token exists).
 * 
 * The component uses React Router for navigation and localStorage to retrieve user session information.
 * It provides visual feedback on the active navigation link and includes simple role-based access control checks.
 * 
 * Role: Admin
 * Developer: Zhaoyi Yang
 */


import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import './Sidebar.css';

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const [activePath, setActivePath] = useState(location.pathname);
  const email = localStorage.getItem("email");

  const handleDashboard = (event) => {
    event.preventDefault();
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authorized. Please log in first.");
      return;
    }
    if (email) {
      navigate(`/AdminDashboard`);
      setActivePath(`/AdminDashboard`);
    }
  };

  const handleProfileClick = (event) => {
    event.preventDefault();
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authorized. Please log in first.");
      return;
    }
    if (email) {
      navigate(`/AdminProfile`);
      setActivePath(`/AdminProfile`);
    }
  };

  const handleOrganizerManageClick = (event) => {
    event.preventDefault();
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authorized. Please log in first.");
      return;
    }
    navigate(`/AdminAccountManage`);
    setActivePath(`/AdminAccountManage`);
  };

  const handleAllCompetitionsClick = (event) => {
    event.preventDefault();
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authorized. Please log in first.");
      return;
    }
    navigate(`/AllCompetitions`);
    setActivePath(`/AllCompetitions`);
  };

  return (
    <aside className="sidebar">
      <nav>
        <ul>
          <li>
            <a
              href={`/AdminDashboard`}
              onClick={handleDashboard}
              className={activePath.startsWith("/AdminDashboard") ? "active-link" : ""}
            >
              {activePath.startsWith("AdminDashboard") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Profile">📊</span> Dashboard
            </a>
          </li>
          <li>
            <a
              href={`/AdminProfile`}
              onClick={handleProfileClick}
              className={activePath.startsWith("/AdminProfile") ? "active-link" : ""}
            >
              {activePath.startsWith("/AdminProfile") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Profile">👤</span> Profile
            </a>
          </li>
          <li>
            <a
              href={`/AdminAccountManage`}
              onClick={handleOrganizerManageClick}
              className={activePath.startsWith("/AdminAccountManage") ? "active-link" : ""}
            >
              {activePath.startsWith("/AdminAccountManage") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Manage">🛠️</span> Accounts Manage
            </a>
          </li>
          <li>
            <a
              href={`/AllCompetitions`}
              onClick={handleAllCompetitionsClick}
              className={activePath.startsWith("/AllCompetitions") ? "active-link" : ""}
            >
              {activePath.startsWith("/AllCompetitions") && <span className="arrow-icon">▶</span>}
              <span role="img" aria-label="Competitions">📋</span> Competitions Manage
            </a>
          </li>
        </ul>
      </nav>
    </aside>
  );
}

export default Sidebar;
