/**
 * @file Navbar.js
 * @description 
 * This component renders the homepage navigation bar.
 * It includes:
 *  - A logo linking back to the homepage.
 *  - Navigation links to the contest listing and usage guide pages.
 *  - A "Log in" button that triggers a role selection modal and subsequent login/register flow.
 * 
 * Users can:
 *  - Browse contests and usage instructions.
 *  - Initiate the authentication flow by selecting their role and logging in or registering.
 * 
 * The component coordinates the display of RoleSelectModal, Login, and RegisterModal.
 * Layout and styling are managed through the Navbar.css file.
 * 
 * Developer: Beiqi Dai
 */

import React, { useState } from "react";
import "./Navbar.css";
import Login from "./Login";
import RegisterModal from "./RegisterModal";
import RoleSelectModal from "./RoleSelectModal";
import { Link } from 'react-router-dom';

function Navbar() {
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showRoleSelect, setShowRoleSelect] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);

  const handleLoginClick = () => {
    setShowRoleSelect(true);
  };

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
    setShowRoleSelect(false);
    setShowLogin(true);
  };

  return (
    <>
      {/* ✅ Only keep the navigation bar */}
      <header className="homepage-navbar">
        <Link to="/" className="homepage-navbar__logo">
          Questora
        </Link>

        <ul className="homepage-navbar__links">
          <li>
            <Link to="/contest-list" className="homepage-navbar-contest-button">
              See All Contests
            </Link>
          </li>
          <li>
            <Link to="/how-to-use" className="homepage-navbar-link">
              How To Use
            </Link>
          </li>
          <li>
            <button className="homepage-login-btn" onClick={handleLoginClick}>
              Log in
            </button>
          </li>
        </ul>
      </header>

      {/* ✅ The pop-up window has been moved outside */}
      {showRoleSelect && (
        <RoleSelectModal
          onSelectRole={handleRoleSelect}
          onClose={() => setShowRoleSelect(false)}
        />
      )}

      {showLogin && (
        <Login
          role={selectedRole}
          onClose={() => setShowLogin(false)}
          onShowRegister={() => {
            setShowLogin(false);
            setShowRegister(true);
          }}
        />
      )}

      {showRegister && (
        <RegisterModal
          onClose={() => setShowRegister(false)}
          role={selectedRole}
        />
      )}
    </>
  );
}

export default Navbar;
