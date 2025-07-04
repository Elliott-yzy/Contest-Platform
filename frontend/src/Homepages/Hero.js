/**
 * @file Hero.js
 * @description 
 * This component renders the hero section on the homepage.
 * It includes:
 *  - A full-width background image.
 *  - A title and subtitle promoting the platform's purpose.
 *  - A "Join Now" button that triggers role selection and login/register modals.
 * 
 * Users can:
 *  - Select their role (e.g., participant, organizer) via a modal.
 *  - Log in or register based on their selected role.
 * 
 * This component coordinates the display and transitions between RoleSelectModal, Login, and RegisterModal components.
 * Layout and styling are managed separately in the Hero.css file.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React, { useState } from 'react';
import './Hero.css';
import Login from './Login';
import RegisterModal from './RegisterModal';
import RoleSelectModal from './RoleSelectModal';

function Hero() {
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
    <section className="hero">
      {/* Inside, a container is used to store pictures (or carousels). */}
      <div className="hero__image-wrapper">
        <img
          src="https://images.pexels.com/photos/13227317/pexels-photo-13227317.jpeg?auto=compress&cs=tinysrgb&w=600"
          alt="Azores"
          className="hero__image"
        />
      </div>

      <div className="hero__content">
        <h1 className="hero__title">Effortless Contest Management - From Submission to Evaluation, All in One Place!</h1>
        <p className="hero__subtitle">A place where nature and adventure unite</p>
        <button className="hero__button" onClick={handleLoginClick}>Join now</button>
      </div>

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
    </section>
  );
}

export default Hero;
